package titocc.compiler.types;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;

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

	@Override
	public void compileConversion(Assembler asm, Scope scope, Vstack vstack, CType targetType)
			throws IOException
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type || targetType instanceof PointerType) {
			// No-op.
		} else
			super.compileConversion(asm, scope, vstack, targetType);
	}
}
