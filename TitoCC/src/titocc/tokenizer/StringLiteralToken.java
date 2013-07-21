package titocc.tokenizer;

import java.io.IOException;
import java.util.List;
import titocc.util.Position;

/**
 * String literal token. E.g. "abc" or "ab\nc\u0123".
 */
public class StringLiteralToken extends CharStrLiteralToken
{
	/**
	 * Constructs a StringLiteralToken.
	 *
	 * @param string the whole token as a string
	 * @param position starting position of the token
	 * @param values character values
	 */
	public StringLiteralToken(String string, Position position, List<Integer> values)
	{
		super(string, position, values);
	}

	/**
	 * Attempts to parse a string literal from input. If the characters don't match a string
	 * literal then resets the stream to its original position and returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return StringLiteralToken object or null if no string literal was found
	 * @throws IOException if code reader throws
	 * @throws SyntaxException if the string literal contains errors
	 */
	public static StringLiteralToken parse(CodeReader reader) throws IOException, SyntaxException
	{
		StringLiteralToken token = null;

		if (reader.peek() == '\"') {
			Position pos = reader.getPosition();
			StringBuilder tokenStr = new StringBuilder();
			List<Integer> values = CharStrLiteralToken.parse(reader, '\"', tokenStr);
			if (values != null)
				token = new StringLiteralToken(tokenStr.toString(), pos, values);
		}

		return token;
	}
}
