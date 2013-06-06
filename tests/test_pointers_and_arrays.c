
int y;

int globalArray[3][4];

int* fpCheckVarLoc;

int foo(int *a, int (*b)[2])
{
	int c[2][2];
	c[0][0] = -10;
	c[0][1] = -5;
	c[1][0] = 5;
	c[1][1] = 10;
	int d = *a + (*b)[0] + (*b)[1] + c[0][0] + c[0][1] + c[1][0] + c[1][1]; 
	return d;
}

int main()
{
	int fpCheckVar;
	fpCheckVarLoc = &fpCheckVar;

	int x;
	out(&x == &*&x);
	*&x = 13;
	out(x == 13);
	out(*&x == 13);

	out(&y == &*&y);
	*&y = 17;
	out(y == 17);
	out(*&y == 17);

	x = 7;
	out((*&x += 7) == 14 && x == 14);

	x = 6;
	out((*&x -= 3) == 3 && x == 3);

	x = 10;
	out(--*&x == 9 && x == 9);
	out(++*&x == 10 && x == 10);
	out((*&x)-- == 10 && x == 9);
	out((*&x)++ == 9 && x == 10);

	int ar[4];
	ar[3] = 21;
	out(ar[3] == 21);
	out(*(ar + 3) == 21);
	
	int ar2[2][3];
	out(&ar2[1][2] == &ar2[0][0] + 5);
	ar2[1][2] = 33;
	out(ar2[1][2] == 33);
	out(*(&ar2[0][0] + 5) == 33);
	out(*&ar2[1][2] == 33);

	out(&globalArray[1][2] == &globalArray[0][0] + 6);
	globalArray[1][2] = 42;
	out(globalArray[1][2] == 42);
	out(*(&globalArray[0][0] + 6) == 42);
	out(*&globalArray[1][2] == 42);

	int ar3[2];
	ar3[0] = 2;
	ar3[1] = 3;
	x = 1;
	out(foo(&x, &ar3) == 6);

	int ar4[4][3];
	out(ar4[3] - ar4[2] == 3);
	out(&ar4[3] - &ar4[2] == 1);

	out(ar4[1] + 6 == ar4[3]);
	out(&ar4[1] + 2 == &ar4[3]);

	out(6 + ar4[1] == ar4[3]);
	out(2 + &ar4[1] == &ar4[3]);

	int (*pnull)[3] = 0;
	out(pnull == 0);
	out(!(pnull != 0));
	out(!pnull);

	out(ar4 != 0);
	out(!(ar4 == 0));
	out(!!ar4);

	if(ar4);
	if(pnull);
	if(0) while(ar4);
	while(pnull);

	int ar5[4];
	out(&ar5[2] == &ar5[2] && !(&ar5[1] == &ar5[2]) && !(&ar5[1] == &ar5[2]));
	out(!(&ar5[2] != &ar5[2]) && &ar5[1] != &ar5[2] && &ar5[2] != &ar5[1]);
	out(&ar5[1] < &ar5[2] && !(&ar5[2] < &ar5[2]));
	out(&ar5[1] <= &ar5[1] && !(&ar5[2] <= &ar5[1]));
	out(&ar5[2] > &ar5[1] && !(&ar5[2] > &ar5[2]));
	out(&ar5[1] >= &ar5[1] && !(&ar5[1] >= &ar5[2]));
	
	void* p1 = &ar[4];
	void* p2 = &ar[5];
	out(p2 == p2 && !(p1 == p2) && !(p1 == p2));
	out(!(p2 != p2) && p1 != p2 && p2 != p1);
	out(p1 < p2 && !(p2 < p2));
	out(p1 <= p1 && !(p2 <= p1));
	out(p2 > p1 && !(p2 > p2));
	out(p1 >= p1 && !(p1 >= p2));

	p1 = &ar4[1];
	p2 = &ar4[2];
	int (*p3)[3] = p1;
	out(++p3 == p2 && p3 == p2);
	out(--p3 == p1 && p3 == p1);
	out(p3++ == p1 && p3 == p2);
	out(p3-- == p2 && p3 == p1);
	out((p3 += 1) == p2 && p3 == p2);
	out((p3 -= 1) == p1 && p3 == p1);

	out(&fpCheckVar == fpCheckVarLoc);

}

