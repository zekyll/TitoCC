package titocc.compiler.types;

import java.util.List;

public class FunctionType extends CType
{
	private CType returnType;
	private List<CType> parameterTypes;

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
