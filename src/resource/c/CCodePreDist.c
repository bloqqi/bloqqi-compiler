#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <sys/time.h>
#include <string.h>
#include <pthread.h>
#include "MQTTAsync.h"

#define ADDRESS     "tcp://localhost:1883"
#define QOS         1
#define TIMEOUT     10000L
