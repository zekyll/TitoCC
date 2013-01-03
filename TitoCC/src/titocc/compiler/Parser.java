package titocc.compiler;

import java.util.List;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Main parser class. Just a wrapper class and the actual work is done by the
 * CodeElement classes. The parser takes a list of tokens and produces a code
 * element tree structure where TranslationUnit is the root element.
 */
public class Parser
{
	private TokenStream tokens;

	/**
	 * Constructs the parser object using a list of tokens.
	 *
	 * @param tokens list of input tokens created by Tokenizer class.
	 */
	public Parser(List<Token> tokens)
	{
		this.tokens = new TokenStream(tokens);
	}

	/**
	 *
	 * @return TranslationUnit object which is the top level element in the code
	 * element hierarchy
	 * @throws SyntaxException if the source code has errors
	 */
	public TranslationUnit parse() throws SyntaxException
	{
		TranslationUnit trUnit = TranslationUnit.parse(tokens);
		if (trUnit == null) {
			throw new SyntaxException("Parser error.", -1, -1);
		}
		return trUnit;
	}
}
