static uint64_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000ull + v.tv_usec;
}

#define HZ (1)
#define T (1000000/HZ)

int main() {
  uint64_t extra = 0;
	
  Main_VARS vars = {};
  while(true) {
    uint64_t start = useconds();
    bloqqi_main(&vars);
    uint64_t end = useconds();
    uint64_t run_time = end - start;

    start = end;
    uint64_t sleep_time = T - run_time - extra;
    assert(sleep_time > 0);
    usleep(sleep_time);
    end = useconds();
    uint64_t time_slept = end - start;
    if (run_time + time_slept > T) {
      extra = run_time + time_slept - T;
    } else {
      extra = 0;
    }
  }
}
