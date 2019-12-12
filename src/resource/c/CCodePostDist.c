void on_connect_failure(void* context, MQTTAsync_failureData* response);
void bloqqi_run_main(MQTTAsync client);

const char *username = NULL;
const char *password = NULL;

// Don't start the Bloqqi program until we are connected to the Broker
volatile int disconnected = 1;
pthread_mutex_t disconnected_lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t disconnected_cond = PTHREAD_COND_INITIALIZER;

void set_disconnected(int value) {
  pthread_mutex_lock(&disconnected_lock);
  disconnected = value;
  pthread_cond_signal(&disconnected_cond);
  pthread_mutex_unlock(&disconnected_lock);
}
int is_disconnected() {
  pthread_mutex_lock(&disconnected_lock);
  int ret = disconnected;
  pthread_mutex_unlock(&disconnected_lock);
  return ret;
}
void wait_until_connected() {
  pthread_mutex_lock(&disconnected_lock);
  while (disconnected) {
    pthread_cond_wait(&disconnected_cond, &disconnected_lock);
  }
  pthread_mutex_unlock(&disconnected_lock);
}

void on_connect(void* context, MQTTAsync_successData* response) {
  set_disconnected(0);

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

MQTTAsync_connectOptions get_connect_options(MQTTAsync client) {
  MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
  conn_opts.keepAliveInterval = 20;
  conn_opts.cleansession = 1;
  conn_opts.onSuccess = on_connect;
  conn_opts.onFailure = on_connect_failure;
  conn_opts.context = client;
  conn_opts.username = username;
  conn_opts.password = password;
  return conn_opts;
}


void reconnect(MQTTAsync client) {
  MQTTAsync_connectOptions conn_opts = get_connect_options(client);
  int rc;
  if ((rc = MQTTAsync_connect(client, &conn_opts)) != MQTTASYNC_SUCCESS) {
    printf("Failed to start connect, return code %d\n. Retrying...", rc);
  }
}

void on_connect_failure(void* context, MQTTAsync_failureData* response) {
  MQTTAsync client = (MQTTAsync)context;

  printf("Connection failed, rc %d\n", response ? response->code : 0);
  if (response && response->code >= 1 && response->code <= 5) {
    if (response->code == 1) {
      printf("Connection refused: unacceptable protocol version\n");
    } else if (response->code == 2) {
      printf("Connection refused: identifier rejected\n");
    } else if (response->code == 3) {
      printf("Connection refused: server unavailable\n");
    } else if (response->code == 4) {
      printf("Connection refused: bad username or password\n");
    } else if (response->code == 5) {
      printf("Connection refused: not authorized\n");
    }
  }

  if (response && response->code < 0) {
    printf("Reconnecting\n");
    usleep(1000000L);
    reconnect(client);
  } else {
    exit(EXIT_FAILURE);
  }
}


void on_connection_lost(void *context, char *cause) {
  MQTTAsync client = (MQTTAsync)context;

  set_disconnected(1);

  printf("\nConnection lost\n");
  printf("     cause: %s\n", cause);
  printf("Reconnecting\n");
  reconnect(client);
}


int on_message_recieved(void *context, char *topicName, int topicLen, MQTTAsync_message *message) {
  handle_input(topicName, message->payload);

  MQTTAsync_freeMessage(&message);
  MQTTAsync_free(topicName);

  return 1;
}

void send_message(MQTTAsync client, const char *topic, char *payload) {
  MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
  MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
  int rc;

  opts.context = client;
  pubmsg.payload = payload;
  pubmsg.payloadlen = (int)strlen(payload) + 1;
  pubmsg.qos = 0;
  pubmsg.retained = 0;

  if ((rc = MQTTAsync_sendMessage(client, topic, &pubmsg, &opts)) != MQTTASYNC_SUCCESS) {
    printf("Failed to start send_message, return code %d\n", rc);
  }
}

void send_message_int(MQTTAsync client, const char *topic, int value) {
  char payload[20];
  sprintf(payload, "%d", value);
  send_message(client, topic, payload);
}

void send_message_double(MQTTAsync client, const char *topic, double value) {
  char payload[20];
  sprintf(payload, "%lf", value);
  send_message(client, topic, payload);
}

uint64_t my_sleep(
    uint64_t start,
    uint64_t end,
    uint64_t extra,
    uint64_t period) {
  uint64_t run_time = end - start;
  start = end;
  uint64_t sleep_time = period - run_time - extra;
  assert(sleep_time > 0);
  usleep(sleep_time);
  end = useconds();
  uint64_t time_slept = end - start;
  if (run_time + time_slept > period) {
    extra = run_time + time_slept - period;
  } else {
    extra = 0;
  }
  return extra;
}


int main(int argc, char* argv[]) {
  char *address;
  if (argc == 1) {
    address = "localhost:1883";
  } else if (argc == 2) {
    address = argv[1];
  } else if (argc == 4) {
    address = argv[1];
    username = argv[2];
    password = argv[3];
  } else {
    printf("Too many arguments given\n");
    return 1;
  }

  MQTTAsync client;

  MQTTAsync_create(&client, address, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL);
  MQTTAsync_setCallbacks(client, client, on_connection_lost, on_message_recieved, NULL);

  if (username == NULL) {
    printf("Connecting to broker '%s'\n", address);
  } else {
    printf("Connecting to broker '%s' with username '%s'\n", address, username);
  }
  reconnect(client);

  // Wait until we are connected to the broker
  wait_until_connected();

  printf("Starting Bloqqi program\n");
  bloqqi_run_main(client);
}
