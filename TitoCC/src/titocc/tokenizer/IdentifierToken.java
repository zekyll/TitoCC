package titocc.tokenizer;

import java.io.IOException;
import titocc.util.AsciiUtil;

public class IdentifierToken extends WordToken
{
	public IdentifierToken(String string, int line, int column)
	{
		super(string, line, column);
	}
}
