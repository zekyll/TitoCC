package titocc.compiler.elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.Assembler;
import titocc.compiler.Register;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;

/**
 * Expression formed by any of the assignment operators and two operands.
 *
 * <p> EBNF definition:
 *
 * <br> ASSIGNMENT_EXPRESSION = BINARY_EXPRESSION [ASSIGNMENT_OPERATOR
 * ASSIGNMENT_EXPRESSION]
 *
 * <br> ASSIGNMENT_OPERATOR = "=" | "+=" | "*=" | "&=" | "|=" | "^=" | "-=" |
 * "/=" | "%=" | "<<=" | ">>="
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
	private Operator operator;
	private String operatorString;
	private Expression left, right;

	/**
	 * Constructs a new AssignmentExpression
	 *
	 * @param operator string representation of the operator
	 * @param left left operand
	 * @param right right operand
	 * @param line starting line number of the assignment expression
	 * @param column starting column/character of the assignment expression
	 */
	public AssignmentExpression(String operator, Expression left,
			Expression right, int line, int column)
	{
		super(line, column);
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Compile LHS and get reference to the variable.
		String leftRef = compileLeft(asm, scope, regs);

		// Compile RHS and the assignment operation.
		if (operator.type == Type.SIMPLE)
			compileSimple(asm, scope, regs, leftRef);
		else if (operator.type == Type.COMMUTATIVE)
			compileCommutative(asm, scope, regs, leftRef);
		else
			compileNoncommutative(asm, scope, regs, leftRef);
	}

	private String compileLeft(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		// Currently assumes that the return value can always be reduced to
		// variable name, so we can do a simple compile time evaluation. This
		// will have to be changed if arrays, pointers and indirection operator
		// are implemented.
		String leftRef = left.getLvalueReference(scope);
		if (leftRef == null)
			throw new SyntaxException("Left side cannot be assigned to.", getLine(), getColumn());
		//left.compile(asm, scope, registers);
		return leftRef;
	}

	private void compileSimple(Assembler asm, Scope scope, Registers regs,
			String leftRef) throws SyntaxException, IOException
	{
		// Load RHS in the first register and stores it in the LHS variable.
		right.compile(asm, scope, regs);
		asm.emit("store", regs.get(0).toString(), leftRef);
	}

	private void compileCommutative(Assembler asm, Scope scope, Registers regs,
			String leftRef) throws SyntaxException, IOException
	{
		// Load RHS in first register.
		right.compile(asm, scope, regs);

		// Because the operation is symmetric, we can use the left operand
		// as the right operand in the assembly instruction, saving one register.
		asm.emit(operator.mnemonic, regs.get(0).toString(), leftRef);
		asm.emit("store", regs.get(0).toString(), leftRef);
	}

	private void compileNoncommutative(Assembler asm, Scope scope, Registers regs,
			String leftRef) throws SyntaxException, IOException
	{
		// Allocate a second register for RHS.
		regs.allocate(asm);

		// Load RHS in the second register.
		regs.removeFirst();
		right.compile(asm, scope, regs);
		regs.addFirst();

		// Load LHS in the first register and operate on it.
		asm.emit("load", regs.get(0).toString(), leftRef);
		asm.emit(operator.mnemonic, regs.get(0).toString(), regs.get(1).toString());

		// Store result to LHS variable.
		asm.emit("store", regs.get(0).toString(), leftRef);

		// Deallocate the second register.
		regs.deallocate(asm);
	}

	@Override
	public String toString()
	{
		return "(ASGN_EXPR " + operatorString + " " + left + " " + right + ")";
	}

	/**
	 * Attempts to parse a syntactic assignment expression from token stream. If
	 * parsing fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Expression object or null if tokens don't form a valid expression
	 */
	public static Expression parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
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
				expr = new AssignmentExpression(op, expr, right, line, column);
		}

		tokens.popMark(expr == null);
		return expr;
	}
}
