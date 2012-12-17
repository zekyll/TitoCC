package titocc.tokenizer;

import java.io.IOException;

public class EofToken extends Token
{
	private EofToken(String string, int line, int column)
	{
		super(string, line, column);
	}

	public static EofToken parse(CodeReader reader) throws IOException
	{
		EofToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		if (reader.read() == '\0')
			token = new EofToken("", line, column);
		else
			reader.unread();

		return token;
	}
}
