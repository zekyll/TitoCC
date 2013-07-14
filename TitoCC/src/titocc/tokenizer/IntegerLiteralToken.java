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
	 * Raw string of the digits (not including the leading "0x" for hecadecimal).
	 */
	private final String value;

	/**
	 * Suffix string.
	 */
	private final String suffix;

	/**
	 * Base/radix of the number.
	 */
	private final int base;

	/**
	 * Constructs an IntegerLiteralToken.
	 *
	 * @param string the whole token as a string
	 * @param position starting position of the token
	 * @param value value part of the token
	 * @param suffix suffix part of the token
	 * @param base base/radix (8/10/16)
	 */
	public IntegerLiteralToken(String string, Position position,
			String value, String suffix, int base)
	{
		super(string, position);
		this.value = value;
		this.suffix = suffix;
		this.base = base;
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
	 * Returns the base of the integer literal (8, 10 or 16).
	 *
	 * @return base
	 */
	public int getBase()
	{
		return base;
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

		String prefix = "";
		StringBuilder digits = new StringBuilder();
		int base = 0;

		if (reader.peek() == '0') {
			reader.read();
			Character c = reader.peek();
			if ((c == 'x' || c == 'X') && AsciiUtil.isHexadecimalDigit(reader.peek2nd())) {
				reader.read();
				prefix = "0" + c;
				base = 16;
			} else {
				digits.append('0');
				base = 8;
			}
		} else if (Character.isDigit(reader.peek()))
			base = 10;

		parseDigits(reader, digits, base);

		String digitStr = digits.toString();
		if (digits.length() > 0) {
			String suffix = parseSuffix(reader);
			String tokenStr = prefix + digitStr + suffix;
			token = new IntegerLiteralToken(tokenStr, pos, digitStr, suffix, base);
		}

		return token;
	}

	private static void parseDigits(CodeReader reader, StringBuilder digits, int base)
			throws IOException
	{
		if (base == 10) {
			while (Character.isDigit(reader.peek()))
				digits.append(reader.read());
		} else if (base == 16) {
			while (AsciiUtil.isHexadecimalDigit(reader.peek()))
				digits.append(reader.read());
		} else { // if (base == 8)
			while (AsciiUtil.isOctalDigit(reader.peek()))
				digits.append(reader.read());
		}
	}

	private static String parseSuffix(CodeReader reader)
			throws IOException
	{
		StringBuilder suffix = new StringBuilder();
		if (AsciiUtil.isIdentifierStart(reader.peek())) {
			suffix.append(reader.read());
			while (AsciiUtil.isIdentifierCharacter(reader.peek()))
				suffix.append(reader.read());
		}
		return suffix.toString();
	}
}
