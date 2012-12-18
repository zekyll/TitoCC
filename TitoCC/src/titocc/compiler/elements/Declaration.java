package titocc.compiler.elements;

import titocc.tokenizer.TokenStream;

public abstract class Declaration extends CodeElement
{
	public Declaration(int line, int column)
	{
		super(line, column);
	}

	public static Declaration parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();

		Declaration declaration = VariableDeclaration.parse(tokens);

		if (declaration == null)
			declaration = Function.parse(tokens);

		tokens.popMark(declaration == null);
		return declaration;
	}
}
