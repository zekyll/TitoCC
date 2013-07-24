
// Multiple function declarations, no definition, no usage.
int f1(int);
int f1(int a);
int f1(int b);
void TEST1() { }

// Multiple function declarations, with definition and usage.
int f2(int);
int f2(int a) { return 2 * a; }
int f2(int b);
void TEST2() { out(f2(11) == 22); }

// Using function that is only declared.
int f3(int a);
void TEST3() { out(f3(12) == 36); }
int f3(int a) { return 3 * a; }

// Declaration of function inside itself.
int f4(int a) {
	int f4(int);
	if (a == 0) return 0; else return a + f4(a - 1);
}
void TEST4() { out(f4(3) == 6); }

// Extern block-scope function declaration.
void TEST5()
{
	extern int f5(int);
	out(f5(5) == 25);
}
int f5(int c) { return 5 * c; }

// Object and function in same declaration.
int a, f6(int);
void TEST6() { }

// Tentative definition of simple variable.
int x7;
int x7, x7, x7;
void TEST7() { out(x7 == 0); }

// Function declaration in inner scope, hiding object with same name.
void TEST8() {
	char f8;
	{
		int f8(int);
		out(f8(8) == 64);
	}
}
int f8(int a) { return 8 * a; }

// Multiple declarations and a definition for simple variable.
int x9;
int x9 = 12;
int x9;
void TEST9() { out(x9 == 12); }

// Tentative definition for an array.
int x10[20];
int x10[20], x10[20];
void TEST10() { out(x10[0] == 0); out(x10[19] == 0); }

// Using forward declared variable.
int x11;
void TEST11() { out(x11 == 16);}
int x11 = 16;

// Forward declaration of main.
int main();
void TEST12() { }

// Implicit declaration & definition of intrinsic functions.
void TEST13() { out(__udiv(15,4) == 3); }

// Static block-scope variable.
void f14(int a)
{
	static int x = 0;
	out(++x == a);
}
void TEST14() { f14(1); f14(2); }

// Static block-scope objects initialized to 0.
void TEST15()
{
	static int a;
	static int b[3];
	out(a == 0);
	out(b[0] == 0 && b[2] == 0);
}

// Extern with initializer is a definition.
extern int x16 = 16;
void TEST16() { out(x16 == 16); }

// Tentative definition of static object.
static int x17;
static int x17;
void TEST17() { out(x17 == 0); }

// Register storage class.
void TEST18()
{
	register int x = 0;
	x = 18;
	out(x == 18);
}

// Auto storage class.
void TEST19()
{
	auto int x = 0;
	x = 19;
	out(x == 19);
}

// Extern object declaration after static.
static int x20 = 20;
extern int x20;
void TEST20() { out(x20 == 20); }

// Extern function declaration after static.
static int f21(int a) { return 21 * a; }
extern int f21(int);
void TEST21() { out(f21(1) == 21); }

// Normal function declaration after static.
static int f22(int a);
int f22(int a) { return 22 * a; }
void TEST22() { out(f22(1) == 22); }

// Extern block-scope object declaration.
void TEST23() { extern int x23; out(x23 == 23); }
int x23 = 23;

// Extern block-scope array declaration.
void TEST24() { extern int x24[3]; out(x24[0] == 0 && x24[2] == 0); }
int x24[3];

// Extern block-scope object declaration, hiding identifier with no linkage.
void TEST25() { int x25 = 0; { out(x25 == 0); extern int x25; out(x25 == 25); } }
int x25 = 25;

// Declarations in different scopes refer to same object.
void f26a() { extern int x26; out(x26 == 26); x26 = -26; }
void f26b() { extern int x26; out(x26 == -26); }
void TEST26() { f26a(); f26b(); }
int x26 = 26;

// Function definition with extern.
int f27(int a) { return 27 * a; }
void TEST27() { out(f27(1) == 27); }

// Function definition with static.
int f28(int a) { return 28 * a; }
void TEST28() { out(f28(1) == 28); }

// Static object in block scope should have no linkage.
extern int x29;
int f29() { static int x29 = 29; { extern int x29; out(x29 == -29); } out(x29 == 29); }
void TEST29() { f29(); extern int x29; out(x29 == -29); }
extern int x29 = -29;

// Register storage class in parameter.
int f30(register int a) { a = 30 * a; return a; }
void TEST30() { out(f30(1) == 30); }

void TEST31() { }
void TEST32() { }
void TEST33() { }
void TEST34() { }
void TEST35() { }
void TEST36() { }
void TEST37() { }
void TEST38() { }
void TEST39() { }

/*
void TESTX0() { }
void TESTX1() { }
void TESTX2() { }
void TESTX3() { }
void TESTX4() { }
void TESTX5() { }
void TESTX6() { }
void TESTX7() { }
void TESTX8() { }
void TESTX9() { }
*/

int main()
{
	TEST1();
	TEST2();
	TEST2();
	TEST3();
	TEST4();
	TEST5();
	TEST6();
	TEST7();
	TEST8();
	TEST9();

	TEST10();
	TEST11();
	TEST12();
	TEST12();
	TEST13();
	TEST14();
	TEST15();
	TEST16();
	TEST17();
	TEST18();
	TEST19();

	TEST20();
	TEST21();
	TEST22();
	TEST22();
	TEST23();
	TEST24();
	TEST25();
	TEST26();
	TEST27();
	TEST28();
	TEST29();

	TEST30();
	TEST31();
	TEST32();
	TEST32();
	TEST33();
	TEST34();
	TEST35();
	TEST36();
	TEST37();
	TEST38();
	TEST39();

	return 0;
}

