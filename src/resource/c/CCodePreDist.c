#include <errno.h>
#include <limits.h>
#include <math.h>
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

// Function adapted:
// from https://wiki.sei.cmu.edu/confluence/display/c/ERR34-C.+Detect+errors+when+converting+a+string+to+a+number
bool read_int(const char *s, int *ip) {
  char *end;
  errno = 0;

  const long sl = strtol(s, &end, 10);
  bool ok = false;

  if (end == s) {
    fprintf(stderr, "Input error: '%s' not a decimal number\n", s);
  } else if (*end != '\0') {
    fprintf(stderr, "Input warning: '%s' extra characters at end of input '%s'\n", s, end);
    ok = true;
  } else if ((sl == LONG_MIN || sl == LONG_MAX) && errno == ERANGE) {
    fprintf(stderr, "Input error: '%s' out of range of type long\n", s);
  } else if (sl > INT_MAX) {
    fprintf(stderr, "Input error: '%ld' greater than INT_MAX\n", sl);
  } else if (sl < INT_MIN) {
    fprintf(stderr, "Input error: '%ld' less than INT_MIN\n", sl);
  } else {
    ok = true;
  }
  if (ok) {
    *ip = (int) sl;
  }
  return ok;
}

bool read_double(const char *s, double *dp) {
  char *end;
  errno = 0;

  const double d = strtod(s, &end);
  bool ok = false;

  if (end == s) {
    fprintf(stderr, "Input error: '%s' not a floating point number\n", s);
  } else if (*end != '\0') {
    fprintf(stderr, "Input warning '%s' extra characters at end of input '%s'\n", s, end);
    ok = true;
  } else if ((d == -HUGE_VAL || d == HUGE_VAL) && errno == ERANGE) {
    fprintf(stderr, "Input error: '%s' out of range of type double\n", s);
  } else {
    ok = true;
  }
  if (ok) {
    *dp = d;
  }
  return ok;
}
