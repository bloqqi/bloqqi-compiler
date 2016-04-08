#include <stdio.h>
#include <stdbool.h>
#include <pthread.h>
#include <assert.h>
#include <sys/time.h>
#include <unistd.h>
#include <math.h>

static double sec()
{
    struct timeval v;
    gettimeofday(&v, NULL);
    return v.tv_sec + v.tv_usec/ (double) 1000000;
}

/** Tank implementation */
struct {
	bool upper_valve;
	bool lower_valve;
	double level;  // In M
} tank;

static pthread_mutex_t mutex_tank = PTHREAD_MUTEX_INITIALIZER;

#define RADIUS (1)
#define HEIGHT (2)
#define IN_VOLUME_SPEED (1)
#define OUT_AREA (1e-2)

// Volume out: V = a * sqrt(2 * g * h)   
//             where a is the area of the hole
static void *simulate_tank(void *arg) 
{
	double time = sec();
	while (true) {
		pthread_mutex_lock(&mutex_tank);
		double time_elapsed = sec() - time;
		time = sec();
		if (tank.lower_valve) {
			double vDelta = OUT_AREA * sqrt(2 * 9.82 * tank.level) * time_elapsed;
			tank.level -= vDelta / (M_PI * RADIUS * RADIUS);
		}
		if (tank.upper_valve) {
			double vDelta = IN_VOLUME_SPEED * time_elapsed;
			tank.level += vDelta / (M_PI * RADIUS * RADIUS);
		}
		if (tank.level >= HEIGHT) {
			printf("ERROR! VOLUME OVERFLOW!!!\n");
			tank.level = HEIGHT;
		}
		pthread_mutex_unlock(&mutex_tank);
		usleep(50000);
	}
}

static bool is_tank_initialized = false;
static void init_tank() 
{
	is_tank_initialized = true;
	tank.upper_valve = false;
	tank.lower_valve = false;
	tank.level = 0;
	
	pthread_t thread;
	int rc = pthread_create(&thread, NULL, simulate_tank, NULL);
	assert(rc == 0);
}

/** Tank interface */
void UpperValve(bool open) 
{ 
	if (!is_tank_initialized) {
		init_tank();
	}
	pthread_mutex_lock(&mutex_tank);
	if (tank.upper_valve != open) {
		printf("Upper valve is now %s\n", open ? "open" : "closed"); 
	}
	tank.upper_valve = open;
	pthread_mutex_unlock(&mutex_tank);
}
void LowerValve(bool open) 
{ 
	if (!is_tank_initialized) {
		init_tank();
	}
	pthread_mutex_lock(&mutex_tank);
	if (tank.lower_valve != open) {
		printf("Lower valve is now %s\n", open ? "open" : "closed"); 
	}
	tank.lower_valve = open;
	pthread_mutex_unlock(&mutex_tank);
}
int StatusMonitor() 
{ 
	if (!is_tank_initialized) {
		init_tank();
	}
	pthread_mutex_lock(&mutex_tank);
	int level = (int) round(tank.level * 100); 
	pthread_mutex_unlock(&mutex_tank);
	return level; 
}


/** Other */
int Sub(int in1, int in2) {
	return in1 - in2;
}

int Add(int in1, int in2) {
	return in1 + in2;
}

int Neg(int in) {
	return -in;
}

bool GTZ(int in1) {
	return in1 > 0;
}

int Inc(int in) {
	return in+1;
}
void Print(int in) {
	printf("%d\n", in);
}
