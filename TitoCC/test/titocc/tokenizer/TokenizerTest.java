package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import titocc.util.Position;

public class TokenizerTest
{
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
		assertEquals("<eof>", "<End of file>", tokens.get(10).toString());
	}

	@Test
	public void throwsWhenUnrecognizedTokens() throws IOException, SyntaxException
	{
		try {
			Tokenizer t = new Tokenizer(new StringReader("x = 1; #"));
			t.tokenize();
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
			assertEquals(new Position(0, 7), e.getPosition());
			assertEquals("Unrecognized token.", e.getMessage());
		}
	}

	@Test
	public void singleLineComment() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(
				"a//b /* c \n"
				+ "\te"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertEquals("e", tokens.get(1).toString());
	}

	@Test
	public void singleLineCommentInFileEnd() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(
				"a//b"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertTrue("<eof>", tokens.get(1) instanceof EofToken);
	}

	@Test
	public void multiLineComment() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(
				"a/*/b // c \n"
				+ "d /* e \n"
				+ "f */ g"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertEquals("g", tokens.get(1).toString());
	}

	@Test
	public void multipleConsequtiveComments() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader(
				"a //b \n"
				+ "\t/* */ //c d\n"
				+ " d\n"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertEquals("d", tokens.get(1).toString());
	}

	@Test
	public void commentBlockFollowedBySlash() throws IOException, SyntaxException
	{
		Tokenizer t = new Tokenizer(new StringReader("a/**//\n"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertEquals("/", tokens.get(1).toString());
	}

	@Test
	public void commentWithoutSurroundingSpaces() throws IOException, SyntaxException
	{
		// Tests that tokens are not combined.
		Tokenizer t = new Tokenizer(new StringReader("a/**/b\n"));
		List<Token> tokens = t.tokenize();
		assertEquals("a", tokens.get(0).toString());
		assertEquals("b", tokens.get(1).toString());
	}

	@Test
	public void throwsWhenUnterminatedComment() throws IOException, SyntaxException
	{
		try {
			Tokenizer t = new Tokenizer(new StringReader(
					"a b/*c d e "));
			t.tokenize();
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
			assertEquals(new Position(0, 3), e.getPosition());
			assertEquals("Unterminated comment.", e.getMessage());
		}
	}
}
