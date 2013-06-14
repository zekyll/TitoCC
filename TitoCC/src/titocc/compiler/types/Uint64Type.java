package titocc.compiler.types;

/**
 * 64-bit unsigned integer type. Implemented on TTK-91 using two 32-bit machine bytes. The only type
 * using this representation is unsigned long long int.
 */
class Uint64Type extends IntegerType
{
	/**
	 * Constructs a Uint64Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Uint64Type(int rank, int tag)
	{
		super(rank, false, tag);
	}

	@Override
	public int getSize()
	{
		return 2;
	}
}
