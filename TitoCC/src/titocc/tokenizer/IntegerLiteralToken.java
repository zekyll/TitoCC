package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public class IntegerLiteralToken extends Token
{
	private int value;
	private String suffix;

	private IntegerLiteralToken(String string, int line, int column,
			int value, String suffix)
	{
		super(string, line, column);
		this.value = value;
		this.suffix = suffix;
	}

	public int getValue()
	{
		return value;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public static IntegerLiteralToken parse(CodeReader reader) throws IOException
	{
		IntegerLiteralToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		StringBuilder digits = new StringBuilder();


		char c = reader.read();
		if (c == '-') {
			digits.append(c);
			c = reader.read();
		}

		while (Character.isDigit(c)) {
			digits.append(c);
			c = reader.read();
		}

		String digitStr = digits.toString();
		if (digitStr.length() > 0 && (digitStr.charAt(0) != '-' || digitStr.length() > 2)) {
			StringBuilder suffix = new StringBuilder();
			while (AsciiUtil.isIdentifierCharacter(c)) {
				suffix.append(c);
				c = reader.read();
			}

			int value = Integer.parseInt(digitStr);
			String tokenString = digitStr + suffix.toString();
			token = new IntegerLiteralToken(tokenString,
					line, column, value, suffix.toString());
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
