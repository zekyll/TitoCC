package titocc.compiler.types;

import titocc.compiler.ExpressionAssembler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;

/**
 * Corresponds to C void type. Is not an object, scalar, arithmetic or integer type. Equals only to
 * VoidType.
 */
public class VoidType extends CType
{
	@Override
	public boolean isIncomplete()
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof VoidType;
	}

	@Override
	public Rvalue compileConversion(ExpressionAssembler asm, Scope scope, Rvalue value,
			CType targetType)
	{
		if (targetType.equals(CType.VOID))
			return value; // No-op.
		else
			return super.compileConversion(asm, scope, value, targetType);
	}
}
