package titocc.compiler;

import titocc.tokenizer.SyntaxException;
import java.util.List;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Parser
{
	private TokenStream tokens;

	public Parser(List<Token> tokens)
	{
		this.tokens = new TokenStream(tokens);
	}

	public TranslationUnit parse() throws SyntaxException
	{
		TranslationUnit trUnit = TranslationUnit.parse(tokens);
		if (trUnit == null) {
			throw new SyntaxException("Parser error.", -1, -1);
		}
		return trUnit;
	}
}
