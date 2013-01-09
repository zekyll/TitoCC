
int index = 0;
int state[16];

void init()
{
	index = 0;
	state[0] = 1467061415;
	state[1] = 4194861102;
	state[2] = 3897720708;
	state[3] = 2937396369;
	state[4] = 64831833;
	state[5] = 1946534496;
	state[6] = 521381113;
	state[7] = 2098546553;
	state[8] = 3693568921;
	state[9] = 177665932;
	state[10] = 3806075856;
	state[11] = 2434158448;
	state[12] = 3916115660;
	state[13] = 3502103198;
	state[14] = 1922215501;
	state[15] = 3253856467;
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
	d = a ^ ((a << 5) & 3661901088);
	index = (index + 15) & 15;
	a = state[index];
	state[index] = a ^ b ^ d ^ (a << 2) ^ (b << 18) ^ (c << 28);
	return state[index];
}

int main()
{
	init();
	while(1)
		out(rand());
}