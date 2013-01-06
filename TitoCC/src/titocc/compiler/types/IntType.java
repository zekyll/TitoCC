package titocc.compiler.types;

public class IntType extends CType
{
	@Override
	public boolean isArithmetic()
	{
		return true;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof IntType;
	}
}
