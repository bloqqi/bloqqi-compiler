void on_connect_failure(void* context, MQTTAsync_failureData* response);
void bloqqi_run_main(MQTTAsync client);

const char *username = NULL;
const char *password = NULL;

// Don't start the Bloqqi program until we are connected to the Broker
volatile bool connected = false;
pthread_mutex_t connected_lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t connected_cond = PTHREAD_COND_INITIALIZER;

void set_connected(bool value) {
  pthread_mutex_lock(&connected_lock);
  connected = value;
  pthread_cond_signal(&connected_cond);
  pthread_mutex_unlock(&connected_lock);
}
bool is_connected() {
  pthread_mutex_lock(&connected_lock);
  bool ret = connected;
  pthread_mutex_unlock(&connected_lock);
  return ret;
}
void wait_until_connected() {
  pthread_mutex_lock(&connected_lock);
  while (!connected) {
    pthread_cond_wait(&connected_cond, &connected_lock);
  }
  pthread_mutex_unlock(&connected_lock);
}

void on_connect(void* context, MQTTAsync_successData* response) {
  MQTTAsync client = (MQTTAsync)context;

  set_connected(true);
  printf("Connected to broker\n");

  publish_parameters_on_connect(client);

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

  set_connected(false);

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

  init_input();

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
