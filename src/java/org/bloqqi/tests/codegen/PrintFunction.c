#include <stdio.h>
#include <stdbool.h>

void Print(int in) {
	printf("%d\n", in);
}

void PrintBool(bool b) {
	printf("%s\n", b ? "true" : "false");
}

void PrintStrInt(const char *str, int n) {
	printf("%s%d\n", str, n);
}

void PrintStrIntInt(const char *str, int n1, int n2) {
	printf("%s(%d, %d)\n", str, n1, n2);
}

void PrintIntInt(int n1, int n2) {
	printf("%d, %d\n", n1, n2);
}


void PrintStrBool(const char *str, bool b) {
	printf("%s%s\n", str, b ? "true" : "false");
}
