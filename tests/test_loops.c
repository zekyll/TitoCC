int* stackCheckVarLoc;

int main() {
	// Variable for testing stack integrity.
	int stackCheckVar = 987654321;
	stackCheckVarLoc = &stackCheckVar;

	// While: body not executed when test is false
	{
		int ok = 0;
		while(ok = 1, 0)
			ok = 0;
		out(ok);
	}

	// While: executed n times
	{
		int i = 3;
		while(i < 3) {
			++i;
		}
		out(i == 3);
	}

	// While: pointer as control expression
	{
		int a;
		int* p = &a;
		int i = 0;
		while(p) {
			p = 0;
			++i;
		}	
		out(i == 1);
	}

	// While: block variables initialized on every iteration
	{
		int i = 0;
		while(i++ < 3) {
			int a = 0;
			if (i==3)
				out(a == 0);
			++a;
		}
	}

	// While: break
	{
		int i = 0;
		int nc = 0;
		while(++nc,1) {
			while(1)
				break;
			if (++i == 3)
				break;
		}
		out (i == 3);
		out (nc == 3);
	}

	// While: continue
	{
		int i = 0;
		int x = 0;
		int nc = 0;
		while(++nc, i < 5) {
			int i2 = 0;
			while(i2++ < 2)
				continue;
			if (i++ < 2)
				continue;
			++x;
		}
		out(x == 3);
		out(nc == 6);
	}

	// While: scopes
	{
		int a = 765;
		while(out(a == 765), 1) {
			out(a == 765);
			int a = 654;
			out(a == 654);
			break;
		}
	}

	// Do-while: body executed once when test is false
	{	
		int x = 0;
		do
			x = 1;
		while (0);
		out(x);
	}

	// Do-while: body executed n times
	{
		int i = 0;
		do {
			++i;
		} while (i < 3);
		out(i == 3);
	}

	// Do-while: pointer as control expression
	{
		int a;
		int* p = &a;
		int i = 0;
		do {
			if (++i == 2)
				p = 0;
		} while(p);
		out(i == 2);
	}

	// Do-while: break
	{
		int i = 0;
		do {
			do break; while(1);
			if (i == 3) break;
		} while (++i);
		out(i == 3);
	}

	// Do-while: continue
	{
		int i = 0;
		int x = 0;
		do {
			if (i < 2) continue;
			++x;
		} while (++i < 5);
		out(i == 5);
		out(x == 3);
	}

	// Do-while: scopes
	{
		int a = 987;
		do {
			out(a == 987);
			int a = 876;
			out(a == 876);
		} while(out(a == 987), 0);
	}

	// For: body and increment expression not executed when test is false
	{
		int ok = 1;
		for(; 0; ok = 0)
			ok = 0;
		out(ok);
	}

	// For: body executed n times
	{
		int x = 0;
		for (int i = 0; i < 4; ++i) {
			++x;
		}
		out(x == 4);
	}

	// For: empty test expression
	{
		int i = 0;
		for(;;) {
			if (++i == 3)
				break;
		}
		out(i == 3);
	}

	// For: pointer as control expression
	{
		int a;
		int* p = &a;
		int i = 0;
		for(;p;) {
			p = 0;
			++i;
		}	
		out(i == 1);
	}

	// For: break
	{
		int i = 0;
		int nc = 0;
		int ni = 0;
		for (;++nc,1;++ni) {
			for(;;)
				break;
			if (++i == 3)
				break;
		}
		out(i == 3);
		out(nc == 3);
		out(ni == 2);
	}

	// For: continue
	{
		int i = 0;
		int x = 0;
		int nc = 0;
		int ni = 0;
		for (;++nc, i < 5;++ni) {
			int i2 = 0;
			for(;i2 < 2;++i2)
				continue;
			if (i++ < 2)
				continue;
			++x;
		}
		out(x == 3);
		out(nc == 6);
		out(ni == 5);
	}

	// For: scopes
	{
		int a = 321;
		for (int a = 432; ; out(a==432)) { //TODO
			out(a == 432);
			int a = 543;
			out(a == 543);
			break;
		}
	}

	// Stack integrity checks.
	out(stackCheckVar == 987654321);
	out(&stackCheckVar == stackCheckVarLoc);

	return 0;
}

