package titocc.compiler.types;

import titocc.compiler.Assembler;
import titocc.compiler.Vstack;

/**
 * 32-bit signed integer type. Implemented on TTK-91 using one 32-bit machine byte. The standard
 * integer types implemented using this representation are char, signed char, short int, int and
 * long int.
 */
class Int32Type extends IntegerType
{
	/**
	 * Constructs an Int32Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Int32Type(int rank, int tag)
	{
		super(rank, true, tag);
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public CType toUnsigned()
	{
		return new Uint32Type(getRank(), tag);
	}

	@Override
	public void compileConversion(Assembler asm, Vstack vstack, CType targetType)
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type) {
			// No-op.
		} else
			super.compileConversion(asm, vstack, targetType);
	}
}
