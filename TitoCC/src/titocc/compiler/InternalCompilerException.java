package titocc.compiler;

/**
 * Exception class for internal compiler errors. Should hopefully never happen and the purpose is
 * just to help detect bugs.
 */
public class InternalCompilerException extends RuntimeException
{
	/**
	 * Constructor.
	 *
	 * @param message error message for the exception
	 */
	public InternalCompilerException(String message)
	{
		super(message);
	}
}
