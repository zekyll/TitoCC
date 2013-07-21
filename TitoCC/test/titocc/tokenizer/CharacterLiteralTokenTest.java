package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;
import titocc.util.Position;

public class CharacterLiteralTokenTest
{
	protected CharStrLiteralToken parse(CodeReader cr) throws IOException, SyntaxException
	{
		return CharacterLiteralToken.parse(cr);
	}

	protected void test(String code, char next, int... values) throws IOException, SyntaxException
	{
		CodeReader cr = new CodeReader(new StringReader("\n  " + code));
		cr.read();
		cr.read();
		cr.read();
		CharStrLiteralToken token = parse(cr);
		assertNotNull(token);
		for (int i = 0; i < values.length; ++i)
			assertEquals(Integer.valueOf(values[i]), token.getValues().get(i));
		assertEquals(next, cr.peek());
		assertEquals(new Position(1, 2), token.getPosition());
	}

	protected void testException(String code, String msg, int line, int column) throws IOException
	{
		try {
			test(code, ' ');
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
			assertEquals(new Position(line + 1, column + 2), e.getPosition());
			assertEquals(msg, e.getMessage());
		}
	}

	@Test
	public void matchSimpleLiteral() throws IOException, SyntaxException
	{
		test("'a'#", '#', (int) 'a');
	}

	@Test
	public void matchSimpleEscapeSequence() throws IOException, SyntaxException
	{
		test("'\\\''#", '#', (int) '\'');
		test("'\\\"'#", '#', (int) '\"');
		test("'\\?'#", '#', (int) '?');
		test("'\\\\'#", '#', (int) '\\');
		test("'\\a'#", '#', 0x07);
		test("'\\b'#", '#', (int) '\b');
		test("'\\f'#", '#', (int) '\f');
		test("'\\n'#", '#', (int) '\n');
		test("'\\r'#", '#', (int) '\r');
		test("'\\t'#", '#', (int) '\t');
		test("'\\v'#", '#', 0x0b);
	}

	@Test
	public void matchHexadecimalEscapeSequence() throws IOException, SyntaxException
	{
		test("'\\x0'#", '#', 0x0);
		test("'\\x1ABCdef'#", '#', 0x1ABCdef);
		test("'\\xffffffff'#", '#', 0xffffffff);
	}

	@Test
	public void matchOctalEscapeSequence() throws IOException, SyntaxException
	{
		test("'\\0'#", '#', 0);
		test("'\\01234567'#", '#', 01234567);
		test("'\\037777777777'#", '#', 0xffffffff);
	}

	@Test
	public void matchUniversalCharacterName() throws IOException, SyntaxException
	{
		test("'\\u0024'#", '#', 0x0024);
		test("'\\u0040'#", '#', 0x0040);
		test("'\\U0060'#", '#', 0x0060);
		test("'\\U00A0'#", '#', 0x00a0);
		test("'\\ud7ff'#", '#', 0xd7ff);
		test("'\\ue000'#", '#', 0xe000);
		test("'\\uffff'#", '#', 0xffff);
	}

	@Test
	public void matchMultipleCharacters() throws IOException, SyntaxException
	{
		test("'abc'#", '#', (int) 'a', (int) 'b', (int) 'c');
		test("'\\xAf\\0123a\\n'#", '#', 0xaf, 0123, (int) 'a', (int) '\n');
	}

	@Test
	public void dontMatchOtherAlphabetsInHexEscapeSequence() throws IOException, SyntaxException
	{
		test("'\\xfg'#", '#', 0xf, (int) 'g');
	}

	@Test
	public void dontMatchEightOrNineInOctalEscapeSequence() throws IOException, SyntaxException
	{
		test("'\\078'#", '#', 7, (int) '8');
		test("'\\079'#", '#', 7, (int) '9');
	}

	@Test
	public void matchOnlyFourDigitsInUniversalCharacterName() throws IOException, SyntaxException
	{
		test("'\\u00C43'#", '#', 0x00c4, (int) '3');
	}

	@Test
	public void throwIfEmptyLiteral() throws IOException
	{
		testException("''", "Empty character or string literal.", 0, 0);
	}

	@Test
	public void throwIfNewLineInLiteral() throws IOException
	{
		testException("'a\nb'", "New-line in character or string literal.", 0, 2);
	}

	@Test
	public void throwIfUnterminated() throws IOException
	{
		testException("'abc", "Unterminated character or string literal.", 0, 0);
	}

	@Test
	public void throwIfEmptyHexadecimalEscapeSequence() throws IOException
	{
		testException("'\\x'", "No digits in hexadecimal escape sequence.", 0, 1);
	}

	@Test
	public void throwIfHexadecimalEscapeSequenceTooLarge() throws IOException
	{
		testException("'\\x100000000'", "Value of hexadecimal escape sequence is too large.", 0, 1);
	}

	@Test
	public void throwIfOctalEscapeSequenceTooLarge() throws IOException
	{
		testException("'\\040000000000'", "Value of octal escape sequence is too large.", 0, 1);
	}

	@Test
	public void throwIfUniversalCharacterNameTooShort() throws IOException
	{
		testException("'\\ufffg'", "Incomplete universal character name.", 0, 1);
	}

	@Test
	public void throwIfIllegalUniversalChar() throws IOException
	{
		String msg = "Illegal value for universal character.";
		testException("'\\u0099'", msg, 0, 1);
		testException("'\\ud800'", msg, 0, 1);
		testException("'\\udfff'", msg, 0, 1);
	}

	@Test
	public void throwIfIllegalEscapeSequence() throws IOException
	{
		testException("'\\X'", "Illegal escape sequence.", 0, 1);
	}
}
