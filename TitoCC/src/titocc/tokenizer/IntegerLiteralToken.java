package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public class IntegerLiteralToken extends Token
{
	private String value, suffix;

	private IntegerLiteralToken(String string, int line, int column,
			String value, String suffix)
	{
		super(string, line, column);
		this.value = value;
		this.suffix = suffix;
	}

	public String getValue()
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
		while (Character.isDigit(c)) {
			digits.append(c);
			c = reader.read();
		}

		String digitStr = digits.toString();
		if (digitStr.length() > 0) {
			StringBuilder suffix = new StringBuilder();
			while (AsciiUtil.isIdentifierCharacter(c)) {
				suffix.append(c);
				c = reader.read();
			}

			String tokenString = digitStr + suffix.toString();
			token = new IntegerLiteralToken(tokenString,
					line, column, digitStr, suffix.toString());
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
