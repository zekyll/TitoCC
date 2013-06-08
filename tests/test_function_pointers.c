int* stackCheckVarLoc;

int f1(int a)
{
	return 2 * a;
}

int (*f2(int a))(int)
{
	return 0;
}

int (*f3(int a))(int)
{
	return 0;
}

int (*f4(int (*p)(int)))(int)
{
	return p;
}

// Adjustment of function parameter types
int f5(int a[2], int b(int))
{
	int* a2 = a;
	int** a3 = &a;
	int (*b2)(int) = b;
	int (**b3)(int) = &b;

	return (a[0] == -a[1]) && ((*b)(3) == 6);
}

// Named and unnamed parameters in function prototypes
void (*p1)(int);
void (*p2)(int a, int);
int (*(*p3)(int,void*))(int* a,int);

// Using function name as parameter name
void g1(int g) { }

// Using same name in nested declarators
void (*g2(int g2))(int g2) { }

// Named and unnamed and parameters in return value declarator
void (*g3(int g))(int,void* a,int(*)(int)) { }

// Array of function pointers
int (*pa[2])(int);

int main() {
	// Variable for testing stack integrity.
	int stackCheckVar = 987654321;
	stackCheckVarLoc = &stackCheckVar;

	// Functions decay to function pointers
	{
		int r = (*f1)(19);
		out(r == 38);
		r = (&f1)(27);
		out(r == 54);
		r = (&**&**&f1)(31);
		out(r == 62);
	}

	// Calling through a function pointer
	{
		int (*p)(int) = &f1;
		int r = p(6);
		out(r == 12);
		r = (*p)(8);
		out(r == 16);
	}

	// Assigning function pointers
	{
		int (*p)(int);
		p = &f1;
		int r = (*p)(7);
		out(r == 14);
		p = f1;
		r = (*p)(8);
		out(r == 16);
		p = 0;
		out(p == 0);
	}

	// Null pointer evaluates to false
	{
		int (*p)(int) = 0;
		int ok = 1;
		if (p)
			ok = 0;
		out(ok);
	}

	// Non-null pointer evaluates to true
	{
		int (*p)(int) = &f1;
		int ok = 0;
		if (p)
			ok = 1;
		out(ok);
	}

	// Comparing function pointers
	{
		int (*(*p1)(int))(int) = &f2;
		int (*(*p2)(int))(int) = &f3;
		int (*(*p3)(int))(int) = 0;
		out((p1 == p1) == 1);
		out((p1 == p2) == 0);
		out((p1 == p3) == 0);
		out((p1 != p1) == 0);
		out((p1 != p2) == 1);
		out((p1 != p3) == 1);
	}

	// Function pointer as parameter and return value
	{
		int (*p)(int);
		p = f4(&f1);
		out((*p)(9) == 18);
	}

	// File scope function pointers initialized to null pointer
	{
		out(p1 == 0);
		out(pa[0] == 0);
		out(pa[1] == 0);
	}

	// Parameter type adjustment
	{
		int a[2];
		a[0] = -15;
		a[1] = 15;
		int (*b)(int) = &f1;
		int r = f5(a, b);
		out(r);
	}

	// Stack integrity checks.
	out(stackCheckVar == 987654321);
	out(&stackCheckVar == stackCheckVarLoc);

	return 0;
}