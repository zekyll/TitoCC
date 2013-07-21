package titocc.tokenizer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import titocc.util.AsciiUtil;
import titocc.util.Position;

/**
 * Base class for string and character literal tokens.
 */
abstract class CharStrLiteralToken extends Token
{
	/**
	 * List of 32-bit character values specified by the token.
	 */
	private final List<Integer> values;

	/**
	 * Constructs a CharStrLiteralToken.
	 *
	 * @param string the whole token as a string
	 * @param position starting position of the token
	 * @param values character values
	 */
	protected CharStrLiteralToken(String string, Position position, List<Integer> values)
	{
		super(string, position);
		this.values = values;
	}

	/**
	 * Get the character values.
	 *
	 * @return
	 */
	public List<Integer> getValues()
	{
		return values;
	}

	/**
	 * Attempts to parse a character or string literal from input. The only difference is the quote
	 * character used (' or "). If the characters don't match a string/character literal then resets
	 * the stream to its original position and returns null.
	 *
	 * @param reader code reader from which charactes are read
	 * @param quote " or ' depending on toke type
	 * @param tokenStr used for returning the full token string
	 * @return list of character values or null if no character/string literal was found
	 * @throws IOException if code reader throws
	 * @throws SyntaxException if the literal contains errors
	 */
	protected static List<Integer> parse(CodeReader reader, char quote, StringBuilder tokenStr)
			throws IOException, SyntaxException
	{
		List<Integer> values = null;

		if (reader.peek() == quote) {
			Position tknPos = reader.getPosition();
			reader.read();
			values = new ArrayList<Integer>();

			// Parse characters until closing quote (' or") or an error is encountered.
			for (;;) {
				Position chrPos = reader.getPosition();
				char c = reader.read();
				tokenStr.append(c);

				if (c == quote) {
					if (values.isEmpty())
						throw new SyntaxException("Empty character or string literal.", tknPos);
					else
						break;
				} else if (c == '\\') {
					int value = parseEscapeSequence(reader, tokenStr, chrPos);
					values.add(value);
				} else if (c == '\n') {
					throw new SyntaxException("New-line in character or string literal.", chrPos);
				} else if (c == '\0') {
					throw new SyntaxException("Unterminated character or string literal.", tknPos);
				} else {
					values.add((int) c);
				}
			}
		}

		return values.isEmpty() ? null : values;
	}

	private static int parseEscapeSequence(CodeReader reader, StringBuilder tokenStr, Position pos)
			throws IOException, SyntaxException
	{
		Integer value = parseSimpleEscapeSequence(reader, tokenStr, pos);

		if (value == null)
			value = parseOctalEscapeSequence(reader, tokenStr, pos);

		if (value == null)
			value = parseHexadecimalEscapeSequence(reader, tokenStr, pos);

		if (value == null)
			value = parseUniversalCharacterName(reader, tokenStr, pos);

		if (value == null)
			throw new SyntaxException("Illegal escape sequence.", pos);

		return value;
	}

	private static Integer parseSimpleEscapeSequence(CodeReader reader, StringBuilder tokenStr,
			Position pos) throws IOException
	{
		Integer ret = null;
		switch (reader.peek()) {
			case '\'':
				ret = (int) '\'';
				break;
			case '\"':
				ret = (int) '\"';
				break;
			case '?':
				ret = (int) '?';
				break;
			case '\\':
				ret = (int) '\\';
				break;
			case 'a':
				ret = 0x07;
				break;
			case 'b':
				ret = (int) '\b';
				break;
			case 'f':
				ret = (int) '\f';
				break;
			case 'n':
				ret = (int) '\n';
				break;
			case 'r':
				ret = (int) '\r';
				break;
			case 't':
				ret = (int) '\t';
				break;
			case 'v':
				ret = 0x0b;
				break;
		}

		if (ret != null)
			tokenStr.append(reader.read());

		return ret;
	}

	private static Integer parseOctalEscapeSequence(CodeReader reader, StringBuilder tokenStr,
			Position pos) throws IOException, SyntaxException
	{
		if (reader.peek() != '0')
			return null;

		StringBuilder digits = new StringBuilder();
		char c = reader.read();
		tokenStr.append(c);
		digits.append(c);

		while (AsciiUtil.isOctalDigit(reader.peek())) {
			c = reader.read();
			digits.append(c);
			tokenStr.append(c);
		}

		BigInteger value = new BigInteger(digits.toString(), 8);
		if (value.compareTo(BigInteger.valueOf(0xffffffffL)) > 0)
			throw new SyntaxException("Value of octal escape sequence is too large.", pos);

		return value.intValue();
	}

	private static Integer parseHexadecimalEscapeSequence(CodeReader reader, StringBuilder tokenStr,
			Position pos) throws IOException, SyntaxException
	{
		if (reader.peek() != 'x')
			return null;

		tokenStr.append(reader.read());

		StringBuilder digits = new StringBuilder();
		while (AsciiUtil.isHexadecimalDigit(reader.peek())) {
			char c = reader.read();
			digits.append(c);
			tokenStr.append(c);
		}

		if (digits.length() == 0)
			throw new SyntaxException("No digits in hexadecimal escape sequence.", pos);

		BigInteger value = new BigInteger(digits.toString(), 16);
		if (value.compareTo(BigInteger.valueOf(0xffffffffL)) > 0)
			throw new SyntaxException("Value of hexadecimal escape sequence is too large.", pos);

		return value.intValue();
	}

	/**
	 * Universal character consisting of exactly 4 hexadecimal digits ($6.4.3). Because our
	 * internal encoding is UTF-32, the code point value can be used as it is without encoding.
	 */
	private static Integer parseUniversalCharacterName(CodeReader reader, StringBuilder tokenStr,
			Position pos) throws IOException, SyntaxException
	{
		char c = reader.peek();
		if (c != 'u' && c != 'U')
			return null;

		tokenStr.append(reader.read());

		StringBuilder digits = new StringBuilder();
		for (int i = 0; i < 4; ++i) {
			c = reader.peek();
			if (!AsciiUtil.isHexadecimalDigit(c))
				throw new SyntaxException("Incomplete universal character name.", pos);
			reader.read();
			digits.append(c);
			tokenStr.append(c);
		}

		// Don't allow values below 0xa0 (except $, @ and `), or values in range 0xd800-0xdfff.
		// ($6.4.3/2)
		int value = Integer.valueOf(digits.toString(), 16);
		if (value < 0x00a0 && value != 0x24 && value != 0x40 && value != 0x60
				|| (value >= 0xd800 && value <= 0xdfff))
			throw new SyntaxException("Illegal value for universal character.", pos);

		return value;
	}
}
