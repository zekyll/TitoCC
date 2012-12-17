package titocc.tokenizer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class CodeReader
{
	private int previousLineLength;
	private LineNumberReader reader;
	private int column;

	public CodeReader(Reader reader)
	{
		this.reader = new LineNumberReader(reader);
	}

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

	public void unread() throws IOException
	{
		if (column == 0)
			column = previousLineLength;
		else
			--column;
		reader.reset();
	}

	public int getLineNumber()
	{
		return reader.getLineNumber();
	}

	public int getColumn()
	{
		return column;
	}

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
