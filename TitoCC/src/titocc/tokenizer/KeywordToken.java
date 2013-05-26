package titocc.tokenizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Word token that is found in the list of keywords.
 */
public class KeywordToken extends WordToken
{
	/**
	 * List of keywords defined in C99 standard ($6.4.1).
	 */
	private static final String[] keywords = {
		"auto",
		"break",
		"case",
		"char",
		"const",
		"continue",
		"default",
		"do",
		"double",
		"else",
		"enum",
		"extern",
		"float",
		"for",
		"goto",
		"if",
		"inline",
		"int",
		"long",
		"register",
		"restrict",
		"return",
		"short",
		"signed",
		"sizeof",
		"static",
		"struct",
		"switch",
		"typedef",
		"union",
		"unsigned",
		"void",
		"volatile",
		"while",
		"_Bool",
		"_Complex",
		"_Imaginary"
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
