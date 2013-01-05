package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Expression that evaluates a named variable, parameter or function.
 *
 * <p> EBNF definition:
 *
 * <br> IDENTIFIER_EXPRESSION = IDENTIFIER
 */
public class IdentifierExpression extends Expression
{
	private String identifier;

	/**
	 * Construcs an IdentifierExpression.
	 *
	 * @param identifier name of the object or function
	 * @param line starting line number of the identifier expression
	 * @param column starting column/character of the identifier expression
	 */
	public IdentifierExpression(String identifier, int line, int column)
	{
		super(line, column);
		this.identifier = identifier;
	}

	/**
	 * Returns the identifier.
	 *
	 * @return the identifier
	 */
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		// Load value to first available register
		asm.emit("load", registers.peek().toString(), getLvalueReference(scope));
	}

	@Override
	public String getLvalueReference(Scope scope) throws SyntaxException
	{
		Symbol symbol = scope.find(identifier);
		if (symbol == null)
			throw new SyntaxException("Undeclared identifier \"" + identifier + "\".", getLine(), getColumn());
		if (symbol instanceof Function)
			throw new SyntaxException("Identifier \"" + identifier + "\" is not a variable.", getLine(), getColumn());

		return symbol.getReference();
	}

	@Override
	public Function getFunction(Scope scope) throws SyntaxException
	{
		Symbol symbol = scope.find(identifier);
		if (symbol == null)
			throw new SyntaxException("Undeclared identifier \"" + identifier + "\".", getLine(), getColumn());
		if (!(symbol instanceof Function))
			throw new SyntaxException("Identifier \"" + identifier + "\" is not a function.", getLine(), getColumn());

		return (Function) symbol;
	}

	@Override
	public String toString()
	{
		return "(ID_EXPR " + identifier + ")";
	}

	/**
	 * Attempts to parse an identifier expression from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static IdentifierExpression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		IdentifierExpression idExpr = null;

		Token token = tokens.read();
		if (token instanceof IdentifierToken)
			idExpr = new IdentifierExpression(token.toString(), line, column);

		tokens.popMark(idExpr == null);
		return idExpr;
	}
}
