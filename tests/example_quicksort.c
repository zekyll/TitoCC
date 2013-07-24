void quickSort(int *a, int n)
{
    if (n < 2)
        return;
    int pivot = a[n / 2];
    int *left = a;
    int *right = a + n - 1;
    while (left <= right) {
        if (*left < pivot) {
            ++left;
            continue;
        }
        if (*right > pivot) {
            --right;
            continue;
        }
        int tmp = *left;
        *left++ = *right;
        *right-- = tmp;
    }
    quickSort(a, right - a + 1);
    quickSort(left, a + n - left);
}

int main () {
	int a[10];
	a[0] = 4;
	a[1] = 65;
	a[2] = 123;
	a[3] = 26;
	a[4] = -4322;
	a[5] = 123;
	a[6] = 6786;
	a[7] = 5656;
	a[8] = -7745464;
	a[9] = 34;
	int n = 10;
	quickSort(a, n);

	for (int i = 0; i < n; ++i)
		out(a[i]);

	return 0;
}

