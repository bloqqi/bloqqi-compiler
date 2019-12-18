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

bool read_int(const char *s, int *ip);
bool read_double(const char *s, double *dp);
static uint64_t useconds();

void send_message(MQTTAsync client, const char *topic, char *payload, bool retain);
void send_message_int(MQTTAsync client, const char *topic, int value, bool retain);
void send_message_double(MQTTAsync client, const char *topic, double value, bool retain);
void on_connect_failure(void* context, MQTTAsync_failureData* response);
void bloqqi_run_main(MQTTAsync client);

// Broker username and password
const char *username = NULL;
const char *password = NULL;

// Connected flag
volatile bool connected = false;
pthread_mutex_t connected_lock = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t connected_cond = PTHREAD_COND_INITIALIZER;
