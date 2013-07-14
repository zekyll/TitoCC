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

	public static boolean isHexadecimalDigit(char c)
	{
		return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	public static boolean isOctalDigit(char c)
	{
		return c >= '0' && c <= '7';
	}
}
