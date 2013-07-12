package titocc.compiler.elements;

import java.math.BigInteger;
import java.util.Arrays;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.compiler.types.PointerType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Expression formed by a prefix operator followed by an operand.
 *
 * <p> EBNF definition:
 *
 * <br> PREFIX_EXPRESSION = ("++" | "--" | "+" | "-" | "!" | "~"| "&"| "*") PREFIX_EXPRESSION
 * | POSTFIX_EXPRESSION
 */
public class PrefixExpression extends Expression
{
	/**
	 * List of supported prefix operators.
	 */
	static final String[] prefixOperators = {"++", "--", "+", "-", "!", "~", "&", "*"};

	/**
	 * Operator for this prefix expression.
	 */
	private final String operator;

	/**
	 * Operand expression.
	 */
	private final Expression operand;

	/**
	 * Constructs a PrefixExpression
	 *
	 * @param operator prefix operator as a string
	 * @param operand operand expression
	 * @param position starting position of the prefix expression
	 */
	public PrefixExpression(String operator, Expression operand, Position position)
	{
		super(position);
		this.operator = operator;
		this.operand = operand;
	}

	/**
	 * Returns the operator as a string.
	 *
	 * @return the operator
	 */
	public String getOperator()
	{
		return operator;
	}

	/**
	 * Returns the operand expression.
	 *
	 * @return the operand expression
	 */
	public Expression getOperand()
	{
		return operand;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		Rvalue constVal = compileConstantExpression(ic, scope);
		if (constVal != null)
			return constVal;

		if (operator.equals("++") || operator.equals("--"))
			return compileIncDec(ic, scope);
		else if (operator.equals("+") || operator.equals("-"))
			return compileUnaryPlusMinus(ic, scope);
		else if (operator.equals("!"))
			return compileLogicalNegation(ic, scope);
		else if (operator.equals("~"))
			return compileBitwiseNegation(ic, scope);
		else if (operator.equals("&"))
			return compileAddressOf(ic, scope);
		else if (operator.equals("*"))
			return compileDereference(ic, scope);
		else
			throw new InternalCompilerException("Unknown prefix operator.");
	}

	@Override
	public Lvalue compileAsLvalue(IntermediateCompiler ic, Scope scope, boolean addressOf)
			throws SyntaxException
	{
		// Dereference operator is the only one that can return an lvalue.
		if (!operator.equals("*"))
			throw new SyntaxException("Operation requires an lvalue.", getPosition());

		if (!addressOf)
			requireLvalueType(scope);

		// Operand for * must be a pointer so we just load its value.
		Rvalue ptrVal = operand.compile(ic, scope);
		return new Lvalue(ptrVal.getRegister());
	}

	private Rvalue compileIncDec(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// ($6.5.3.1)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isArithmetic() && !operandType.dereference().isObject()) {
			//TODO arithmetic -> real
			throw new SyntaxException("Operator " + operator
					+ " requires an arithmetic or object pointer type.", getPosition());
		}

		Lvalue val = operand.compileAsLvalue(ic, scope, false);
		boolean inc = operator.equals("++");
		return operandType.compileIncDecOperator(ic, scope, val, inc, false, 1);
	}

	private Rvalue compileUnaryPlusMinus(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// ($6.5.3.3/1)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isArithmetic()) {
			throw new SyntaxException("Operator " + operator
					+ " requires an arithmetic type.", getPosition());
		}

		operandType = operandType.promote();
		Rvalue val = operand.compileWithConversion(ic, scope, operandType);
		return operandType.compileUnaryPlusMinusOperator(ic, scope, val, operator.equals("+"));
	}

	private Rvalue compileLogicalNegation(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// ($6.5.3.3/1)
		if (!operand.getType(scope).decay().isScalar()) {
			throw new SyntaxException("Operator " + operator
					+ " requires a scalar type.", getPosition());
		}

		Rvalue val = operand.compileWithConversion(ic, scope, CType.BOOLISH);

		// Compares operand to zero and sets register value according to the result.
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		ic.emit("jzer", val.getRegister(), jumpLabel);
		ic.emit("load", val.getRegister(), "=1");
		ic.addLabel(jumpLabel);
		ic.emit("xor", val.getRegister(), "=1");

		return val;
	}

	private Rvalue compileBitwiseNegation(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// ($6.5.3.3/1)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isInteger()) {
			throw new SyntaxException("Operator " + operator
					+ " requires an integer type.", getPosition());
		}

		operandType = operandType.promote();
		Rvalue val = operand.compileWithConversion(ic, scope, operandType);
		return operandType.compileUnaryBitwiseNegationOperator(ic, scope, val);
	}

	private Rvalue compileAddressOf(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// Load the address of the operand a register.
		Lvalue lvalue = operand.compileAsLvalue(ic, scope, true);
		return new Rvalue(lvalue.getRegister());
	}

	private Rvalue compileDereference(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		// ($6.5.3.2/2)
		if (!operand.getType(scope).decay().isPointer()) {
			throw new SyntaxException("Invalid operand for operator *. Pointer type required.",
					getPosition());
		}

		// Operand must be a pointer; load into register.
		Rvalue val = operand.compile(ic, scope);

		// Dereference the pointer unless the result type is an array or function!
		CType resultType = getType(scope);
		if (!(resultType instanceof ArrayType) && !resultType.isFunction())
			ic.emit("load", val.getRegister(), "0", val.getRegister());

		return val;
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		CType operandType = operand.getType(scope).decay();

		if (operator.equals("&")) {
			// No decay with operator &. ($6.3.2.1/3-4)
			return new PointerType(operand.getType(scope));
		} else if (operator.equals("*")) {
			if (!operandType.isPointer()) {
				throw new SyntaxException("Invalid operand for operator *. Pointer type required.",
						getPosition());
			}
			return operandType.dereference();
		} else if (operator.equals("!")) {
			return CType.INT;
		} else if (operator.equals("~")) {
			return operandType.promote();
		} else if (operator.equals("+") || operator.equals("-")) {
			return operandType.promote();
		} else { // ++ --
			return operandType;
		}
	}

	@Override
	public Integer getCompileTimeValue() throws SyntaxException
	{
		// Handle unary minus for literals as a special case. Literals need to be non-negative so
		// this is a way of simulating negative literals. Also, because 2147483648 doesn't fit int
		// range, this is necessary for expressing the smallest int value of -2147483648.
		if (operator.equals("-") && operand instanceof IntegerLiteralExpression) {
			((IntegerLiteralExpression) operand).getCompileTimeValue();
			String rawValue = ((IntegerLiteralExpression) operand).getRawValue();
			return new BigInteger("-" + rawValue).intValue();
		}

		// Compile time evaluation of operators + - ~ ! could be implemented here.

		return null;
	}

	@Override
	public String toString()
	{
		return "(PRE_EXPR " + operator + " " + operand + ")";
	}

	/**
	 * Attempts to parse a syntactic prefix expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Expression expr = null;

		String op = tokens.read().toString();
		if (Arrays.asList(prefixOperators).contains(op)) {
			Expression operand = PrefixExpression.parse(tokens);
			if (operand != null)
				expr = new PrefixExpression(op, operand, pos);
		}

		tokens.popMark(expr == null);

		if (expr == null)
			expr = PostfixExpression.parse(tokens);

		return expr;
	}
}
