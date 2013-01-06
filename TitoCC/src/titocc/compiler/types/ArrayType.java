package titocc.compiler.types;

public class ArrayType extends CType
{
	private CType elementType;
	private int length;

	public ArrayType(CType elementType, int size)
	{
		this.elementType = elementType;
	}

	public boolean isPointer()
	{
		return true;
	}

	public CType dereference()
	{
		return elementType;
	}

	public int getSize()
	{
		return elementType.getSize() * length;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PointerType))
			return false;

		return elementType.equals(((ArrayType) obj).elementType)
				&& length == (((ArrayType) obj).length);
	}
}
