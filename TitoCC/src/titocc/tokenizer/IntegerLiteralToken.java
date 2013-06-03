package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;
import titocc.util.Position;

/**
 * Integer literals consist of sequence of digits 0-9, followed by an optional suffix. The suffix
 * follows the same rules as identifiers. Hexadecimal and and octal literals are not yet supported.
 * All integer literals are non-negative and "negative literals" are implemented in the
 * parser/compiler with unary minus operator.
 */
public class IntegerLiteralToken extends Token
{
	/**
	 * Raw string of the digits.
	 */
	private final String value;

	/**
	 * Suffix string.
	 */
	private final String suffix;

	/**
	 * Constructs an IntegerLiteralToken.
	 *
	 * @param string the whole token as a string
	 * @param position starting position of the token
	 * @param value value part of the token
	 * @param suffix suffix part of the token
	 */
	public IntegerLiteralToken(String string, Position position,
			String value, String suffix)
	{
		super(string, position);
		this.value = value;
		this.suffix = suffix;
	}

	/**
	 * Returns the value part (digits) of the token as a string.
	 *
	 * @return the value as a string
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Returns the suffix part of the token.
	 *
	 * @return the suffix
	 */
	public String getSuffix()
	{
		return suffix;
	}

	/**
	 * Attempts to parse an integer literal from input. If the characters don't match an integer
	 * literal then resets the stream to its original position and returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @return IntegerLiteralToken object or null if no valid integer literal was found
	 * @throws IOException if code reader throws
	 */
	public static IntegerLiteralToken parse(CodeReader reader) throws IOException
	{
		IntegerLiteralToken token = null;
		Position pos = reader.getPosition();

		StringBuilder digits = new StringBuilder();

		while (Character.isDigit(reader.peek()))
			digits.append(reader.read());

		String digitStr = digits.toString();
		if (digitStr.length() > 0) {
			StringBuilder suffix = new StringBuilder();
			while (AsciiUtil.isIdentifierCharacter(reader.peek()))
				suffix.append(reader.read());

			String tokenString = digitStr + suffix.toString();
			token = new IntegerLiteralToken(tokenString, pos, digitStr, suffix.toString());
		}

		return token;
	}
}
