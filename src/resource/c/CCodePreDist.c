#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <sys/time.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include "MQTTAsync.h"

static uint64_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000ull + v.tv_usec;
}

void send_message(MQTTAsync client, const char *topic, char *payload, bool retain) {
  MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
  MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
  int rc;

  opts.context = client;
  pubmsg.payload = payload;
  pubmsg.payloadlen = (int)strlen(payload) + 1;
  pubmsg.qos = 0;
  pubmsg.retained = retain;

  if ((rc = MQTTAsync_sendMessage(client, topic, &pubmsg, &opts)) != MQTTASYNC_SUCCESS) {
    printf("Failed to start send_message, return code %d\n", rc);
  }
}

void send_message_int(MQTTAsync client, const char *topic, int value, bool retain) {
  char payload[20];
  sprintf(payload, "%d", value);
  send_message(client, topic, payload, retain);
}

void send_message_double(MQTTAsync client, const char *topic, double value, bool retain) {
  char payload[20];
  sprintf(payload, "%lf", value);
  send_message(client, topic, payload, retain);
}
