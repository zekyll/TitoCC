package titocc.tokenizer;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenStreamTest
{
	private TokenStream stream;
	private Token token1, token2, token3;

	@Before
	public void setUp()
	{
		token1 = new IdentifierToken("a", 10, 30);
		token2 = new IdentifierToken("b", 11, 31);
		token3 = new IdentifierToken("c", 12, 32);
		List<Token> tokenList = new ArrayList<Token>();
		tokenList.add(token1);
		tokenList.add(token2);
		tokenList.add(token3);
		stream = new TokenStream(tokenList);
	}

	@Test
	public void readsCorrectTokens()
	{
		assertSame(stream.read(), token1);
		assertSame(stream.read(), token2);
		assertSame(stream.read(), token3);
	}

	@Test
	public void hasNextReturnsCorrectValue()
	{
		assertTrue(stream.hasNext());
		for (int i = 0; i < 3; ++i)
			stream.read();
		assertFalse(stream.hasNext());
	}

	@Test
	public void popMarkWithResetMovesToCorrectPosition()
	{
		stream.read();
		stream.pushMark();
		stream.read();
		stream.read();
		stream.popMark(true);
		assertSame(token2, stream.read());
	}

	@Test
	public void popMarkTwiceWithResetMovesToCorrectPosition()
	{
		stream.pushMark();
		stream.read();
		stream.read();
		stream.pushMark();
		stream.read();
		stream.popMark(true);
		stream.popMark(true);
		assertSame(token1, stream.read());
	}

	@Test
	public void popMarkWithoutResetDoesntMovePosition()
	{
		stream.read();
		stream.pushMark();
		stream.read();
		stream.popMark(false);
		assertSame(token3, stream.read());
	}

	@Test
	public void popMarkRemovesTheMark()
	{
		stream.read();
		stream.pushMark();
		stream.read();
		stream.pushMark();
		stream.read();
		stream.popMark(false);
		stream.popMark(true);
		assertSame(token2, stream.read());
	}

	@Test
	public void lineAndColumnAreCorrect()
	{
		stream.read();
		assertEquals(token2.getColumn(), stream.getColumn());
		assertEquals(token2.getLine(), stream.getLine());
		assertSame(token2, stream.read());
	}
}
