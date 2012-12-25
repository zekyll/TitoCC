package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegerLiteralTokenTest
{
	public IntegerLiteralTokenTest()
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
	public void matchMaxInt() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("2147483647*"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNotNull(token);
		assertEquals("2147483647", token.getValue());
		assertEquals('*', cr.read());
	}

	@Test
	public void matchHugeInteger() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("18446744073709551616+"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNotNull(token);
		assertEquals("18446744073709551616", token.getValue());
		assertEquals('+', cr.read());
	}

	@Test
	public void matchZero() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("0 "));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNotNull(token);
		assertEquals("0", token.getValue());
		assertEquals(' ', cr.read());
	}

	@Test
	public void dontMatchNegative() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("-1"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('-', cr.read());
	}

	@Test
	public void dontMatchLetters() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("abc"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('a', cr.read());
	}

	@Test
	public void dontMatchSymbols() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("%abc"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNull(token);
		assertEquals('%', cr.read());
	}

	@Test
	public void matchSuffixes() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("123abc0&"));
		IntegerLiteralToken token = IntegerLiteralToken.parse(cr);
		assertNotNull(token);
		assertEquals("123", token.getValue());
		assertEquals("abc0", token.getSuffix());
		assertEquals('&', cr.read());
	}
}
