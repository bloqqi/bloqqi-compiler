static useconds_t useconds() {
  struct timeval v;
  gettimeofday(&v, NULL);
  return v.tv_sec*1000000 + v.tv_usec;
}

useconds_t my_sleep(
    useconds_t start,
    useconds_t end,
    useconds_t extra,
    useconds_t period) {
  useconds_t run_time = end - start;
  start = end;
  useconds_t sleep_time = period - run_time - extra;
  assert(sleep_time > 0);
  usleep(sleep_time);
  end = useconds();
  useconds_t time_slept = end - start;
  if (run_time + time_slept > period) {
    extra = run_time + time_slept - period;
  } else {
    extra = 0;
  }
  return extra;
}
