package titocc.compiler.types;

public class PointerType extends CType
{
	private CType pointedType;

	public PointerType(CType pointedType)
	{
		this.pointedType = pointedType;
	}

	public boolean isPointer()
	{
		return true;
	}

	public CType dereference()
	{
		return pointedType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof PointerType))
			return false;

		return pointedType.equals(((PointerType) obj).pointedType);
	}
}
