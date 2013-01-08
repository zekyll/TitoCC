package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Lvalue;
import titocc.compiler.Register;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.types.CType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Abstract base for all expressions.
 *
 * <p> EBNF definition:
 *
 * <br> EXPRESSION = ASSIGNMENT_EXPRESSION
 */
public abstract class Expression extends CodeElement
{
	/**
	 * Constructs an Expression.
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
	 * @param regs available registers; must have at least one active register
	 * and the first one is used for return value
	 * @throws SyntaxException if expression contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException;

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
	public Lvalue compileAsLvalue(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		throw new SyntaxException("Operation requires an lvalue.", getLine(), getColumn());
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
	 * Returns the type of the expression.
	 *
	 * @param scope scope in which the expression is evaluated
	 * @return the type
	 * @throws SyntaxException if expression contains errors
	 */
	public abstract CType getType(Scope scope) throws SyntaxException;

	/**
	 * Attempts to parse an expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
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
	 * @param regs available registers; must have at least one active register
	 * and the first one is used for return value
	 * @return true if compile time constant, otherwise false
	 * @throws IOException if assembler throws
	 * @throws SyntaxException if expression contains an error
	 */
	protected boolean compileConstantExpression(Assembler asm, Scope scope,
			Registers regs) throws IOException, SyntaxException
	{
		Integer value = getCompileTimeValue();
		if (value != null) {
			// Use immediate operand if value fits in 16 bits; otherwise allocate
			// a data constant. Load value in first available register.
			if (value < 32768 && value >= -32768)
				asm.emit("load", regs.get(0).toString(), "=" + value);
			else {
				String name = scope.makeGloballyUniqueName("int");
				asm.addLabel(name);
				asm.emit("dc", "" + value);
				asm.emit("load", regs.get(0).toString(), name);
			}
			return true;
		} else
			return false;
	}

	/**
	 * Returns whether the expression can be assigned to the target type.
	 *
	 * @param targetType target type of the assignment
	 * @param scope scope in which the expression is evaluated
	 * @return true if assignment is possible
	 * @throws SyntaxException if expression has errors
	 */
	protected boolean isAssignableTo(CType targetType, Scope scope) throws SyntaxException
	{
		CType sourceType = getType(scope).decay();
		CType sourceDeref = sourceType.dereference();
		CType targetDeref = targetType.dereference();

		if (targetType.isArithmetic() && sourceType.isArithmetic())
			return true;
		if (targetType.isPointer() && sourceDeref.equals(targetDeref))
			return true;
		if (targetType.isPointer() && sourceDeref.isValid()
				&& (targetDeref instanceof VoidType || sourceDeref instanceof VoidType))
			return true;

		if (targetType.isPointer() && sourceType.isInteger()
				&& new Integer(0).equals(getCompileTimeValue()))
			return true;

		return false;
	}
}
