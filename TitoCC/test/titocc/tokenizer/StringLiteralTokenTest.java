package titocc.tokenizer;

import java.io.IOException;

public class StringLiteralTokenTest extends CharacterLiteralTokenTest
{
	protected CharStrLiteralToken parse(CodeReader cr) throws IOException, SyntaxException
	{
		return StringLiteralToken.parse(cr);
	}

	protected void test(String code, char next, int... values) throws IOException, SyntaxException
	{
		// Replace opening and closing single quotes with double quotes.
		code = code.replaceAll("^'", "\"");
		code = code.replaceAll("'([^']*)$", "\"$1");
		super.test(code, next, values);
	}
}
