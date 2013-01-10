package titocc.tokenizer;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Stream of tokens that allows marking positions in the stream and returning
 * back to them.
 */
public class TokenStream
{
	/**
	 * Source tokens.
	 */
	private List<Token> tokens;
	/**
	 * Saved stream positions.
	 */
	private Stack<Integer> marks = new Stack<Integer>();
	/**
	 * Current position.
	 */
	private ListIterator<Token> position;
	/**
	 * Tokens since the last mark.
	 */
	private int sinceLastMark;
	/**
	 * Read token that has biggest line number/column.
	 */
	private Token furthestReadToken;

	/**
	 * Constructs a TokenStream from a list of tokens.
	 *
	 * @param tokens input tokens
	 */
	public TokenStream(List<Token> tokens)
	{
		this.tokens = tokens;
		position = this.tokens.listIterator();
		sinceLastMark = 0;
	}

	/**
	 * Marks the current position in the stream and pushes the mark into an
	 * internal stack.
	 */
	public void pushMark()
	{
		marks.push(sinceLastMark);
		sinceLastMark = 0;
	}

	/**
	 * Removes the previously added mark and optionally resets the stream
	 * position back to the mark.
	 *
	 * @param reset if true then stream position is set to the marked position
	 */
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

	/**
	 * Reads one token and advances stream position by one.
	 *
	 * @return the token
	 */
	public Token read()
	{
		++sinceLastMark;
		Token next = position.next();
		if (isNewFurthestReadToken(next))
			furthestReadToken = next;
		return next;
	}

	/**
	 * Checks if stream contains unread tokens.
	 *
	 * @return true is stream has unread tokens
	 */
	public boolean hasNext()
	{
		return position.hasNext();
	}

	/**
	 * Returns the line number of the next token.
	 *
	 * @return line number
	 */
	public int getLine()
	{
		Token t = position.next();
		position.previous();
		return t.getLine();
	}

	/**
	 * Returns the column number of the next token.
	 *
	 * @return column number
	 */
	public int getColumn()
	{
		Token t = position.next();
		position.previous();
		return t.getColumn();
	}

	/**
	 * Returns a previously read token that is furthest into the stream. It is
	 * determined by line number of column. Resetting the stream does not reset
	 * this.
	 *
	 * @return column number
	 */
	public Token getFurthestReadToken()
	{
		return furthestReadToken;
	}

	/**
	 * Checks if the new token is further in the text. It first checks based on
	 * line number and if they are the same then based on column.
	 */
	private boolean isNewFurthestReadToken(Token token)
	{
		if (furthestReadToken == null)
			return true;
		if (token.getLine() != furthestReadToken.getLine())
			return token.getLine() > furthestReadToken.getLine();
		else
			return token.getColumn() > furthestReadToken.getColumn();
	}
}
