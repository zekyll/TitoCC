package titocc.util;

public class AsciiUtil
{
	public static boolean isAsciiAlphabet(char c)
	{
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	public static boolean isIdentifierStart(char c)
	{
		return isAsciiAlphabet(c) || c == '_';
	}

	public static boolean isIdentifierCharacter(char c)
	{
		return isAsciiAlphabet(c) || c == '_' || Character.isDigit(c);
	}
}
