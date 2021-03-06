package titocc.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import titocc.util.Position;

/**
 * Main tokenizer class. Converts input text into a list of tokens. There are
 * currently 5 token types: punctuators (operators etc), keywords, identifiers,
 * integer literals and end of file (EOF).
 */
public class Tokenizer
{
	/**
	 * Input reader.
	 */
	private final CodeReader input;

	/**
	 * Constructs a Tokenizer.
	 *
	 * @param input Reader for the source text
	 */
	public Tokenizer(Reader input) throws IOException
	{
		this.input = new CodeReader(input);
	}

	/**
	 * Tokenizes the input.
	 *
	 * @return list of tokens
	 * @throws IOException if input reader throws
	 * @throws SyntaxException if invalid tokens are encountered
	 */
	public List<Token> tokenize() throws IOException, SyntaxException
	{
		List<Token> tokens = new LinkedList<Token>();

		Token token;
		do {
			boolean commentFound;
			do {
				input.skipWhiteSpace();
				commentFound = skipComments();
			} while (commentFound);

			input.skipWhiteSpace();
			token = getNextToken();
			tokens.add(token);
		} while (!(token instanceof EofToken));

		//TODO check that non-empty source file ends in	newline

		return tokens;
	}

	/**
	 * Parses the next token from the input stream.
	 *
	 * @return next token
	 * @throws IOException if code reader throws
	 * @throws SyntaxException if no tokens matched the input text
	 */
	private Token getNextToken() throws IOException, SyntaxException
	{
		Token token = IntegerLiteralToken.parse(input);
		if (token != null)
			return token;

		token = PunctuatorToken.parse(input);
		if (token != null)
			return token;

		token = WordToken.parse(input); // KeywordToken or IdentifierToken
		if (token != null)
			return token;

		token = CharacterLiteralToken.parse(input);
		if (token != null)
			return token;

		token = StringLiteralToken.parse(input);
		if (token != null)
			return token;

		token = EofToken.parse(input);
		if (token != null)
			return token;

		throw new SyntaxException("Unrecognized token.", input.getPosition());
	}

	/**
	 * Checks if the next character starts a comment and skips the comment.
	 *
	 * @return true if comment was found, false otherwise
	 * @throws IOException if reader throws
	 * @throws SyntaxException if EOF is found inside a comment block
	 */
	private boolean skipComments() throws IOException, SyntaxException
	{
		if (input.peek() == '/' && input.peek2nd() == '/') {
			char c;
			do {
				c = input.read();
			} while (!(c == '\n' || c == '\0'));

			return true;
		} else if (input.peek() == '/' && input.peek2nd() == '*') {
			Position pos = input.getPosition();
			input.read();
			input.read();

			char c = '\0', prevc;
			do {
				prevc = c;
				c = input.read();
				if (c == '\0')
					throw new SyntaxException("Unterminated comment.", pos);
			} while (!(prevc == '*' && c == '/'));

			return true;
		}

		return false;
	}
}
