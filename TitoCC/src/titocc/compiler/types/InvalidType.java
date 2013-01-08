package titocc.compiler.types;

/**
 * A dummy type that is not an object, function or incomplete type. Basically it
 * doesn't support any operations and doesn't equal to any type (even itself).
 */
public class InvalidType extends CType
{
	@Override
	public boolean equals(Object obj)
	{
		return false;
	}
}
