package titocc.tokenizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Word token that is found in the list of keywords.
 */
public class KeywordToken extends WordToken
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

	/**
	 * Constructs a KeywordToken.
	 *
	 * @param string keyword string
	 * @param line line number where the token is located
	 * @param column column number where the token is located
	 */	
	public KeywordToken(String string, int line, int column)
	{
		super(string, line, column);
	}

	/**
	 * Checks whether the string is a keyword.
	 *
	 * @param s string to check
	 * @return true if keyword
	 */
	public static boolean isKeyword(String s)
	{
		return keywordSet.contains(s);
	}
}
