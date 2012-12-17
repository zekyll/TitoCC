package titocc.tokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import titocc.util.AsciiUtil;

public class KeywordToken extends Token
{
	private static final String[] keywords = {
		// In use:
		"while",
		"if",
		"else",
		"return",
		"int",
		"void",
		// Reserved:
		"do",
		"for",
		"break",
		"continue",
		"goto",
		"const",
		"volatile",
		"unsigned",
		"signed",
		"char",
		"short",
		"long",
		"float",
		"double",
		"enum",
		"struct",
		"union",
		"switch",
		"case",
		"default",
		"static",
		"extern",
		"auto",
		"register",
		"sizeof",
		"typedef"
	};
	private static Set<String> keywordSet = new HashSet<String>(Arrays.asList(keywords));

	private KeywordToken(String string, int line, int column)
	{
		super(string, line, column);
	}

	public static KeywordToken parse(CodeReader reader) throws IOException
	{
		KeywordToken token = null;
		int line = reader.getLineNumber(), column = reader.getColumn();

		char c = reader.read();

		if (AsciiUtil.isIdentifierStart(c)) {
			StringBuilder tokenString = new StringBuilder();
			do {
				tokenString.append((char) c);
				c = reader.read();
			} while (AsciiUtil.isIdentifierCharacter(c));

			if (keywordSet.contains(tokenString.toString()))
				token = new KeywordToken(tokenString.toString(), line, column);
		}

		if (c != '\0')
			reader.unread();

		return token;
	}
}
