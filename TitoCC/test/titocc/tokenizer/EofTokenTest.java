package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class EofTokenTest
{
	public EofTokenTest()
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
	public void match() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader(""));
		EofToken token = EofToken.parse(cr);
		assertNotNull(token);
		assertEquals("<End of file>", token.toString());
	}

	@Test
	public void nomatch() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("a"));
		assertNull(EofToken.parse(cr));
		assertEquals('a', cr.read());
	}

}
