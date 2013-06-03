package titocc.compiler.types;

/**
 * Corresponds to C int type. Is an object, scalar, arithmetic and integer type, but not arithmetic.
 * Equals only to IntType.
 */
public class IntType extends CType
{
	/**
	 * Constructs an IntType.
	 */
	public IntType()
	{
	}

	@Override
	public boolean isObject()
	{
		return true;
	}

	@Override
	public boolean isArithmetic()
	{
		return true;
	}

	@Override
	public boolean isInteger()
	{
		return true;
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	public int getIncrementSize()
	{
		return 1;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof IntType;
	}
}
