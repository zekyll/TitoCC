package titocc.compiler.types;

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
}
