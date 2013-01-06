package titocc.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class AsciiUtilTest
{
	private String specialCharacters = " !#¤%&/()=?-.,'\n\t:;*^<>§½";

	public AsciiUtilTest()
	{
	}

	@BeforeClass
	public static void setUpClass()
	{
	}

	@AfterClass
	public static void tearDownClass()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

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
