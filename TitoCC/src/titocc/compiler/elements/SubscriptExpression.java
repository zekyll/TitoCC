package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Subscript to an array. Takes an array expression and an integer typed
 * subscript expression. C standard actually requires that their order doesn't
 * matter. E.g. myArray[2] is the same as 2[myArray].
 *
 * <p> EBNF definition:
 *
 * <br> SUBSCRIPT_EXPRESSION = POSTFIX_EXPRESSION "[" EXPRESSION "]"
 */
public class SubscriptExpression extends Expression
{
	private Expression array, subscript;

	/**
	 * Constructs a SubscriptExpression.
	 *
	 * @param array the left side expression ("array")
	 * @param subscript expression inside square brackets
	 * @param line starting line number of the subscript expression
	 * @param column starting column/character of the subscript expression
	 */
	public SubscriptExpression(Expression array, Expression subscript, int line, int column)
	{
		super(line, column);
		this.array = array;
		this.subscript = subscript;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return getActualArrayOperand(scope).getType(scope).dereference();
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		compile(asm, scope, regs, false);
	}

	@Override
	public Lvalue compileAsLvalue(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		compile(asm, scope, regs, true);
		return new Lvalue(regs.get(0));
	}

	private void compile(Assembler asm, Scope scope, Registers regs, boolean lvalue)
			throws SyntaxException, IOException
	{
		// Standard allows the operands to be switched so get the actual operands.
		Expression actualArrayOperand = getActualArrayOperand(scope);
		Expression actualSubscriptOperand = getActualSubscriptOperand(scope);

		// Evaluate array expression in first register.
		actualArrayOperand.compile(asm, scope, regs);

		// Allocate second register and evaluate subscript in it.
		regs.allocate(asm);
		regs.removeFirst();
		actualSubscriptOperand.compile(asm, scope, regs);
		regs.addFirst();

		// If increment size > 1 then multiply subscript.
		int incSize = actualArrayOperand.getType(scope).getIncrementSize();
		if (incSize != 1)
			asm.emit("mul", regs.get(1).toString(), "=" + incSize);

		// Add subscript to the array pointer. Dereference the result if lvalue
		// is not explicitly requested and result is not an array.
		asm.emit("add", regs.get(0).toString(), regs.get(1).toString());
		if (!lvalue && !(getType(scope) instanceof ArrayType))
			asm.emit("load", regs.get(0).toString(), "@" + regs.get(0).toString());

		// Deallocate second register.
		regs.deallocate(asm);
	}

	private Expression getActualArrayOperand(Scope scope) throws SyntaxException
	{
		if (array.getType(scope).dereference().isObject())
			return array;
		else if (subscript.getType(scope).dereference().isObject())
			return subscript;
		else
			throw new SyntaxException("Operator [] requires an object pointer or an array.", getLine(), getColumn());
	}

	private Expression getActualSubscriptOperand(Scope scope) throws SyntaxException
	{
		if (subscript.getType(scope).isInteger())
			return subscript;
		else if (array.getType(scope).isInteger())
			return array;
		else
			throw new SyntaxException("Operator [] requires an integer operand.", getLine(), getColumn());
	}

	@Override
	public String toString()
	{
		return "(SUBSCR_EXPR " + array + " " + subscript + ")";
	}

	/**
	 * Attempts to parse a subscript expression from token stream, given the
	 * first operand of the expression. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param firstOperand preparsed array expression
	 * @param tokens source token stream
	 * @return SubscriptExpression object or null if tokens don't form a valid
	 * subscript expression
	 */
	public static SubscriptExpression parse(Expression firstOperand, TokenStream tokens)
	{
		tokens.pushMark();
		SubscriptExpression expr = null;

		if (tokens.read().toString().equals("[")) {
			Expression subscript = Expression.parse(tokens);
			if (subscript != null && tokens.read().toString().equals("]"))
				expr = new SubscriptExpression(firstOperand, subscript,
						firstOperand.getLine(), firstOperand.getColumn());
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
