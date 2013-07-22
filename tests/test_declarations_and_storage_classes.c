
// Multiple function declarations, no definition, no usage.
int f1(int);
int f1(int a);
int f1(int b);

// Multiple function declarations, with definition and usage.
int f2(int);
int f2(int a) { return 2 * a; }
int f2(int b);

// Using function that is only declared.
int f3(int a);

// Declaration of function inside itself.
int f5(int a) {
	int f5(int);
	if (a == 0) return 0; else return a + f5(a - 1);
}

// Object and function in same declaration.
int a, f(int);

// Tentative definition of simple variable.
int b;
int b, b, b;

// Multiple declarations and a definition for simple variable.
int c;
int c = 12;
int c;

// Tentative definition for an array.
int d[20];
int d[20], d[20];

// Using forward declared variable.
int e;

// Forward declaration of main.
int main();

int main()
{
	// ...
	out(f2(11) == 22);

	// ...
	out(f3(12) == 36);

	// Block-scope function declaration.
	int f4(int);
	out(f4(13) == 52);

	// Function declaration in inner scope, hiding object with same name.
	{
		char f4;
		{
			int f4(int);
			out(f4(14) == 56);
		}
	}

	// ...
	out(f5(3) == 6);

	// ...
	out(b == 0);

	// ...
	out(c == 12);

	// ...
	out(d[0] == 0);
	out(d[19] == 0);

	// ...
	out(e == 16);

	// Using declared intrinsic functions.
	__udiv(1,2);

	return 0;
}

int f3(int b) { return 3 * b; }

int f4(int c) { return 4 * c; }

int e = 16;