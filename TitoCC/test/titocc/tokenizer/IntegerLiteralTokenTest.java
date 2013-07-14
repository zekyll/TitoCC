package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;
import titocc.util.Position;

public class IntegerLiteralTokenTest
{
	private void test(String code, String prefix, String digits, String suffix, int base,
			char next) throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("\n  " + code));
		cr.read();
		cr.read();
		cr.read();
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNotNull(token);
		assertEquals(digits, token.getValue());
		assertEquals(suffix, token.getSuffix());
		assertEquals(prefix + digits + suffix, token.toString());
		assertEquals(next, cr.peek());
		assertEquals(new Position(1, 2), token.getPosition());
	}

	@Test
	public void matchMaxInt() throws IOException
	{
		test("2147483647*", "", "2147483647", "", 10, '*');
	}

	@Test
	public void matchHugeInteger() throws IOException
	{
		test("18446744073709551616+", "", "18446744073709551616", "", 10, '+');
	}

	@Test
	public void dontMatchNegative() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("-1"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('-', cr.peek());
	}

	@Test
	public void dontMatchLetters() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("abc"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('a', cr.peek());
	}

	@Test
	public void dontMatchSymbols() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("%abc"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('%', cr.peek());
	}

	@Test
	public void matchSuffixes() throws IOException
	{
		test("123abc0&", "", "123", "abc0", 10, '&');
		test("98_9[", "", "98", "_9", 10, '[');
	}

	@Test
	public void matchHexadecimal() throws IOException
	{
		test("0x12F3C!", "0x", "12F3C", "", 16, '!');
		test("0x00aBu|", "0x", "00aB", "u", 16, '|');
		test("0Xabcdef01234567890ABCDEFG0 ", "0X", "abcdef01234567890ABCDEF", "G0", 16, ' ');
	}

	@Test
	public void matchOctal() throws IOException
	{
		test("01234567/", "", "01234567", "", 8, '/');
		test("0007410a&", "", "0007410", "a", 8, '&');
		test("01234567^", "", "01234567", "", 8, '^');
	}

	@Test
	public void eightAndNineDontMatchOctal() throws IOException
	{
		test("0778'", "", "077", "", 8, '8');
		test("09,", "", "0", "", 8, '9');
	}

	@Test
	public void interterpretAsOctalIfNoHexaDigits() throws IOException
	{
		test("0xg+", "", "0", "xg", 8, '+');
		test("0Xx-", "", "0", "Xx", 8, '-');
	}

	@Test
	public void zeroIsOctal() throws IOException
	{
		test("0)", "", "0", "", 8, ')');
		test("0_$", "", "0", "_", 8, '$');
	}
}
