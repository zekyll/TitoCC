int notprime[10001];
int primes[10000];
int primeCount = 0;

void generatePrimes(int n)
{
	for (int i = 2; i * i <= n;) {
		for (int j = 2 * i; j <= n; j += i)
			notprime[j] = 1;
		do {
			++i;
		} while (notprime[i]);
	}

	for (int i = 2; i <= n; ++i) {
		if (!notprime[i])
			primes[primeCount++] = i;
	}
}

int main()
{
	for (;;) {
		int n = in();
		if (n < 0 || n > 10000)
			break;
		generatePrimes(n);
		for (int i = 0; i < primeCount; ++i)
			out(primes[i]);
	}
}