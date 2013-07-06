package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.Vstack;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

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
	 * @param position starting position of the expression
	 */
	public Expression(Position position)
	{
		super(position);
	}

	/**
	 * Generates assembly code for the expression. For non-void expressions the resulting value is
	 * returned on top of the virtual stack.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param vstack virtual stack
	 * @throws SyntaxException if expression contains an error
	 * @throws IOException if assembler throws
	 */
	public abstract void compile(Assembler asm, Scope scope, Vstack vstack)
			throws SyntaxException, IOException;

	/**
	 * Evaluates the expression at compile time if possible.
	 *
	 * @return value of the expression or null if expression cannot be evaluated at compile time
	 * @throws SyntaxException if the expression contains an error
	 */
	public Integer getCompileTimeValue() throws SyntaxException
	{
		return null;
	}

	/**
	 * Generates assembly code for the expression, evaluating it as an lvalue. The resulting lvalue
	 * is returned on top of the virtual stack.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param vstack virtual stack used for giving the operands to the expression and returning
	 * its value
	 * @param addressOf true if the expression appears as the operand of operator &, which disables
	 * the array->pointer decay and function->pointer decay
	 * @throws SyntaxException if expression contains an error
	 * @throws IOException if assembler throws
	 */
	public void compileAsLvalue(Assembler asm, Scope scope, Vstack vstack, boolean addressOf)
			throws SyntaxException, IOException
	{
		throw new SyntaxException("Operation requires an lvalue.", getPosition());
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
		return CommaExpression.parse(tokens);
	}

	/**
	 * Generates code for compile time constant expression. Checks if the expression is compile time
	 * constant by using getCompileTimeValue() and if it is then returns the value on top of the
	 * virtual stack.
	 *
	 * @param asm assembler used for code generation
	 * @param scope scope in which the expression is evaluated
	 * @param vstack virtual stack used for giving the operands to the expression and returning
	 * its value
	 * @return true if compile time constant, otherwise false
	 * @throws IOException if assembler throws
	 * @throws SyntaxException if expression contains an error
	 */
	protected boolean compileConstantExpression(Assembler asm, Scope scope,
			Vstack vstack) throws IOException, SyntaxException
	{
		Integer value = getCompileTimeValue();
		if (value != null) {
			// Use immediate operand if value fits in 16 bits; otherwise allocate a data constant.
			// Load value in first available register.
			if (value < 32768 && value >= -32768)
				vstack.pushSymbolicValue("=" + value);
			else {
				String name = scope.makeGloballyUniqueName("int");
				asm.addLabel(name);
				asm.emit("dc", "" + value);
				vstack.pushSymbolicValue(name);
			}
			return true;
		} else
			return false;
	}

	/**
	 * Returns whether the expression can be assigned to the target type. Target type must be a
	 * decayed type.
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

		// These rules are defined in ($6.5.16.1/1). In addition to these requirements the target
		// expression must be lvalue, which is checked separately in compileAsLvalue().
		if (targetType.isArithmetic() && sourceType.isArithmetic())
			return true;
		if (sourceDeref.equals(targetDeref))
			return true;
		if (targetDeref instanceof VoidType && (sourceDeref.isObject()
				|| sourceDeref.isIncomplete()))
			return true;
		if (sourceDeref instanceof VoidType && (targetDeref.isObject()
				|| targetDeref.isIncomplete()))
			return true;
		if (targetType.isPointer() && sourceType.isInteger()
				&& new Integer(0).equals(getCompileTimeValue()))
			return true;

		return false;
	}

	/**
	 * Throws if the expression cannot be an lvalue based on its type.
	 *
	 * @param scope scope where the expression is evaluated
	 * @throws SyntaxException if array or function type
	 */
	protected void requireLvalueType(Scope scope) throws SyntaxException
	{
		CType resultType = getType(scope); // No type decay
		if (resultType instanceof ArrayType)
			throw new SyntaxException("Array used as an lvalue.", getPosition());
		if (resultType instanceof FunctionType)
			throw new SyntaxException("Function used as an lvalue.", getPosition());
	}
}
