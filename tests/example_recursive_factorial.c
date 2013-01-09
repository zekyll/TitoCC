
int recFact(int n)
{
	if(n <= 0)
		return 1;
	else
		return n * recFact(n - 1);
}

int main()
{
	int stop = 0;
	while(!stop)  {
		int n = in();
		if(n != -1)
			out(recFact(n));
		else
			stop = 1;
	}
}