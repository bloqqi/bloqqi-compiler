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