package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public class IntegerLiteralToken extends Token
{
	private int value;

	private IntegerLiteralToken(String string, int line, int column)
	{
		super(string, line, column);
		value = Integer.parseInt(string);
	}

	public int getValue()
	{
		return value;
	}

	public static IntegerLiteralToken parse(CodeReader reader) throws IOException
	{
		IntegerLiteralToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		StringBuilder tokenString = new StringBuilder();

		char c = reader.read();
		if (c == '-') {
			tokenString.append(c);
			c = reader.read();
		}

		while (Character.isDigit(c)) {
			tokenString.append(c);
			c = reader.read();
		}

		if (tokenString.length() > 0 && (tokenString.charAt(0) != '-'
				|| tokenString.length() > 2) && !AsciiUtil.isAsciiAlphabet(c))
			token = new IntegerLiteralToken(tokenString.toString(), line, column);

		if (c != '\0')
			reader.unread();

		return token;
	}
}
