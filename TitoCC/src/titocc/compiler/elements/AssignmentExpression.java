package titocc.compiler.elements;

import java.util.HashMap;
import java.util.Map;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Lvalue;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.VirtualRegister;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Expression formed by any of the assignment operators and two operands.
 *
 * <p> EBNF definition:
 *
 * <br> ASSIGNMENT_EXPRESSION = BINARY_EXPRESSION [ASSIGNMENT_OPERATOR ASSIGNMENT_EXPRESSION]
 *
 * <br> ASSIGNMENT_OPERATOR = "=" | "+=" | "*=" | "&=" | "|=" | "^=" | "-=" | "/=" | "%=" | "<<=" |
 * ">>="
 */
public class AssignmentExpression extends Expression
{
	private enum Type
	{
		SIMPLE, COMMUTATIVE, NONCOMMUTATIVE
	};

	/**
	 * Assignment operator with operator mnemonic and operation type.
	 */
	private static class Operator
	{
		public String mnemonic;

		public Type type;

		public Operator(String mnemonic, Type type)
		{
			this.mnemonic = mnemonic;
			this.type = type;
		}
	}

	/**
	 * Map of assignment operators.
	 */
	static final Map<String, Operator> assignmentOperators = new HashMap<String, Operator>()
	{
		{
			put("=", new Operator("", Type.SIMPLE));
			put("+=", new Operator("add", Type.COMMUTATIVE));
			put("*=", new Operator("mul", Type.COMMUTATIVE));
			put("&=", new Operator("and", Type.COMMUTATIVE));
			put("|=", new Operator("or", Type.COMMUTATIVE));
			put("^=", new Operator("xor", Type.COMMUTATIVE));
			put("-=", new Operator("sub", Type.NONCOMMUTATIVE));
			put("/=", new Operator("div", Type.NONCOMMUTATIVE));
			put("%=", new Operator("mod", Type.NONCOMMUTATIVE));
			put("<<=", new Operator("shl", Type.NONCOMMUTATIVE));
			put(">>=", new Operator("shr", Type.NONCOMMUTATIVE));
		}
	};

	/**
	 * Assignment operator.
	 */
	private final Operator operator;

	/**
	 * String representation of the operator.
	 */
	private final String operatorString;

	/**
	 * Left hand side expression.
	 */
	private final Expression left;

	/**
	 * Right hand side expression.
	 */
	private final Expression right;

	/**
	 * Constructs a new AssignmentExpression
	 *
	 * @param operator string representation of the operator
	 * @param left left operand
	 * @param right right operand
	 */
	public AssignmentExpression(String operator, Expression left,
			Expression right, Position position)
	{
		super(position);
		this.operatorString = operator;
		this.operator = assignmentOperators.get(operator);
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the left operand.
	 *
	 * @return left operand
	 */
	public Expression getLeft()
	{
		return left;
	}

	/**
	 * Returns the right operand.
	 *
	 * @return right operand
	 */
	public Expression getRight()
	{
		return right;
	}

	@Override
	public Rvalue compile(IntermediateCompiler ic, Scope scope) throws SyntaxException
	{
		checkTypes(scope);

		// Compile Operator.
		if (operator.type == Type.SIMPLE)
			return compileSimple(ic, scope);
		else if (operator.type == Type.COMMUTATIVE)
			return compileCommutative(ic, scope);
		else
			return compileNoncommutative(ic, scope);
	}

	private Rvalue compileSimple(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		Rvalue rhs = right.compileWithConversion(ic, scope, left.getType(scope).decay());
		Lvalue lhs = left.compileAsLvalue(ic, scope, false);
		ic.emit("store", rhs.getRegister(), "0", lhs.getRegister());
		return rhs;
	}

	private Rvalue compileCommutative(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		Rvalue rhs = right.compile(ic, scope);

		// If operation is POINTER += INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).decay().getIncrementSize();
		if (incSize > 1)
			ic.emit("mul", rhs.getRegister(), "=" + incSize);

		Lvalue lhs = left.compileAsLvalue(ic, scope, false);

		// Because the operation is symmetric, we can use the left operand
		// as the right operand in the assembly instruction, saving one register.
		ic.emit(operator.mnemonic, rhs.getRegister(), "0", lhs.getRegister());
		ic.emit("store", rhs.getRegister(), "0", lhs.getRegister());

		return rhs;
	}

	private Rvalue compileNoncommutative(IntermediateCompiler ic, Scope scope)
			throws SyntaxException
	{
		Rvalue rhs = right.compile(ic, scope);
		Lvalue lhs = left.compileAsLvalue(ic, scope, false);

		// If operation is POINTER -= INTEGER, we need to scale the integer value.
		int incSize = left.getType(scope).decay().getIncrementSize();
		if (incSize > 1)
			ic.emit("mul", rhs.getRegister(), "=" + incSize);

		// Load LHS value to new register and operate on it.
		VirtualRegister retReg = new VirtualRegister();
		ic.emit("load", retReg, "0", lhs.getRegister());
		ic.emit(operator.mnemonic, retReg, rhs.getRegister());

		// Store result back to LHS variable.
		ic.emit("store", retReg, "0", lhs.getRegister());

		return new Rvalue(retReg);
	}

	private void checkTypes(Scope scope) throws SyntaxException
	{
		CType leftType = left.getType(scope).decay();
		CType rightType = right.getType(scope).decay();

		// Compound assignment rules defined in ($6.5.16.2).
		if (operatorString.equals("=")) {
			if (right.isAssignableTo(leftType, scope))
				return;
		} else if (operatorString.equals("+=") || operatorString.equals("-=")) {
			if (leftType.dereference().isObject() && rightType.isInteger())
				return;
			if (leftType.isArithmetic() && rightType.isArithmetic())
				return;
		} else if (operatorString.equals("&=") || operatorString.equals("|=")
				|| operatorString.equals("^=")) {
			if (leftType.isInteger() && rightType.isInteger())
				return;
		} else if (operatorString.equals("%=")) {
			if (leftType.isArithmetic() && rightType.isInteger())
				return;
		} else {
			if (leftType.isArithmetic() && rightType.isArithmetic())
				return;
		}

		throw new SyntaxException("Incompatible operands for operator " + operatorString + ".",
				getPosition());
	}

	@Override
	public CType getType(Scope scope) throws SyntaxException
	{
		return left.getType(scope);
	}

	@Override
	public String toString()
	{
		return "(ASGN_EXPR " + operatorString + " " + left + " " + right + ")";
	}

	/**
	 * Attempts to parse a syntactic assignment expression from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Expression expr = BinaryExpression.parse(tokens);

		if (expr != null) {
			tokens.pushMark();
			Expression right = null;
			String op = tokens.read().toString();
			if (assignmentOperators.containsKey(op))
				right = AssignmentExpression.parse(tokens);

			tokens.popMark(right == null);
			if (right != null)
				expr = new AssignmentExpression(op, expr, right, pos);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
