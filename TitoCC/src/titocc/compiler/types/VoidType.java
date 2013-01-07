package titocc.compiler.types;

public class VoidType extends CType
{
	public boolean isObject()
	{
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof IntType;
	}

	@Override
	public int getSize()
	{
		// Could return 1 here to allow void pointer arithmetic, but it's not
		// standard compliant.
		return 0;
	}
}
