package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Register;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
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

	public Expression getArray()
	{
		return array;
	}

	public Expression getSubscript()
	{
		return subscript;
	}

//	@Override
//	public CType getType()
//	{
//		throw new UnsupportedOperationException();
//	}
	@Override
	public String toString()
	{
		return "(SUBSCR_EXPR " + array + " " + subscript + ")";
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
		// Evaluate array expression in first register.
		array.compile(asm, scope, regs);

		// Evaluate subscript in second register.
		regs.allocate(asm);
		regs.removeFirst();
		subscript.compile(asm, scope, regs);
		regs.addFirst();

		// Add subscript to the array pointer and dereference unless lvalue required.
		asm.emit("add", regs.get(0).toString(), regs.get(1).toString());
		if (!lvalue)
			asm.emit("load", regs.get(0).toString(), "@" + regs.get(0).toString());

		// Pop register.
		regs.deallocate(asm);
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
