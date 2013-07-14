
/*
Some unsigned operations require signed overflow in the
TTK-91 machine. Therefore they won't work without modifying
Titokone's source code to disable overflow exceptions.
*/
int main()
{
	unsigned m1 = 2147483647; // 2^31-1
	unsigned m2 = 2147483648u; // 2^31
	unsigned mx = 4294967295U; // 2^32-1
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
	a = 3123123123u; b = 3123123123u; out(a / b == 1);
	a = 3123123122u; b = 3123123123u; out(a / b == 0);
	a = 7654321; b = 3111222333u; out(a / b == 0);
	a = 1234567; b = 321; out(a / b == 3846);
	a = 2747601960u; b = 12345; out(a / b == 222568);
	a = 2747601959u; b = 12345; out(a / b == 222567);
	a = 0; b = 2747601959u; out(a / b == 0);

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
	a = 3123123123u; b = 3123123123u; out(a % b == 0);
	a = 3123123122u; b = 3123123123u; out(a % b == a);
	a = 3999888777u; b = 3111222333u; out(a % b == 888666444);
	a = 2345678; b = 432; out(a % b == 350);
	a = 3444554988u; b = 987; out(a % b == 0);
	a = 3444554987u; b = 987; out(a % b == 986);

	// Unsigned int: less than
	out(m1 < m2 == 1 && m2 < m1 == 0);
	out(0 < mx == 1 && mx < 0 == 0);
	a = 3444555666u; b = 3444555666u; out(a < b == 0 && b < a == 0);
	a = 2999888777u; b = 2999888778u; out(a < b == 1 && b < a == 0);
	a = 123; b = 2888777666u; out(a < b == 1 && b < a == 0);

	// Unsigned int: greater than
	out(m1 > m2 == 0 && m2 > m1 == 1);
	out(0 > mx == 0 && mx > 0 == 1);
	a = 3444555666u; b = 3444555666u; out(a > b == 0 && b > a == 0);
	a = 2999888777u; b = 2999888778u; out(a > b == 0 && b > a == 1);
	a = 123; b = 2888777666u; out(a > b == 0 && b > a == 1);

	// Unsigned int: less than or equal to
	out(m1 <= m2 == 1 && m2 <= m1 == 0);
	out(0 <= mx == 1 && mx <= 0 == 0);
	a = 3444555666u; b = 3444555666u; out(a <= b == 1 && b <= a == 1);
	a = 2999888777u; b = 2999888778u; out(a <= b == 1 && b <= a == 0);
	a = 123; b = 2888777666u; out(a <= b == 1 && b <= a == 0);

	// Unsigned int: greater than or equal to
	out(m1 >= m2 == 0 && m2 >= m1 == 1);
	out(0 >= mx == 0 && mx >= 0 == 1);
	a = 3444555666u; b = 3444555666u; out(a >= b == 1 && b >= a == 1);
	a = 2999888777u; b = 2999888778u; out(a >= b == 0 && b >= a == 1);
	a = 123; b = 2888777666u; out(a >= b == 0 && b >= a == 1);

	// Unsigned int: (logical) right shift
	a = 2333444555u; out(a >> 3 == 291680569u);

	// Unsigned int: other binary operators
	a = 3321321321u; b = 2777888999u;
	out((a | b) == 3858202607u);
	out((a ^ b) == 1617194894u);
	out((a & b) == 2241007713u);
	out(a << 4 == 1601533584u);
	out(a + b == 1804243024u);
	out(a - b == 543432322u);
	out(a * b == 2610550719u);

	// Unsigned int: Unary operators
	out(+a == 3321321321u);
	out(-a == 973645975u);
	out(~a == 973645974u);

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
	b = 3555444333u;
	a = 4222111000u; out((a = b) == a && a == b);
	a = 4222111000u; out((a += b) == 3482588037u && a == 3482588037u);
	a = 4222111000u; out((a *= b) == 3746652984u && a == 3746652984u);
	a = 4222111000u; out((a &= b) == 3551020040u && a == 3551020040u);
	a = 4222111000u; out((a |= b) == 4226535293u && a == 4226535293u);
	a = 4222111000u; out((a ^= b) == 675515253u && a == 675515253u);
	a = 4222111000u; out((a -= b) == 666666667u && a == 666666667u);
	a = 4222111000u; out((a /= 333) == 12679012u && a == 12679012u);
	a = 4222111000u; out((a %= b) == 666666667u && a == 666666667u);
	a = 4222111000u; out((a <<= 25) == 805306368u && a == 805306368u);
	a = 4222111000u; out((a >>= 17) == 32212u && a == 32212u);

	return 0;
}
