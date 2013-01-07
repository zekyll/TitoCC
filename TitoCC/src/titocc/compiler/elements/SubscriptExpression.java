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

public class SubscriptExpression extends Expression
{
	private Expression array, subscript;

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
		if (isConvertableToObjectPointer(array, scope))
			return array;
		else if (isConvertableToObjectPointer(subscript, scope))
			return subscript;
		else
			throw new SyntaxException("Operator [] requires an array or object pointer.", getLine(), getColumn());
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

	private boolean isConvertableToObjectPointer(Expression expr, Scope scope)
			throws SyntaxException
	{
		CType derefType = expr.getType(scope).dereference();
		return derefType != null && derefType.isObject();
	}

	@Override
	public Lvalue compileAsLvalue(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		compile(asm, scope, regs, true);
		return new Lvalue(regs.get(0));
	}

	@Override
	public String toString()
	{
		return "(SUBSCR_EXPR " + array + " " + subscript + ")";
	}

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
