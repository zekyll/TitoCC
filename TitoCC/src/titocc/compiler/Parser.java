package titocc.compiler;

import java.util.List;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Main parser class. Really just a wrapper and the actual parsing is done by
 * the CodeElement classes.
 */
public class Parser
{
	/**
	 * Parses a list of tokens. Produces a code element tree structure where
	 * TranslationUnit is the root element.
	 *
	 * @return a TranslationUnit object which is the top level element in the
	 * code element hierarchy
	 * @throws SyntaxException if the source code has errors
	 */
	static public TranslationUnit parse(List<Token> tokens) throws SyntaxException
	{
		TokenStream tokenStream = new TokenStream(tokens);

		TranslationUnit trUnit = TranslationUnit.parse(tokenStream);
		if (trUnit == null) {
			Token token = tokenStream.getFurthestReadToken();
			throw new SyntaxException(
					"Unexpected token \"" + token + "\".",
					token.getLine(),
					token.getColumn());
		}
		return trUnit;
	}
}
