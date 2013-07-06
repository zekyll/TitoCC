package titocc.compiler.types;

/**
 * A dummy target type used when converting control expressions, denoting a value that is 0 if the
 * original value is considered false and non-zero otherwise. Only scalar types can be converted to
 * boolish and all scalar types must implement the conversion.
 */
public class BoolishType extends CType
{
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BoolishType;
	}
}
