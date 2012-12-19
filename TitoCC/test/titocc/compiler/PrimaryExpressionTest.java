package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import titocc.compiler.elements.Expression;
import titocc.compiler.elements.PrimaryExpression;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.EofToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.tokenizer.Tokenizer;

public class PrimaryExpressionTest
{
	private TokenStream stream;

	public PrimaryExpressionTest()
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

	private Expression parse(String code) throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(code));
		stream = new TokenStream(t.tokenize());
		return PrimaryExpression.parse(stream);
	}

	@Test
	public void matchesIdentifier() throws IOException, SyntaxException
	{
		assertEquals("(ID_EXPR abc)", parse("abc").toString());
		assertTrue(stream.read() instanceof EofToken);
	}

	@Test
	public void matchesIntegerLiteral() throws IOException, SyntaxException
	{
		assertEquals("(INT_EXPR 13 )", parse("13").toString());
		assertTrue(stream.read() instanceof EofToken);
	}

	@Test
	public void matchesIntegerLiteralWithSuffix() throws IOException, SyntaxException
	{
		assertEquals("(INT_EXPR 0 U)", parse("0U").toString());
		assertTrue(stream.read() instanceof EofToken);
	}

	@Test
	public void matchesBraceExpression() throws IOException, SyntaxException
	{
		assertEquals("(INT_EXPR 666 )", parse("(666)").toString());
		assertTrue(stream.read() instanceof EofToken);
	}

	@Test
	public void doesntMatchKeywords() throws IOException, SyntaxException
	{
		assertNull(parse("if"));
		assertTrue(stream.read().toString().equals("if"));
	}

	@Test
	public void doesntMatchPunctuators() throws IOException, SyntaxException
	{
		assertNull(parse("+"));
		assertTrue(stream.read().toString().equals("+"));
	}
}
