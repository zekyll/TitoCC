package titocc.tokenizer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Wrapper for Reader interface that reads characters one at a time and keeps
 * track of lines and columns. Also supports unreading single character back
 * into the input stream.
 */
public class CodeReader
{
	/**
	 * Length of the previous line. Needed when unreading line changes.
	 */
	private int previousLineLength;
	/**
	 * LineNumberReader that wraps the given reader object and keeps track of
	 * the line number.
	 */
	private final LineNumberReader reader;
	/**
	 * Current column number.
	 */
	private int column;

	/**
	 *
	 */
	/**
	 * Constructs a CodeReader.
	 *
	 * @param reader
	 */
	public CodeReader(Reader reader)
	{
		this.reader = new LineNumberReader(reader);
	}

	/**
	 * Reads a single character.
	 *
	 * @return next character or null character if end of the stream
	 * @throws IOException if the underlying reader throws
	 */
	public char read() throws IOException
	{
		reader.mark(1);
		int c = reader.read();

		if (c == -1)
			return '\0';

		// Store previous line length so we can unread newline
		if (c == '\n') {
			previousLineLength = column;
			column = 0;
		} else
			++column;

		return (char) c;
	}

	/**
	 * Puts the previously read character back into the stream. This cannot be
	 * called more than once between two read() calls.
	 *
	 * @throws IOException if reader.reset() throws
	 */
	public void unread() throws IOException
	{
		if (column == 0)
			column = previousLineLength;
		else
			--column;
		reader.reset();
	}

	/**
	 * Returns current line number.
	 *
	 * @return line number
	 */
	public int getLineNumber()
	{
		return reader.getLineNumber();
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
	 * Reads and ignores characters until next non-whitespace character is
	 * found.
	 *
	 * @throws IOException if the underlying reader throws
	 */
	public void skipWhiteSpace() throws IOException
	{
		char c;
		do {
			c = read();
		} while (Character.isWhitespace(c));

		if (c != '\0')
			unread();
	}
}
