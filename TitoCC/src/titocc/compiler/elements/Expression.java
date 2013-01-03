package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Abstract base for all expressions types.
 */
public abstract class Expression extends CodeElement
{
	/**
	 * Constructs a new Expression object.
	 *
	 * @param line starting line number of the expression
	 * @param column starting column/character of the expression
	 */
	public Expression(int line, int column)
	{
		super(line, column);
	}

	/**
	 * Generates assembly code for the expression. Value of the expression is
	 * returned in the first available register.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param registers available registers, must contain at least one register
	 * and the first one is used for the return value
	 * @throws SyntaxException if expression contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException;

	/**
	 * Generates assembly code for the expression without returning a value.
	 * Behaves like normal compile() but the expression is allowed to have void
	 * type and it is not requierd to return a value.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param registers available registers, must contain at least one register
	 * @throws SyntaxException if expression contains an error
	 * @throws IOException if assembler throws
	 */
	public void compileAsVoid(Assembler asm, Scope scope, Stack<Register> registers)
			throws SyntaxException, IOException
	{
		compile(asm, scope, registers);
	}

	/**
	 * Evaluates the expression at compile time if possible.
	 *
	 * @return value of the expression or null if expression cannot be evaluated
	 * at compile time
	 * @throws SyntaxException if the expression contains an error
	 */
	public Integer getCompileTimeValue() throws SyntaxException
	{
		return null;
	}

	/**
	 * Evaluates the expression as an lvalue. At the moment the only lvalue
	 * expressions are IdentifierExpressions that name variables or parameters,
	 * so this function can just return the assembly reference to that
	 * variable/parameter. However if generalized lvalues are implemented along
	 * with pointers and arrays this function will have to be be replaced with
	 * something like compileAsLvalue() which would return an address in a
	 * register.
	 *
	 * @param scope scope in which the expression is evaluated
	 * @return assembly code reference to an assignable variable/parameter or
	 * null if expression does not evaluate to an lvalue
	 * @throws SyntaxException if expression contains an error
	 */
	public String getLvalueReference(Scope scope) throws SyntaxException
	{
		return null;
	}

	/**
	 * Attempts to evaluates the expression as a function. As with
	 * getLvalueReference() this will have to be replaced with something more
	 * general if function pointers are implemented.
	 *
	 * @param scope in which the expression is evaluated
	 * @return Function object or null if the expression does not name a
	 * function
	 * @throws SyntaxException if expression contains an error
	 */
	public Function getFunction(Scope scope) throws SyntaxException
	{
		return null;
	}

	/**
	 * Parses an expression from token stream.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		return AssignmentExpression.parse(tokens);
	}

	/**
	 * Generates code for compile time constant expression. Checks if the
	 * expression is compile time constant by using getCompileTimeValue() and if
	 * it is then generates code that returns the value in first available
	 * register.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param registers available registers; must contain at least one register
	 * @return true if compile time constant, otherwise false
	 * @throws IOException if assembler throws
	 * @throws SyntaxException if expression contains an error
	 */
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
