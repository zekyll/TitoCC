package titocc.compiler;

/**
 * Exception class for internal compiler errors. Should hopefully never
 * happen and the purpose is just to help detect bugs.
 */
public class InternalCompilerException extends RuntimeException
{
	private String message;

	/**
	 * Constructor.
	 * @param message error message for the exception
	 */
	public InternalCompilerException(String message)
	{
		this.message = message;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
