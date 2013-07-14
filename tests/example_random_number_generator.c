
int index = 0;
int state[16];

void init()
{
	index = 0;
	state[0] = 1467061415;
	state[1] = -100106194;
	state[2] = -397246588;
	state[3] = -1357570927;
	state[4] = 64831833;
	state[5] = 1946534496;
	state[6] = 521381113;
	state[7] = 2098546553;
	state[8] = -601398375;
	state[9] = 177665932;
	state[10] = -488891440;
	state[11] = -1860808848;
	state[12] = -378851636;
	state[13] = -792864098;
	state[14] = 1922215501;
	state[15] = -1041110829;

}

int rand()
{
	int a;
	int b;
	int c;
	int d;
	a = state[index];
	c = state[(index + 13) & 15];
	b = a ^ c ^ (a << 16) ^ (c << 15);
	c = state[(index + 9) & 15];
	c ^= c >> 11;
	a = state[index] = b ^ c;
	d = a ^ ((a << 5) & -633066208);
	index = (index + 15) & 15;
	a = state[index];
	state[index] = a ^ b ^ d ^ (a << 2) ^ (b << 18) ^ (c << 28);
	return state[index];
}

int main()
{
	init();
	for (int i = 0; i < 50; ++i)
		out(rand());
}