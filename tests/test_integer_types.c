
/*
Some unsigned operations require signed overflow in the
TTK-91 machine. Therefore they won't work without modifying
Titokone's source code to disable overflow exceptions.
*/
int main()
{
	unsigned m1 = 2147483647; // 2^31-1
	unsigned m2 = 2147483648; // 2^31
	unsigned mx = 4294967295; // 2^32-1
	unsigned a;
	unsigned b;

	// Unsigned int: division
	out(m1 / 1 == m1);
	out(m2 / 1 == m2);
	out(mx / 1 == mx);
	out(m1 / m1 == 1);
	out(m2 / m2 == 1);
	out(mx / mx == 1);
	out((m1 - 1) / m1 == 0);
	out((m2 - 1) / m2 == 0);
	out((mx - 1) / mx == 0);
	out(mx / m1 == 2);
	out(mx / m2 == 1);
	out(m2 / m1 == 1);
	out(m2 / 555 == 3869339);
	out(mx / 666 == 6448899);
	a = 3123123123; b = 3123123123; out(a / b == 1);
	a = 3123123122; b = 3123123123; out(a / b == 0);
	a = 7654321; b = 3111222333; out(a / b == 0);
	a = 1234567; b = 321; out(a / b == 3846);
	a = 2747601960; b = 12345; out(a / b == 222568);
	a = 2747601959; b = 12345; out(a / b == 222567);
	a = 0; b = 2747601959; out(a / b == 0);

	// Unsigned int: remainder
	out(m1 % 1 == 0);
	out(m2 % 1 == 0);
	out(mx % 1 == 0);
	out(m1 % m1 == 0);
	out(m2 % m2 == 0);
	out(mx % mx == 0);
	out((m1 - 1) % m1 == m1 - 1);
	out((m2 - 1) % m2 == m2 - 1);
	out((mx - 1) % mx == mx - 1);
	out(mx % m1 == 1);
	out(mx % m2 == m1);
	out(m2 % m1 == 1);
	out(mx % 7777 == 2390);
	out(m2 % 8888 == 640);
	a = 3123123123; b = 3123123123; out(a % b == 0);
	a = 3123123122; b = 3123123123; out(a % b == a);
	a = 3999888777; b = 3111222333; out(a % b == 888666444);
	a = 2345678; b = 432; out(a % b == 350);
	a = 3444554988; b = 987; out(a % b == 0);
	a = 3444554987; b = 987; out(a % b == 986);

	// Unsigned int: less than
	out(m1 < m2 == 1 && m2 < m1 == 0);
	out(0 < mx == 1 && mx < 0 == 0);
	a = 3444555666; b = 3444555666; out(a < b == 0 && b < a == 0);
	a = 2999888777; b = 2999888778; out(a < b == 1 && b < a == 0);
	a = 123; b = 2888777666; out(a < b == 1 && b < a == 0);

	// Unsigned int: greater than
	out(m1 > m2 == 0 && m2 > m1 == 1);
	out(0 > mx == 0 && mx > 0 == 1);
	a = 3444555666; b = 3444555666; out(a > b == 0 && b > a == 0);
	a = 2999888777; b = 2999888778; out(a > b == 0 && b > a == 1);
	a = 123; b = 2888777666; out(a > b == 0 && b > a == 1);

	// Unsigned int: less than or equal to
	out(m1 <= m2 == 1 && m2 <= m1 == 0);
	out(0 <= mx == 1 && mx <= 0 == 0);
	a = 3444555666; b = 3444555666; out(a <= b == 1 && b <= a == 1);
	a = 2999888777; b = 2999888778; out(a <= b == 1 && b <= a == 0);
	a = 123; b = 2888777666; out(a <= b == 1 && b <= a == 0);

	// Unsigned int: greater than or equal to
	out(m1 >= m2 == 0 && m2 >= m1 == 1);
	out(0 >= mx == 0 && mx >= 0 == 1);
	a = 3444555666; b = 3444555666; out(a >= b == 1 && b >= a == 1);
	a = 2999888777; b = 2999888778; out(a >= b == 0 && b >= a == 1);
	a = 123; b = 2888777666; out(a >= b == 0 && b >= a == 1);

	// Unsigned int: (logical) right shift
	a = 2333444555; out(a >> 3 == 291680569);

	// Unsigned int: other binary operators
	a = 3321321321; b = 2777888999;
	out((a | b) == 3858202607);
	out((a ^ b) == 1617194894);
	out((a & b) == 2241007713);
	out(a << 4 == 1601533584);
	out(a + b == 1804243024);
	out(a - b == 543432322);
	out(a * b == 2610550719);

	// Unsigned int: Unary operators
	out(+a == 3321321321);
	out(-a == 973645975);
	out(~a == 973645974);

	// Unsigned int: increment/decrement
	a = mx; out(++a == 0 && a == 0);
	a = m1; out(++a == m2 && a == m2);
	a = mx; out(a++ == mx && a == 0);
	a = m1; out(a++ == m1 && a == m2);
	a = 0; out(--a == mx && a == mx);
	a = m2; out(--a == m1 && a == m1);
	a = 0; out(a-- == 0 && a == mx);
	a = m2; out(a-- == m2 && a == m1);

	// Unsigned int: assignment
	b = 3555444333;
	a = 4222111000; out((a = b) == a && a == b);
	a = 4222111000; out((a += b) == 3482588037 && a == 3482588037);
	a = 4222111000; out((a *= b) == 3746652984 && a == 3746652984);
	a = 4222111000; out((a &= b) == 3551020040 && a == 3551020040);
	a = 4222111000; out((a |= b) == 4226535293 && a == 4226535293);
	a = 4222111000; out((a ^= b) == 675515253 && a == 675515253);
	a = 4222111000; out((a -= b) == 666666667 && a == 666666667);
	a = 4222111000; out((a /= 333) == 12679012 && a == 12679012);
	a = 4222111000; out((a %= b) == 666666667 && a == 666666667);
	a = 4222111000; out((a <<= 25) == 805306368 && a == 805306368);
	a = 4222111000; out((a >>= 17) == 32212 && a == 32212);

	return 0;
}
