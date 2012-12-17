package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public class IdentifierToken extends Token
{
	private IdentifierToken(String string, int line, int column)
	{
		super(string, line, column);
	}

	public static IdentifierToken parse(CodeReader reader) throws IOException
	{
		IdentifierToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		char c = reader.read();

		if (AsciiUtil.isIdentifierStart(c)) {
			StringBuilder tokenString = new StringBuilder();
			do {
				tokenString.append((char) c);
				c = reader.read();
			} while (AsciiUtil.isIdentifierCharacter(c));

			token = new IdentifierToken(tokenString.toString(), line, column);
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
