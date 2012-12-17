package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PunctuatorTokenTest
{
	public PunctuatorTokenTest()
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
	public void matchPunctuators() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("*abc"));
		PunctuatorToken token = PunctuatorToken.parse(cr);
		assertNotNull(token);
		assertEquals("*", token.toString());
		assertEquals('a', cr.read());
	}

	@Test
	public void matchLongestPunctuator() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("+++"));
		PunctuatorToken token = PunctuatorToken.parse(cr);
		assertNotNull(token);
		assertEquals("++", token.toString());
		assertEquals('+', cr.read());
	}

	@Test
	public void dontMatchLetters() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("abc"));
		PunctuatorToken token = PunctuatorToken.parse(cr);
		assertNull(token);
		assertEquals('a', cr.read());
	}

	@Test
	public void dontMatchNumbers() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("0123=="));
		PunctuatorToken token = PunctuatorToken.parse(cr);
		assertNull(token);
		assertEquals('0', cr.read());
	}
}
