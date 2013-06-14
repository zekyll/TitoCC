package titocc.compiler.types;

/**
 * 32-bit unsigned integer type. Implemented on TTK-91 using one 32-bit machine byte. The standard
 * integer types implemented using this representation are unsigned char, unsigned short int,
 * unsigned int and unsigned long int.
 */
class Uint32Type extends IntegerType
{
	/**
	 * Constructs a Uint32Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Uint32Type(int rank, int tag)
	{
		super(rank, false, tag);
	}

	@Override
	public int getSize()
	{
		return 1;
	}
}