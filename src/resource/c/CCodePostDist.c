void on_connect_failure(void* context, MQTTAsync_failureData* response);
void bloqqi_run_main(MQTTAsync client);

void on_connect(void* context, MQTTAsync_successData* response) {
  printf("Connected to broker\n");

  MQTTAsync client = (MQTTAsync)context;
  MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;

  //opts.onSuccess = onSubscribe;
  //opts.onFailure = onSubscribeFailure;
  opts.context = client;

  for (int i = 0; i < sizeof(SIGNALS)/sizeof(char *); i++) {
    int rc = MQTTAsync_subscribe(client, SIGNALS[i], 1, &opts);
    if (rc != MQTTASYNC_SUCCESS) {
      printf("Failed to start subscribe to %s, return code %d\n", SIGNALS[i], rc);
      exit(EXIT_FAILURE);
    }
  }
}

void reconnect(MQTTAsync client, MQTTAsync_connectOptions conn_opts) {
  conn_opts.keepAliveInterval = 20;
  conn_opts.cleansession = 1;
  conn_opts.onSuccess = on_connect;
  conn_opts.onFailure = on_connect_failure;
  conn_opts.context = client;
  int rc;
  if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS) {
    printf("Failed to start connect, return code %d\n. Retrying...", rc);
  }
}

void on_connect_failure(void* context, MQTTAsync_failureData* response) {
  printf("Connect failed, rc %d\n", response ? response->code : 0);

  MQTTAsync client = (MQTTAsync)context;
  MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;

  usleep(1000000L);
  printf("Reconnecting\n");
  reconnect(client, conn_opts);
}


void connection_lost(void *context, char *cause) {
  MQTTAsync client = (MQTTAsync)context;
  MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;

  printf("\nConnection lost\n");
  printf("     cause: %s\n", cause);

  printf("Reconnecting\n");
  reconnect(client, conn_opts);
}


int message_recieved(void *context, char *topicName, int topicLen, MQTTAsync_message *message) {
  handle_input(topicName, message->payload);

  MQTTAsync_freeMessage(&message);
  MQTTAsync_free(topicName);

  return 1;
}

void send_message(MQTTAsync client, const char *topic, int value) {
  MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
  MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
  int rc;

  opts.context = client;

  char payload[100];
  sprintf(payload, "%d", value);
  pubmsg.payload = payload;
  pubmsg.payloadlen = (int)strlen(payload) + 1;
  pubmsg.qos = 0;
  pubmsg.retained = 0;

  if ((rc = MQTTAsync_sendMessage(client, topic, &pubmsg, &opts)) != MQTTASYNC_SUCCESS) {
    printf("Failed to start send_message, return code %d\n", rc);
    //exit(EXIT_FAILURE);
  }
}

static useconds_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000 + v.tv_usec;
}

useconds_t my_sleep(
    useconds_t start,
    useconds_t end,
    useconds_t extra,
    useconds_t period) {
  useconds_t run_time = end - start;
  start = end;
  useconds_t sleep_time = period - run_time - extra;
  assert(sleep_time > 0);
  usleep(sleep_time);
  end = useconds();
  useconds_t time_slept = end - start;
  if (run_time + time_slept > period) {
    extra = run_time + time_slept - period;
  } else {
    extra = 0;
  }
  return extra;
}


int main(int argc, char* argv[]) {
  MQTTAsync client;
  MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;

  MQTTAsync_create(&client, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
  MQTTAsync_setCallbacks(client, client, connection_lost, message_recieved, NULL);

  reconnect(client, conn_opts);

  usleep(1000000L);

  bloqqi_run_main(client);
}
