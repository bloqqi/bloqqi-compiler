int main() {
  Main_VARS vars = {};
  for (int i = 0; i < ITERATIONS; i++) {
    bloqqi_main(&vars);
  }
  return 0;
}
