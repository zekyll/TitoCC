package titocc.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class AsciiUtilTest
{
	private String specialCharacters = " !#¤%&/()=?-.,'\n\t:;*^<>§½";

	@Test
	public void isAsciiAlphabetWorks()
	{
		assertTrue(AsciiUtil.isAsciiAlphabet('a'));
		assertTrue(AsciiUtil.isAsciiAlphabet('z'));
		assertTrue(AsciiUtil.isAsciiAlphabet('A'));
		assertTrue(AsciiUtil.isAsciiAlphabet('Z'));
		assertFalse(AsciiUtil.isAsciiAlphabet('2'));
		assertFalse(AsciiUtil.isAsciiAlphabet('ä'));
		assertFalse(AsciiUtil.isAsciiAlphabet('_'));
		for (int i = 0; i < specialCharacters.length(); ++i)
			assertFalse(AsciiUtil.isAsciiAlphabet(specialCharacters.charAt(i)));
	}

	@Test
	public void isIdentifierStartWorks()
	{
		assertTrue(AsciiUtil.isIdentifierStart('a'));
		assertTrue(AsciiUtil.isIdentifierStart('z'));
		assertTrue(AsciiUtil.isIdentifierStart('A'));
		assertTrue(AsciiUtil.isIdentifierStart('Z'));
		assertTrue(AsciiUtil.isIdentifierStart('_'));
		assertFalse(AsciiUtil.isIdentifierStart('2'));
		for (int i = 0; i < specialCharacters.length(); ++i)
			assertFalse(AsciiUtil.isIdentifierStart(specialCharacters.charAt(i)));
	}

	@Test
	public void isIdentifierCharWorks()
	{
		assertTrue(AsciiUtil.isIdentifierCharacter('a'));
		assertTrue(AsciiUtil.isIdentifierCharacter('z'));
		assertTrue(AsciiUtil.isIdentifierCharacter('A'));
		assertTrue(AsciiUtil.isIdentifierCharacter('Z'));
		assertTrue(AsciiUtil.isIdentifierCharacter('_'));
		assertTrue(AsciiUtil.isIdentifierCharacter('2'));
		assertFalse(AsciiUtil.isIdentifierCharacter('ä'));
		for (int i = 0; i < specialCharacters.length(); ++i)
			assertFalse(AsciiUtil.isIdentifierCharacter(specialCharacters.charAt(i)));
	}
}
