package titocc.compiler.types;

public class ArrayType extends CType
{
	private CType elementType;
	private int length;

	public ArrayType(CType elementType, int length)
	{
		this.elementType = elementType;
		this.length = length;
	}

	@Override
	public boolean isObject()
	{
		return true;
	}

	@Override
	public CType dereference()
	{
		return elementType;
	}

	@Override
	public int getSize()
	{
		return elementType.getSize() * length;
	}

	@Override
	public CType decay()
	{
		return new PointerType(elementType);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ArrayType))
			return false;

		return elementType.equals(((ArrayType) obj).elementType)
				&& length == (((ArrayType) obj).length);
	}
}
