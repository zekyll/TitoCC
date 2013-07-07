package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;
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
	public void compile(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		if (compileConstantExpression(asm, scope, vstack))
			return;

		if (operator.equals("++") || operator.equals("--"))
			compileIncDec(asm, scope, vstack);
		else if (operator.equals("+") || operator.equals("-"))
			compileUnaryPlusMinus(asm, scope, vstack);
		else if (operator.equals("!"))
			compileLogicalNegation(asm, scope, vstack);
		else if (operator.equals("~"))
			compileBitwiseNegation(asm, scope, vstack);
		else if (operator.equals("&"))
			compileAddressOf(asm, scope, vstack);
		else if (operator.equals("*"))
			compileDereference(asm, scope, vstack);
	}

	@Override
	public void compileAsLvalue(Assembler asm, Scope scope, Vstack vstack, boolean addressOf)
			throws IOException, SyntaxException
	{
		// Dereference operator is the only one that can return an lvalue.
		if (!operator.equals("*"))
			throw new SyntaxException("Operation requires an lvalue.", getPosition());

		if (!addressOf)
			requireLvalueType(scope);

		// Operand for * must be a pointer so we just load its value.
		operand.compile(asm, scope, vstack);
		vstack.dereferenceTop(asm);
	}

	private void compileIncDec(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// ($6.5.3.1)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isArithmetic() && !operandType.dereference().isObject()) {
			//TODO arithmetic -> real
			throw new SyntaxException("Operator " + operator
					+ " requires an arithmetic or object pointer type.", getPosition());
		}

		// Allocate 1st register for result value.
		Register retReg = vstack.pushRegisterRvalue(asm);

		// Evaluate operand; load address to 2nd register.
		vstack.enterFrame(); //TODO is this necessary? (retReg not used yet)
		operand.compileAsLvalue(asm, scope, vstack, false);
		vstack.exitFrame(asm);

		boolean inc = operator.equals("++");
		operandType.compileIncDecOperator(asm, scope, vstack, retReg, inc, false, 1);
	}

	private void compileUnaryPlusMinus(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// ($6.5.3.3/1)
		CType operandType = operand.getType(scope).decay();
		if (!operandType.isArithmetic()) {
			throw new SyntaxException("Operator " + operator
					+ " requires an arithmetic type.", getPosition());
		}

		operand.compile(asm, scope, vstack);
		operandType.compileUnaryPlusMinusOperator(asm, scope, vstack, operator.equals("+"));
	}

	private void compileLogicalNegation(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// ($6.5.3.3/1)
		if (!operand.getType(scope).decay().isScalar()) {
			throw new SyntaxException("Operator " + operator
					+ " requires a scalar type.", getPosition());
		}

		// Evaluate operand, push to vstack and convert to boolish.
		operand.compileWithConversion(asm, scope, vstack, CType.BOOLISH);
		Register topReg = vstack.loadTopValue(asm);

		// Compares operand to zero and sets register value according to the result.
		asm.emit("comp", topReg, "=0");
		asm.emit("load", topReg, "=1");
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("jequ", jumpLabel);
		asm.emit("load", topReg, "=0");
		asm.addLabel(jumpLabel);
	}

	private void compileBitwiseNegation(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// ($6.5.3.3/1)
		if (!operand.getType(scope).decay().isInteger()) {
			throw new SyntaxException("Operator " + operator
					+ " requires an integer type.", getPosition());
		}

		operand.compile(asm, scope, vstack);
		Register topReg = vstack.loadTopValue(asm);

		// -1 has representation of all 1 bits (0xFFFFFFFF), and therefore xoring with it gives
		// the bitwise negation.
		asm.emit("xor", topReg, "=-1");
	}

	private void compileAddressOf(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// Load the address of the operand in the first register.
		operand.compileAsLvalue(asm, scope, vstack, true);
		vstack.replaceTopWithAddress(asm);
	}

	private void compileDereference(Assembler asm, Scope scope, Vstack vstack)
			throws IOException, SyntaxException
	{
		// ($6.5.3.2/2)
		if (!operand.getType(scope).decay().isPointer()) {
			throw new SyntaxException("Invalid operand for operator *. Pointer type required.",
					getPosition());
		}

		// Operand must be a pointer; load into register.
		operand.compile(asm, scope, vstack);
		Register topReg = vstack.loadTopValue(asm);

		// Dereference the pointer unless the result type is an array or function!
		CType resultType = getType(scope);
		if (!(resultType instanceof ArrayType) && !resultType.isFunction())
			asm.emit("load", topReg, "@" + topReg.toString()); //TODO vstack.top(0)?
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
