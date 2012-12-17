package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IdentifierTokenTest
{
	public IdentifierTokenTest()
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
	public void matchUnderscoreStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("_abc123+"));
		IdentifierToken token = IdentifierToken.parse(cr);
		assertNotNull(token);
		assertEquals("_abc123", token.toString());
		assertEquals('+', cr.read());
	}

	@Test
	public void matchAlphabetStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("zyx321 "));
		IdentifierToken token = IdentifierToken.parse(cr);
		assertNotNull(token);
		assertEquals("zyx321", token.toString());
		assertEquals('\0', cr.read());
	}

	@Test
	public void dontMatchNumberStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("123abc"));
		IdentifierToken token = IdentifierToken.parse(cr);
		assertNull(token);
		assertEquals('1', cr.read());
	}

	@Test
	public void dontMatchSymbolStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("$abc"));
		IdentifierToken token = IdentifierToken.parse(cr);
		assertNull(token);
		assertEquals('$', cr.read());
	}
}
