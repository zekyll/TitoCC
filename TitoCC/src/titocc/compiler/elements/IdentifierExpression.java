package titocc.compiler.elements;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
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
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		Symbol symbol = findSymbol(scope);
		if (!symbol.getType().isObject() && !symbol.getType().isFunction()) {
			throw new SyntaxException("Identifier \"" + identifier
					+ "\" is not an object or function.", getPosition());
		}

		// Load value to register (or address if we have an array/function).
		VirtualRegister retReg = new VirtualRegister();
		if (symbol.getType() instanceof ArrayType || symbol.getType().isFunction())
			ic.emit("load", retReg, symbol.getRhsOperand(false));
		else
			ic.emit("load", retReg, symbol.getRhsOperand(true));

		return new Rvalue(retReg);
	}

	@Override
	public Lvalue compileAsLvalue(IntermediateCompiler ic, Scope scope, boolean addressOf)
			throws SyntaxException
	{
		Symbol symbol = findSymbol(scope);

		if (!addressOf)
			requireLvalueType(scope);

		VirtualRegister retReg = new VirtualRegister();
		ic.emit("load", retReg, symbol.getRhsOperand(false));

		return new Lvalue(retReg);
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
