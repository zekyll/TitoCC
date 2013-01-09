package titocc.compiler.types;

/**
 * Corresponds to C void type. Is not an object, scalar, arithmetic or integer
 * type. Equals only to VoidType.
 */
public class VoidType extends CType
{
	/**
	 * Constructs a VoidType.
	 */
	public boolean isObject()
	{
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof VoidType;
	}
}
