package titocc.tokenizer;

import java.io.IOException;
import java.util.List;
import titocc.util.Position;

/**
 * Character literal token. E.g. 'a' or 'ab\nc'.
 */
public class CharacterLiteralToken extends CharStrLiteralToken
{
	/**
	 * Constructs an CharacterLiteralToken.
	 *
	 * @param string the whole token as a string
	 * @param position starting position of the token
	 * @param values character values
	 */
	public CharacterLiteralToken(String string, Position position, List<Integer> values)
	{
		super(string, position, values);
	}

	/**
	 * Attempts to parse an character literal from input. If the characters don't match an character
	 * literal then resets the stream to its original position and returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return CharacterLiteralToken object or null if no character literal was found
	 * @throws IOException if code reader throws
	 * @throws SyntaxException if the character literal contains errors
	 */
	public static CharacterLiteralToken parse(CodeReader reader)
			throws IOException, SyntaxException
	{
		CharacterLiteralToken token = null;

		if (reader.peek() == '\'') {
			Position pos = reader.getPosition();
			StringBuilder tokenStr = new StringBuilder();
			List<Integer> values = CharStrLiteralToken.parse(reader, '\'', tokenStr);
			if (values != null)
				token = new CharacterLiteralToken(tokenStr.toString(), pos, values);
		}

		return token;
	}
}
