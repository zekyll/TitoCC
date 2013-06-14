package titocc.compiler.types;

/**
 * 64-bit signed integer type. Implemented on TTK-91 using two 32-bit machine bytes. The only type
 * using this representation is long long int.
 */
class Int64Type extends IntegerType
{
	/**
	 * Constructs an Int64Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Int64Type(int rank, int tag)
	{
		super(rank, true, tag);
	}

	@Override
	public int getSize()
	{
		return 2;
	}

	@Override
	public CType toUnsigned()
	{
		return new Uint64Type(getRank(), tag);
	}
}
