package titocc.compiler;

/**
 * Exception class for internal compiler errors. Should hopefully never happen.
 */
public class InternalCompilerException extends RuntimeException
{
	private String message;

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
