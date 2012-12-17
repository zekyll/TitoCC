package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public abstract class WordToken extends Token
{
	protected WordToken(String string, int line, int column)
	{
		super(string, line, column);
	}

	public static WordToken parse(CodeReader reader) throws IOException
	{
		WordToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		char c = reader.read();

		if (AsciiUtil.isIdentifierStart(c)) {
			StringBuilder tokenString = new StringBuilder();
			do {
				tokenString.append((char) c);
				c = reader.read();
			} while (AsciiUtil.isIdentifierCharacter(c));

			if (KeywordToken.isKeyword(tokenString.toString()))
				token = new KeywordToken(tokenString.toString(), line, column);
			else
				token = new IdentifierToken(tokenString.toString(), line, column);
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
