package titocc.compiler.types;

public class IntType extends CType
{
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
