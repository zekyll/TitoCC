package titocc.compiler.types;

public abstract class CType
{
	public boolean isObject()
	{
		return true;
	}

	public boolean isPointer()
	{
		return false;
	}

	public CType dereference()
	{
		return null;
	}

	public boolean isArithmetic()
	{
		return false;
	}

	public int getSize()
	{
		return 1;
	}
}
