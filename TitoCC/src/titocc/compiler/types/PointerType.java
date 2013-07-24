package titocc.compiler.types;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;

/**
 * Corresponds to C pointer type. Is an object and a scalar, but not arithmetic. Equals only to
 * PointerType that has same pointed type.
 */
public class PointerType extends CType
{
	/**
	 * Type that this pointer type points to.
	 */
	private final CType pointedType;

	/**
	 * Constructs a PointerType.
	 *
	 * @param pointedType type of the pointed object.
	 */
	public PointerType(CType pointedType)
	{
		this.pointedType = pointedType;
	}

	@Override
	public boolean isObject()
	{
		return true;
	}

	@Override
	public boolean isPointer()
	{
		return true;
	}

	@Override
	public CType dereference()
	{
		return pointedType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PointerType))
			return false;

		return pointedType.equals(((PointerType) obj).pointedType);
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
