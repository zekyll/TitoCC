package titocc.tokenizer;

import titocc.util.Position;

/**
 * An exception caused by errors in source code.
 */
public class SyntaxException extends Exception
{
	/**
	 * Position within source file where the compilation error occured.
	 */
	private final Position position;

	/**
	 * Constructs a SyntaxException.
	 *
	 * @param message error message
	 * @param position position where the error occured
	 */
	public SyntaxException(String message, Position position)
	{
		super(message);
		this.position = position;
	}

	/**
	 * Returns the position of the error.
	 *
	 * @return position
	 */
	public Position getPosition()
	{
		return position;
	}
}
