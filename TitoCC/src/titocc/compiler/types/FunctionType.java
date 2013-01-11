package titocc.compiler.types;

import java.util.List;

/**
 * Corresponds to C function types. Is not an object, scalar, arithmetic or
 * integer type. Equals only to FunctionType with the same return type and
 * parameter types.
 */
public class FunctionType extends CType
{
	/**
	 * Function return type.
	 */	
	private final CType returnType;
	/**
	 * List of parameter types.
	 */
	private final List<CType> parameterTypes;

	/**
	 * Constructs a new FunctionType.
	 *
	 * @param returnType return type
	 * @param parameterTypes types of all the parameters
	 */
	public FunctionType(CType returnType, List<CType> parameterTypes)
	{
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FunctionType))
			return false;

		FunctionType func = (FunctionType) obj;

		return parameterTypes.equals(func.parameterTypes)
				&& returnType.equals(func.returnType);
	}

	@Override
	public int getSize()
	{
		return 0;
	}
}
