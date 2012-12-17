package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenizerTest
{
	public TokenizerTest()
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
	public void returnsCorrectTokens() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(
				"if(x == 0)\n"
				+ "\tx = 1;"));
		List<Token> tokens = t.tokenize();

		assertTrue("if", tokens.get(0) instanceof KeywordToken);
		assertEquals("if", "if", tokens.get(0).toString());

		assertTrue("(", tokens.get(1) instanceof PunctuatorToken);
		assertEquals("(", "(", tokens.get(1).toString());

		assertTrue("x", tokens.get(2) instanceof IdentifierToken);
		assertEquals("x", "x", tokens.get(2).toString());

		assertTrue("==", tokens.get(3) instanceof PunctuatorToken);
		assertEquals("==", "==", tokens.get(3).toString());

		assertTrue("0", tokens.get(4) instanceof IntegerLiteralToken);
		assertEquals("0", "0", tokens.get(4).toString());

		assertTrue(")", tokens.get(5) instanceof PunctuatorToken);
		assertEquals(")", ")", tokens.get(5).toString());

		assertTrue("x", tokens.get(6) instanceof IdentifierToken);
		assertEquals("x", "x", tokens.get(6).toString());

		assertTrue("=", tokens.get(7) instanceof PunctuatorToken);
		assertEquals("=", "=", tokens.get(7).toString());

		assertTrue("1", tokens.get(8) instanceof IntegerLiteralToken);
		assertEquals("1", "1", tokens.get(8).toString());

		assertTrue(";", tokens.get(9) instanceof PunctuatorToken);
		assertEquals(";", ";", tokens.get(9).toString());

		assertTrue("<eof>", tokens.get(10) instanceof EofToken);
		assertEquals("<eof>", "", tokens.get(10).toString());
	}

	@Test
	public void throwsWhenUnrecognizedTokens() throws IOException, SyntaxException
	{
		try {
			Tokenizer t = new Tokenizer(new StringReader("x = 1; #"));
			t.tokenize();
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
		}
	}
}
