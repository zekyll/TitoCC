package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeReaderTest
{
	private CodeReader codeReader;

	private final String text = "abc\ndef gh \n ijkl";

	@Before
	public void setUp() throws IOException
	{
		codeReader = new CodeReader(new StringReader(text));
	}

	@Test
	public void lineNumberAndColumnStartFromZero()
	{
		assertEquals(0, codeReader.getLineNumber());
		assertEquals(0, codeReader.getColumn());
	}

	@Test
	public void readsRightCharacters() throws IOException
	{
		for (int i = 0; i < text.length(); ++i) {
			char c = codeReader.read();
			assertEquals(text.charAt(i), c);
		}
	}

	@Test
	public void endsInNullCharacter() throws IOException
	{
		for (int i = 0; i < text.length(); ++i)
			codeReader.read();
		assertEquals('\0', codeReader.read());
	}

	@Test
	public void peekReturnsNextChar() throws IOException
	{
		codeReader.read();
		assertEquals('b', codeReader.peek());
	}

	@Test
	public void peek2ndReturnsCharacterAfterNext() throws IOException
	{
		codeReader.read();
		assertEquals('c', codeReader.peek2nd());
	}

	@Test
	public void peekDoesNotRemoveCharacters() throws IOException
	{
		codeReader.read();
		codeReader.peek();
		codeReader.peek();
		char c = codeReader.read();
		assertEquals('b', c);
	}

	@Test
	public void peek2ndDoesNotRemoveCharacters() throws IOException
	{
		codeReader.read();
		codeReader.peek2nd();
		codeReader.peek2nd();
		char c = codeReader.read();
		assertEquals('b', c);
	}

	@Test
	public void lineNumberAndColumnAreCorrect() throws IOException
	{
		for (int i = 0; i < text.indexOf('\n') + 3; ++i)
			codeReader.read();
		assertEquals(1, codeReader.getLineNumber());
		assertEquals(2, codeReader.getColumn());
	}

	@Test
	public void lineNumberChangesAfterNewline() throws IOException
	{
		int lineLen = text.indexOf('\n');
		for (int i = 0; i < lineLen; ++i)
			codeReader.read();
		assertEquals(0, codeReader.getLineNumber());
		codeReader.read();
		assertEquals(1, codeReader.getLineNumber());
	}

	@Test
	public void columnResetAfterNewline() throws IOException
	{
		int lineLen = text.indexOf('\n');
		for (int i = 0; i < lineLen; ++i)
			codeReader.read();
		assertEquals(lineLen, codeReader.getColumn());
		codeReader.read();
		assertEquals(0, codeReader.getColumn());
	}

	@Test
	public void skippingWhitespaceWorks() throws IOException
	{
		for (int i = 0; i < text.indexOf('h') + 1; ++i)
			codeReader.read();
		codeReader.skipWhiteSpace();
		assertEquals('i', codeReader.read());
	}
}
