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
		return obj instanceof VoidType;
	}
}
