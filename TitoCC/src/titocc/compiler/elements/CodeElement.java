package titocc.compiler.elements;

import titocc.util.Position;

/**
 * Abstract base for all the code elements. Stores the position of the element
 * within the source file so that it can be used by compiler messages.
 */
public abstract class CodeElement
{
	/**
	 * Starting position.
	 */
	private final Position position;

	/**
	 * Constructs a code element.
	 *
	 * @param position starting position of the element
	 */
	public CodeElement(Position position)
	{
		this.position = position;
	}

	/**
	 * Returns the starting position of the code element in the source file.
	 *
	 * @return position
	 */
	public Position getPosition()
	{
		return position;
	}

	/**
	 * Returns a string representation of the code element for testing and
	 * debugging purposes.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString()
	{
		return "";
	}
}
