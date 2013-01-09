package titocc.compiler.types;

/**
 * Corresponds to C array type. Is an object and a scalar, but not arithmetic.
 * Equals only to PointerType that has same pointed type.
 */
public class PointerType extends CType
{
	private CType pointedType;

	/**
	 * Constructs a PointerType.
	 *
	 * @param elementType type of the pointed object.
	 */
	public PointerType(CType pointedType)
	{
		this.pointedType = pointedType;
	}

	@Override
	public boolean isObject()
	{
		return true;
	}

	@Override
	public boolean isPointer()
	{
		return true;
	}

	@Override
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

	@Override
	public int getSize()
	{
		return 1;
	}
}
