int* stackCheckVarLoc;

int gok;
int gok2;

int x0;
int x1 = -2147483648;
int x2 = 2147483647;

void voidFunc(int a, int b)
{
	gok = a - b;
}

void voidFunc2()
{
	int a = 2;
	if(a != a)
		;
	else {
		gok = 1;
		return;
		gok = 0;
	}
}

int intFunc(int a, int b, int c)
{
	return a + b + c;
}

int recursiveFactorial(int n)
{
	if(n <= 1)
		return 1;
	else
		return n * recursiveFactorial(n - 1);
}

// Using variable name in initializer (undefined behavior but must compile).
void scope_start_test1(int scope_start_test1)
{
	int foobar = foobar;
}

// Using function name as local variable name
void scope_start_test2()
{
	int scope_start_test2 = scope_start_test2;
}


int main()
{
	// Variable for testing stack integrity.
	int stackCheckVar = 987654321;
	stackCheckVarLoc = &stackCheckVar;

	int a;
	int b;
	int c;
	int d;
	int e;
	int f;
	int g;
	int h;
	int i;
	int ok;
	int ok2;

	// Null statement
	;

	// Empty compound statement
	{}

	/*/ 
		Comment block test /* /* /*
	//*/

	// Accessing global variables
	out(x1 == -2147483648);
	out(x2 == 2147483647);

	// Globals default initialized to 0
	out(x0 == 0);

	// Hiding global names, int initializers, negative literals, case sensitivity
	int x1 = -2147483648;
	int x2 = 2147483647;
	int X1 = 1001;
	out(x1 == -2147483648);
	out(x2 == 2147483647);
	out(X1 == 1001);

	// Long variable names
	int long_variable_name1 = 2001;
	int long_variable_name2 = 2002;
	out(long_variable_name1 == 2001);
	out(long_variable_name2 == 2002);

	// Using TTK-91 reserved words as identifiers
	ok = 1;
	int R0 = 100; ok |= R0 == 100;
	int R1 = 101; ok |= R1 == 101;
	int R2 = 102; ok |= R2 == 102;
	int R3 = 103; ok |= R3 == 103;
	int R4 = 104; ok |= R4 == 104;
	int R5 = 105; ok |= R5 == 105;
	int R6 = 106; ok |= R6 == 106;
	int crt = 107; ok |= crt == 107;
	int kbd = 108; ok |= kbd == 108;
	int stdin = 109; ok |= stdin == 109;
	int stdout = 110; ok |= stdout == 110;
	int halt = 111; ok |= halt == 111;
	int read = 112; ok |= read == 112;
	int write = 113; ok |= write == 113;
	int time = 114; ok |= time == 114;
	int date = 115; ok |= date == 115;
	out(ok);

	// Multiple init-declarators
	{
		int x;
		int *a = &x, b = 9, c[12], d = b;
		out(a == &x && b == 9 && d == 9);
	}

	// Void function call
	gok = 0;
	voidFunc(99, 98);
	out(gok);

	// Function call returning int
	out(intFunc(13, 14, 15) == 42);

	// Recursive function call
	out(recursiveFactorial(3) == 6);

	// Calling function with return statement in middle of function
	gok = 0;
	voidFunc2();
	out(gok);

	// If
	ok = 0;
	a = 3001;
	if(a == 3001)
		ok = 1;
	out(ok);

	// If-else: control expression is true
	ok = 0;
	ok2 = 1;
	a = 4001;
	if(a == 4001)
		ok = 1;
	else
		ok2 = 0;
	out(ok);
	out(ok2);

	// If-else: control expression is false
	ok = 1;
	ok2 = 0;
	a = 5001;
	if(a == 5002) {
		ok = 0;
	} else {
		ok2 = 1;
	}
	out(ok);
	out(ok2);

	// Add
	a = 7001; b = 7002; out(a + b == 14003);
	a = -2147483648; b = 2147483647; out(a + b == -1);

	// Subtract
	a = 8013; b = 8001; out(a - b == 12);
	a = 2147483647; b = 2147483647; out(a - b == 0);
	a = 8002; b = -8003; out(a - b == 16005);

	// Multiplication
	a = 9001; b = -9002; out(a * b == -81027002);

	// Division
	a = -10001; b = -17; out(a / b == 588);
	a = 10002; b = -13; out(a / b == -769);

	// Remainder
	a = 17; b = 7; out(a % b == 3);
	a = -19; b = 5; out(a % b == -4);
	a = 13; b = -3; out(a % b == 1);

	// Left shift
	a = 9; b = 3; out(a << b == 72);
	a = 2147483647; b = 1; out(a << b == -2);
	a = -1; b = 31; out(a << b == -2147483648);

	// Right shift
	a = 72; b = 3; out(a >> b == 9);
	a = 255; b = 1; out(a >> b == 127);

	// Arithmetic right shift
	a = -1; b = 31; out(a >> b == -1);
	a = -12345678; b = 7; out(a >> b == -96451);

	// Less than
	a = -7; b = -7; out(a < b == 0);
	a = -8; b = -7; out(a < b == 1);
	a = 14; b = 13; out(a < b == 0);
	a = -2147483648; b = 2147483647; out(a < b == 1);

	// Greater than
	a = -7; b = -7; out(b > a == 0);
	a = -8; b = -7; out(b > a == 1);
	a = 14; b = 13; out(b > a == 0);
	a = -2147483648; b = 2147483647; out(b > a == 1);

	// Less than or equal to
	a = -7; b = -7; out(a <= b == 1);
	a = -8; b = -7; out(a <= b == 1);
	a = 14; b = 13; out(a <= b == 0);
	a = -2147483648; b = 2147483647; out(a <= b == 1);

	// Greater than or equal to
	a = -7; b = -7; out(b >= a == 1);
	a = -8; b = -7; out(b >= a == 1);
	a = 14; b = 13; out(b >= a == 0);
	a = -2147483648; b = 2147483647; out(b >= a == 1);

	// Inequality
	a = -7; b = -7; out((a != b) == 0);
	a = -8; b = -7; out((a != b) == 1);
	a = 14; b = 13; out((a != b) == 1);
	a = -2147483648; b = 2147483647; out((a == b) != 1);

	// Equality
	a = -7; b = -7; out((a == b) == 1);
	a = -8; b = -7; out((a == b) == 0);
	a = 14; b = 13; out((a == b) == 0);
	a = -2147483648; b = 2147483647; out((a == b) == 0);

	// Bitwise and
	a = 1234567890; b = 987654321; out((a & b) == 144048272);
	a = -1234567890; b = -987654321; out((a & b) == -2078173938);

	// Bitwise or
	a = 1234567890; b = 987654321; out((a | b) == 2078173939);
	a = -1234567890; b = -987654321; out((a | b) == -144048273);

	// Bitwise xor
	a = 1234567890; b = 987654321; out((a ^ b) == 1934125667);
	a = -1234567890; b = -987654321; out((a ^ b) == 1934125665);

	// Logical and
	a = 6; b = -20; out((a && b) == 1);
	a = -7; b = 0; out((a && b) == 0);
	a = 0; b = 8; out((a && b) == 0);
	a = 0; b = 0; out((a && b) == 0);

	// Logical or
	a = 6; b = -20; out((a || b) == 1);
	a = -7; b = 0; out((a || b) == 1);
	a = 0; b = 8; out((a || b) == 1);
	a = 0; b = 0; out((a || b) == 0);

	// Logical operator short circuit
	ok = 1; b = 0 && (ok = 0); b = 1 || (ok = 0); out(ok);

	// Unary plus
	a = -2147483648; out(+a == -2147483648);
	a = 2147483647; out(+a == 2147483647);

	// Unary minus
	a = 0; out(-a == 0);
	a = -2147483647; out(-a == 2147483647);
	a = 2147483647; out(-a == -2147483647);

	// Logical negation
	a = 0; out(!a == 1);
	a = -666; out(!a == 0);
	a = 2147483647; out(!a == 0);

	// Bitwise negation
	a = 0; out(~a == -1);
	a = -1; out(~a == 0);
	a = -1234567890; out(~a == 1234567889);

	// Prefix increment
	a = -2147483648; out(++a == -2147483647); out(a == -2147483647);
	a = 2147483646; out(++a == 2147483647); out(a == 2147483647);

	// Prefix decrement
	a = -2147483647; out(--a == -2147483648); out(a == -2147483648);
	a = 2147483647; out(--a == 2147483646); out(a == 2147483646);

	// Postfix increment
	a = -2147483648; out(a++ == -2147483648); out(a == -2147483647);
	a = 2147483646; out(a++ == 2147483646); out(a == 2147483647);

	// Posfix decrement
	a = -2147483647; out(a-- == -2147483647); out(a == -2147483648);
	a = 2147483647; out(a-- == 2147483647); out(a == 2147483646);

	// Simple assignment
	a = 0; b = 13; out((a = b) == 13 && a == 13);
	a = 666; b = -2147483648; out((a = b) == -2147483648 && a == -2147483648);

	// Add assignment
	a = 7001; b = 7002; out((a += b) == 14003 && a == 14003);
	a = -2147483648; b = 2147483647; out((a += b) == -1 && a == -1);

	// Subtract assignment
	a = 8013; b = 8001; out((a -= b) == 12 && a == 12);
	a = 2147483647; b = 2147483647; out((a -= b) == 0 && a == 0);
	a = 8002; b = -8003; out((a -= b) == 16005 && a == 16005);

	// Multiply assignment
	a = 9001; b = -9002; out((a *= b) == -81027002 && a == -81027002);

	// Bitwise and assignment
	a = 1234567890; b = 987654321; out((a &= b) == 144048272 && a == 144048272);
	a = -1234567890; b = -987654321; out((a &= b) == -2078173938 && a == -2078173938);

	// Bitwise or assignment
	a = 1234567890; b = 987654321; out((a |= b) == 2078173939 && a == 2078173939);
	a = -1234567890; b = -987654321; out((a |= b) == -144048273 && a == -144048273);

	// Bitwise xor assignment
	a = 1234567890; b = 987654321; out((a ^= b) == 1934125667 && a == 1934125667);
	a = -1234567890; b = -987654321; out((a ^= b) == 1934125665 && a == 1934125665);

	// Division assignment
	a = -10001; b = -17; out((a /= b) == 588 && a == 588);
	a = 10002; b = -13; out((a /= b) == -769 && a == -769);

	// Remainder assignment
	a = 17; b = 7; out((a %= b) == 3 && a == 3);
	a = -19; b = 5; out((a %= b) == -4 && a == -4);
	a = 13; b = -3; out((a %= b) == 1 && a == 1);

	// Left shift assignment
	a = 9; b = 3; out((a <<= b) == 72 && a == 72);
	a = 2147483647; b = 1; out((a <<= b) == -2 && a ==-2);
	a = -1; b = 31; out((a <<= b) == -2147483648 && a == -2147483648);

	// Right shift assignment
	a = 72; b = 3; out((a >>= b) == 9 && a == 9);
	a = 255; b = 1; out((a >>= b) == 127 && a == 127);

	// Arithmetic right shift assignment
	a = -1; b = 31; out((a >>= b) == -1 && a == -1);
	a = -12345678; b = 7; out((a >>= b) == -96451 && a == -96451);

	// Comma operator
	a = 22;
	b = (c = 33, c *= 2, a);
	out(b == 22);
	out(c == 66);

	// Complex expressions and register allocation
	a = 1; b = 2; c = 3; d = 4; e = 5; f = 6; g = 7; h = 8;
	i = a + b + c + d + e + f + g + h;
	out(i == 36);
	i = (a + (b + (c + (d + (e + (f + (g + h)))))));
	out(i == 36);
	i = (a + (b + (c + (d + (e + (f + (g + (h += 100))))))));
	out(i == 136);
	out(h == 108);

	// Stack integrity checks.
	out(stackCheckVar == 987654321);
	out(&stackCheckVar == stackCheckVarLoc);
}
