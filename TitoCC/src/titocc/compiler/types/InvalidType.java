package titocc.compiler.types;

/**
 * A dummy type that is not an object, function or incomplete type. Basically it doesn't support any
 * operations and doesn't equal to any type (even itself). The purpose is just so the return value
 * of dereference() can be used without having to do a null check first.
 */
public class InvalidType extends CType
{
	@Override
	public boolean equals(Object obj)
	{
		return false;
	}
}
