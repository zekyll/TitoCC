
int main()
{
	int n1 = 0;
	int n2 = 1;
	while(1) {
		int next = n1 + n2;
		out(next);
		n1 = n2;
		n2 = next;
	}
}