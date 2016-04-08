int main() {
  Main_STATE state = {};
  for (int i = 0; i < ITERATIONS; i++) {
    Main(&state);
  }
  return 0;
}
