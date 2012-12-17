package titocc.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeReaderTest
{
	private CodeReader codeReader;
	private final String text = "abc\ndef gh \n ijkl";

	public CodeReaderTest()
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
		codeReader = new CodeReader(new StringReader(text));
	}

	@After
	public void tearDown()
	{
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
	public void readsSameCharacterAfterUnread() throws IOException
	{
		char c = codeReader.read();
		codeReader.unread();
		char c2 = codeReader.read();
		assertEquals(c, c2);
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
	public void lineNumberAndColumnAreCorrectAfterUnreadingNewline() throws IOException
	{
		for (int i = 0; i < text.indexOf('\n') + 1; ++i)
			codeReader.read();
		codeReader.unread();
		assertEquals(0, codeReader.getLineNumber());
		assertEquals(text.indexOf('\n'), codeReader.getColumn());
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
