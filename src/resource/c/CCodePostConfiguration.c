static uint64_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000ull + v.tv_usec;
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