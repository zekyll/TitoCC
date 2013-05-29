package titocc.tokenizer;

import java.io.IOException;
import titocc.util.Position;

/**
 * End of file token. Matches null character returned by the code reader.
 */
public class EofToken extends Token
{
	/**
	 * Constructs an EofToken.
	 *
	 * @param position starting position of the token
	 */
	public EofToken(Position position)
	{
		super("", position);
	}

	/**
	 * Attempts to parse end of file token from input. If the reader is not at
	 * the end of the text then resets the stream to its original position and
	 * returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return EofToken object or null if not at the end of file
	 * @throws IOException if code reader throws
	 */
	public static EofToken parse(CodeReader reader) throws IOException
	{
		EofToken token = null;
		Position pos = reader.getPosition();

		if (reader.peek() == '\0') {
			token = new EofToken(pos);
			reader.read();
		}

		return token;
	}

	@Override
	public String toString()
	{
		return "<End of file>";
	}
}
