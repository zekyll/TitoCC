package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

public abstract class Expression extends CodeElement
{
	public Expression(int line, int column)
	{
		super(line, column);
	}

	public Integer getCompileTimeValue() throws SyntaxException
	{
		return null;
	}

	public String getLvalueReference(Scope scope) throws SyntaxException
	{
		return null;
	}

	public static Expression parse(TokenStream tokens)
	{
		return AssignmentExpression.parse(tokens);
	}

	protected boolean compileConstantExpression(Assembler asm, Scope scope,
			Stack<Register> registers) throws IOException, SyntaxException
	{
		Integer value = getCompileTimeValue();
		if (value != null) {
			// Use immediate operand if value fits in 16 bits; otherwise allocate
			// a data constant. Load value in first available register.
			if (value < 32768 && value >= -32768)
				asm.emit("load", registers.peek().toString(), "=" + value);
			else {
				String name = scope.makeGloballyUniqueName("int");
				asm.addLabel(name);
				asm.emit("dc", "" + value);
				asm.emit("load", registers.peek().toString(), name);
			}
			return true;
		} else
			return false;
	}
}
