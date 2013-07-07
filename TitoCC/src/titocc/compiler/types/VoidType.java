package titocc.compiler.types;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;

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
	public void compileConversion(Assembler asm, Scope scope, Vstack vstack, CType targetType)
			throws IOException
	{
		if (targetType.equals(CType.VOID))
			; // No-op.
		else
			super.compileConversion(asm, scope, vstack, targetType);
	}
}
