package titocc.util;

/**
 * Binary index tree.
 */
public class BinaryIndexedTree
{
	private int[] array;

	/**
	 * Constructs a binary index tree with given size.
	 *
	 * @param size
	 */
	public BinaryIndexedTree(int size)
	{
		array = new int[size + 1];
	}

	/**
	 * Increment the value in given index.
	 *
	 * @param idx index
	 * @param val increment amount
	 */
	public void update(int idx, int val)
	{
		while (idx < array.length) {
			array[idx] += val;
			idx += idx & -idx;
		}
	}

	/**
	 * Sum range from 1 to idx.
	 *
	 * @param idx range end
	 * @return sum of elements
	 */
	public int get(int idx)
	{
		int sum = 0;
		while (idx > 0) {
			sum += array[idx];
			idx -= idx & -idx;
		}
		return sum;
	}
}
