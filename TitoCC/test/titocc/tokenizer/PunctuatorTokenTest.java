package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;

public class PunctuatorTokenTest
{
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
