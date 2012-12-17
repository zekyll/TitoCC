package titocc.tokenizer;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class TokenStream
{
	private List<Token> tokens;
	private Stack<Integer> marks = new Stack<Integer>();
	private ListIterator<Token> position;
	private int sinceLastMark;

	public TokenStream(List<Token> tokens)
	{
		this.tokens = tokens;
		position = this.tokens.listIterator();
		sinceLastMark = 0;
	}

	public void pushMark()
	{
		marks.push(sinceLastMark);
		sinceLastMark = 0;
	}

	public void popMark(boolean reset)
	{
		if (reset) {
			while (sinceLastMark > 0) {
				--sinceLastMark;
				position.previous();
			}
		}
		sinceLastMark += marks.pop();
	}

	public Token read()
	{
		++sinceLastMark;
		return position.next();
	}

	public boolean hasNext()
	{
		return position.hasNext();
	}

	public int getLine()
	{
		Token t = position.next();
		position.previous();
		return t.getLine();
	}

	public int getColumn()
	{
		Token t = position.next();
		position.previous();
		return t.getColumn();
	}
}
