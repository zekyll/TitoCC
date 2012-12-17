package titocc.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class Tokenizer
{
	private CodeReader input;

	public Tokenizer(Reader input)
	{
		this.input = new CodeReader(input);
	}

	public List<Token> tokenize() throws IOException, SyntaxException
	{
		List<Token> tokens = new LinkedList<Token>();

		Token token;
		do {
			input.skipWhiteSpace();
			token = getNextToken();
			tokens.add(token);
		} while (!(token instanceof EofToken));

		return tokens;
	}

	private Token getNextToken() throws IOException, SyntaxException
	{
		// Int literals must be parsed before punctuators because they might
		// start with "-".
		Token token = IntegerLiteralToken.parse(input);
		if (token != null)
			return token;

		token = PunctuatorToken.parse(input);
		if (token != null)
			return token;

		token = IdentifierToken.parse(input);
		if (token != null)
			return token;

		token = KeywordToken.parse(input);
		if (token != null)
			return token;

		token = EofToken.parse(input);
		if (token != null)
			return token;

		throw new SyntaxException("Unrecognized token.", input.getLineNumber(),
				input.getColumn());
	}
}
