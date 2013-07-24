package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;

public class WordTokenTest
{
	@Test
	public void matchIdentifierWithUnderscoreStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("_abc123+"));
		WordToken token = WordToken.parse(cr);
		assertTrue(token instanceof IdentifierToken);
		assertEquals("_abc123", token.toString());
		assertEquals('+', cr.read());
	}

	@Test
	public void matchIdentifierWithAlphabetStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("zyx321 "));
		WordToken token = WordToken.parse(cr);
		assertTrue(token instanceof IdentifierToken);
		assertEquals("zyx321", token.toString());
		assertEquals(' ', cr.read());
	}

	@Test
	public void matchIdentifiersContainingKeywords() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("ifabc/"));
		WordToken token = WordToken.parse(cr);
		assertTrue(token instanceof IdentifierToken);
		assertEquals("ifabc", token.toString());
		assertEquals('/', cr.read());
	}

	@Test
	public void dontMatchNumberStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("123abc"));
		WordToken token = WordToken.parse(cr);
		assertNull(token);
		assertEquals('1', cr.read());
	}

	@Test
	public void dontMatchSymbolStart() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("$abc"));
		WordToken token = WordToken.parse(cr);
		assertNull(token);
		assertEquals('$', cr.read());
	}

	@Test
	public void matchKeywords() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("if "));
		WordToken token = WordToken.parse(cr);
		assertTrue(token instanceof KeywordToken);
		assertEquals("if", token.toString());
		assertEquals(' ', cr.read());
	}

	@Test
	public void dontMatchNumbers() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("123abc"));
		WordToken token = WordToken.parse(cr);
		assertNull(token);
		assertEquals('1', cr.read());
	}

	@Test
	public void dontMatchSymbols() throws IOException
	{
		CodeReader cr = new CodeReader(new StringReader("$abc"));
		WordToken token = WordToken.parse(cr);
		assertNull(token);
		assertEquals('$', cr.read());
	}
}
