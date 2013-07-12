package titocc.compiler.types;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;

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
	public Rvalue compileConversion(IntermediateCompiler ic, Scope scope, Rvalue value,
			CType targetType)
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type || targetType instanceof PointerType) {
			return value; // No-op.
		} else
			return super.compileConversion(ic, scope, value, targetType);
	}

	@Override
	public Rvalue compileIncDecOperator(IntermediateCompiler ic, Scope scope, Lvalue operand,
			boolean inc, boolean postfix, int incSize)
	{
		incSize *= getIncrementSize();
		return INTPTR_T.compileIncDecOperator(ic, scope, operand, inc, postfix, incSize);
	}
}
