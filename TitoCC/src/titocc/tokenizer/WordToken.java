package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;
import titocc.util.Position;

/**
 * Common base class for identifiers and keywords. Allowed characters in word
 * tokens are letters a-z/A-Z, underscores and digits 0-9. The first character
 * must not be a digit. If the word token is not a keyword then it is considered
 * an identifier.
 */
public abstract class WordToken extends Token
{
	/**
	 * Constructs a WordToken.
	 *
	 * @param string word string
	 * @param position starting position of the token
	 */
	protected WordToken(String string, Position position)
	{
		super(string, position);
	}

	/**
	 * Attempts to parse a word token from input. If the characters don't
	 * match a valid word token then resets the stream to its original position
	 * and returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return WordToken object or null if no valid word was found
	 * @throws IOException if code reader throws
	 */	
	public static WordToken parse(CodeReader reader) throws IOException
	{
		WordToken token = null;
		Position pos = reader.getPosition();

		char c = reader.read();

		if (AsciiUtil.isIdentifierStart(c)) {
			StringBuilder tokenString = new StringBuilder();
			do {
				tokenString.append((char) c);
				c = reader.read();
			} while (AsciiUtil.isIdentifierCharacter(c));

			if (KeywordToken.isKeyword(tokenString.toString()))
				token = new KeywordToken(tokenString.toString(), pos);
			else
				token = new IdentifierToken(tokenString.toString(), pos);
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
