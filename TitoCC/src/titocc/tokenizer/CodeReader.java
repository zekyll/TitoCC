package titocc.tokenizer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import titocc.util.Position;

/**
 * Wrapper for Reader interface that reads characters one at a time and keeps
 * track of lines and columns. Also supports peeking the next and character
 * after that without removing them from the input stream.
 */
public class CodeReader
{
	/**
	 * Next character to read. Returned by peek().
	 */
	private char nextChar;

	/**
	 * The character to read after next char. Returned by peek2nd().
	 */
	private char nextChar2;

	/**
	 * The wrapped reader object.
	 */
	private final Reader reader;

	/**
	 * Current line number.
	 */
	private int line;

	/**
	 * Current column number.
	 */
	private int column;

	/**
	 * Constructs a CodeReader.
	 *
	 * @param reader reader used for input
	 */
	public CodeReader(Reader reader) throws IOException
	{
		this.reader = new LineNumberReader(reader);
		nextChar = readFromInternalReader();
		nextChar2 = readFromInternalReader();
		line = 0;
		column = 0;
	}

	/**
	 * Reads a single character, removing it from the stream.
	 *
	 * @return next character or null character if end of the stream
	 * @throws IOException if the underlying reader throws
	 */
	public char read() throws IOException
	{
		char c = nextChar;
		nextChar = nextChar2;
		nextChar2 = readFromInternalReader();

		++column;

		if (c == '\n') {
			++line;
			column = 0;
		}

		return (char) c;
	}

	/**
	 * Return the next character that will be returned by read(), but does not
	 * remove the character from the stream.
	 *
	 * @return next character
	 */
	public char peek()
	{
		return nextChar;
	}

	/**
	 * Return the character that will be returned by second call to read(), but
	 * does not remove any characters from the stream.
	 *
	 * @return the character after next character
	 */
	public char peek2nd()
	{
		return nextChar2;
	}

	/**
	 * Returns current line number.
	 *
	 * @return line number
	 */
	public int getLineNumber()
	{
		return line;
	}

	/**
	 * Returns current column.
	 *
	 * @return current column number
	 */
	public int getColumn()
	{
		return column;
	}

	/**
	 * Returns current position.
	 *
	 * @return current position
	 */
	public Position getPosition()
	{
		return new Position(line, column);
	}

	/**
	 * Reads and ignores characters until next non-whitespace character is
	 * found.
	 *
	 * @throws IOException if the underlying reader throws
	 */
	public void skipWhiteSpace() throws IOException
	{
		while (Character.isWhitespace(peek()))
			read();
	}

	/**
	 * Reads one character from the internal reader. Convert the int value to
	 * char so that EOF (-1) is repsesented by null character.
	 *
	 * @param c character to convert
	 * @return result of the conversion
	 */
	private char readFromInternalReader() throws IOException
	{
		int c = reader.read();
		return c == -1 ? '\0' : (char) c;
	}
}
