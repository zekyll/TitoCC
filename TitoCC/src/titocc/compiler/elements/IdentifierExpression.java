package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.Vstack;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

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
	 * @param position starting position of the identifier expression
	 */
	public IdentifierExpression(String identifier, Position position)
	{
		super(position);
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
	public void compile(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException
	{
		Symbol symbol = findSymbol(scope);
		if (!symbol.getType().isObject() && !symbol.getType().isFunction()) {
			throw new SyntaxException("Identifier \"" + identifier
					+ "\" is not an object or function.", getPosition());
		}

		// Load value to first register (or address if we have an array/function).
		if (symbol.getType() instanceof ArrayType || symbol.getType().isFunction())
			vstack.pushSymbolicValue("=" + symbol.getReference());
		else
			vstack.pushSymbolicValue(symbol.getReference());
	}

	@Override
	public void compileAsLvalue(Assembler asm, Scope scope, Vstack vstack, boolean addressOf)
			throws SyntaxException, IOException
	{
		Symbol symbol = findSymbol(scope);

		if (!addressOf)
			requireLvalueType(scope);

		vstack.pushSymbolicValue(symbol.getReference());
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return findSymbol(scope).getType(); // No decay since we want to return the original type.
	}

	@Override
	public String toString()
	{
		return "(ID_EXPR " + identifier + ")";
	}

	private Symbol findSymbol(Scope scope) throws SyntaxException
	{
		Symbol symbol = scope.find(identifier);
		if (symbol == null) {
			throw new SyntaxException("Undeclared identifier \"" + identifier + "\".",
					getPosition());
		}
		return symbol;
	}

	/**
	 * Attempts to parse an identifier expression from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static IdentifierExpression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		IdentifierExpression idExpr = null;

		Token token = tokens.read();
		if (token instanceof IdentifierToken)
			idExpr = new IdentifierExpression(token.toString(), pos);

		tokens.popMark(idExpr == null);
		return idExpr;
	}
}
