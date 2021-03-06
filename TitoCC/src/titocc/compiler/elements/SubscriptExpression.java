package titocc.compiler.elements;

import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Subscript to an array. Takes an array expression and an integer typed subscript expression. C
 * standard actually requires that their order doesn't matter. E.g. myArray[2] is the same as
 * 2[myArray].
 *
 * <p> EBNF definition:
 *
 * <br> SUBSCRIPT_EXPRESSION = POSTFIX_EXPRESSION "[" EXPRESSION "]"
 */
public class SubscriptExpression extends Expression
{
	/**
	 * Array operand.
	 */
	private final Expression array;

	/**
	 * Subscript operand.
	 */
	private final Expression subscript;

	/**
	 * Constructs a SubscriptExpression.
	 *
	 * @param array the left side expression ("array")
	 * @param subscript expression inside square brackets
	 * @param position starting position of the subscript expression
	 */
	public SubscriptExpression(Expression array, Expression subscript, Position position)
	{
		super(position);
		this.array = array;
		this.subscript = subscript;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return getActualArrayOperand(scope).getType(scope).decay().dereference();
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		return compile(ic, scope, false);
	}

	@Override
	public Lvalue compileAsLvalue(IntermediateCompiler ic, Scope scope, boolean addressOf)
			throws SyntaxException
	{
		if (!addressOf)
			requireLvalueType(scope);

		Rvalue addrVal = compile(ic, scope, true);
		return new Lvalue(addrVal.getRegister());
	}

	private Rvalue compile(IntermediateCompiler ic, Scope scope, boolean lvalue)
			throws SyntaxException
	{
		// Standard allows the operands to be switched so get the actual operands.
		Expression actualArrayOperand = getActualArrayOperand(scope);
		Expression actualSubscriptOperand = getActualSubscriptOperand(scope);

		Rvalue arrayVal = actualArrayOperand.compile(ic, scope);
		Rvalue subscriptVal = actualSubscriptOperand.compileWithConversion(ic, scope,
				CType.PTRDIFF_T);

		// If increment size > 1 then multiply subscript.
		int incSize = actualArrayOperand.getType(scope).decay().getIncrementSize();
		if (incSize != 1)
			ic.emit("mul", subscriptVal.getRegister(), "=" + incSize);

		// Add subscript to the array pointer.
		ic.emit("add", arrayVal.getRegister(), subscriptVal.getRegister());

		// Dereference the result if lvalue is not explicitly requested and result is not an array
		// or function.
		CType resultType = getType(scope);
		if (!lvalue && !(resultType instanceof ArrayType) && !resultType.isFunction())
			ic.emit("load", arrayVal.getRegister(), "0", arrayVal.getRegister());

		return arrayVal;
	}

	private Expression getActualArrayOperand(Scope scope) throws SyntaxException
	{
		// ($6.5.2.1/1)
		if (array.getType(scope).decay().dereference().isObject())
			return array;
		else if (subscript.getType(scope).decay().dereference().isObject())
			return subscript;
		else
			throw new SyntaxException("Operator [] requires an object pointer.", getPosition());
	}

	private Expression getActualSubscriptOperand(Scope scope) throws SyntaxException
	{
		// ($6.5.2.1/1)
		if (subscript.getType(scope).decay().isInteger())
			return subscript;
		else if (array.getType(scope).decay().isInteger())
			return array;
		else
			throw new SyntaxException("Operator [] requires an integer operand.", getPosition());
	}

	@Override
	public String toString()
	{
		return "(SUBSCR_EXPR " + array + " " + subscript + ")";
	}

	/**
	 * Attempts to parse a subscript expression from token stream, given the first operand of the
	 * expression. If parsing fails the stream is reset to its initial position.
	 *
	 * @param firstOperand preparsed array expression
	 * @param tokens source token stream
	 * @return SubscriptExpression object or null if tokens don't form a valid subscript expression
	 */
	public static SubscriptExpression parse(Expression firstOperand, TokenStream tokens)
	{
		tokens.pushMark();
		SubscriptExpression expr = null;

		if (tokens.read().toString().equals("[")) {
			Expression subscript = Expression.parse(tokens);
			if (subscript != null && tokens.read().toString().equals("]"))
				expr = new SubscriptExpression(firstOperand, subscript, firstOperand.getPosition());
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
