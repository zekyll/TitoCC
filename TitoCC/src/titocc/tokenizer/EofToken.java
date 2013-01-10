package titocc.tokenizer;

import java.io.IOException;

/**
 * End of file token. Matches null character returned by the code reader.
 */
public class EofToken extends Token
{
	/**
	 * Constructs an EofToken.
	 *
	 * @param line line number where the token is located
	 * @param column column number where the token is located
	 */
	public EofToken(int line, int column)
	{
		super("", line, column);
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
		int line = reader.getLineNumber(), column = reader.getColumn();

		if (reader.read() == '\0')
			token = new EofToken(line, column);
		else
			reader.unread();

		return token;
	}

	@Override
	public String toString()
	{
		return "<End of file>";
	}
}
