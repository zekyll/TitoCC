package titocc.util;

/**
 * Position within a text file. Contains a line number and a column number, both of which start from
 * 0.
 */
public final class Position implements Comparable<Position>
{
	/**
	 * Line number.
	 */
	public final int line;

	/**
	 * Column number. (Position within a line.)
	 */
	public final int column;

	/**
	 * Constructs a Position from line and column numbers.
	 *
	 * @param line line number
	 * @param column column number
	 */
	public Position(int line, int column)
	{
		this.line = line;
		this.column = column;
	}

	@Override
	public int compareTo(Position pos2)
	{
		if (line != pos2.line)
			return line - pos2.line;
		else
			return column - pos2.column;
	}

	@Override
	public boolean equals(Object obj)
	{
		Position pos2 = (Position) obj;
		return line == pos2.line && column == pos2.column;
	}

	@Override
	public String toString()
	{
		return "(line " + line + ", column " + column + ")";
	}
}
