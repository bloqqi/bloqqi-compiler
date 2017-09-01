static useconds_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000 + v.tv_usec;
}

#define HZ (1)
#define T (1000000/HZ)

int main() {
  useconds_t extra = 0;
	
  Main_VARS vars = {};
  while(true) {
    useconds_t start = useconds();
    bloqqi_main(&vars);
    useconds_t end = useconds();
    useconds_t run_time = end - start;

    start = end;
    useconds_t sleep_time = T - run_time - extra;
    assert(sleep_time > 0);
    usleep(sleep_time);
    end = useconds();
    useconds_t time_slept = end - start;
    if (run_time + time_slept > T) {
      extra = run_time + time_slept - T;
    } else {
      extra = 0;
    }
  }
}
