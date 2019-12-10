#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <sys/time.h>
#include <string.h>
#include <pthread.h>
#include "MQTTAsync.h"

#define ADDRESS     "localhost:1883"
#define QOS         1
#define TIMEOUT     10000L

static useconds_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000 + v.tv_usec;
}