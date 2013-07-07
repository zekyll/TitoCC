package titocc.compiler.types;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import titocc.compiler.Assembler;
import titocc.compiler.InternalCompilerException;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Vstack;

/**
 * 32-bit signed integer type. Implemented on TTK-91 using one 32-bit machine byte. The standard
 * integer types implemented using this representation are char, signed char, short int, int and
 * long int.
 */
class Int32Type extends IntegerType
{
	static final Map<String, String> instructions = new HashMap<String, String>()
	{
		{
			put("||", "jnzer");
			put("&&", "jzer");
			put("|", "or");
			put("^", "xor");
			put("&", "and");
			put("==", "jequ");
			put("!=", "jnequ");
			put("<", "jles");
			put("<=", "jngre");
			put(">", "jgre");
			put(">=", "jnles");
			put("<<", "shl");
			put(">>", "shr");
			put("+", "add");
			put("-", "sub");
			put("*", "mul");
			put("/", "div");
			put("%", "mod");
		}
	};

	/**
	 * Constructs an Int32Type.
	 *
	 * @param rank rank
	 * @param tag tag
	 */
	public Int32Type(int rank, int tag)
	{
		super(rank, true, tag);
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public CType toUnsigned()
	{
		return new Uint32Type(getRank(), tag);
	}

	@Override
	public void compileConversion(Assembler asm, Scope scope, Vstack vstack, CType targetType)
			throws IOException
	{
		if (targetType.equals(CType.BOOLISH) || targetType instanceof Int32Type
				|| targetType instanceof Uint32Type || targetType instanceof PointerType) {
			// No-op.
		} else
			super.compileConversion(asm, scope, vstack, targetType);
	}

	@Override
	public void compileBinaryBitwiseOperator(Assembler asm, Scope scope, Vstack vstack,
			Register leftReg, String operator) throws IOException
	{
		asm.emit(instructions.get(operator), leftReg, vstack.top(0));
		vstack.pop();
	}

	@Override
	public void compileBinaryComparisonOperator(Assembler asm, Scope scope, Vstack vstack,
			Register leftReg, String operator) throws IOException
	{
		String jumpLabel = scope.makeGloballyUniqueName("lbl");
		asm.emit("comp", leftReg, vstack.top(0));
		asm.emit("load", leftReg, "=1");
		asm.emit(instructions.get(operator), leftReg, jumpLabel);
		asm.emit("load", leftReg, "=0");
		asm.addLabel(jumpLabel);
		vstack.pop();
	}

	@Override
	public void compileBinaryShiftOperator(Assembler asm, Scope scope, Vstack vstack,
			Register leftReg, String operator) throws IOException
	{
		asm.emit(instructions.get(operator), leftReg, vstack.top(0));
		vstack.pop();
	}

	@Override
	public void compileBinaryArithmeticOperator(Assembler asm, Scope scope, Vstack vstack,
			Register leftReg, String operator) throws IOException
	{
		asm.emit(instructions.get(operator), leftReg, vstack.top(0));
		vstack.pop();
	}

	@Override
	public void compileIncDecOperator(Assembler asm, Scope scope, Vstack vstack,
			Register retReg, boolean inc, boolean postfix, int incSize) throws IOException
	{
		// Load value to 1st register.
		asm.emit("load", retReg, vstack.top(0));

		// Modify and write back the value.
		asm.emit(inc ? "add" : "sub", retReg, "=" + incSize);
		asm.emit("store", retReg, vstack.top(0));

		// Deallocate 2nd register.
		vstack.pop();

		// Postfix operator must return the old value.
		if (postfix)
			asm.emit(inc ? "sub" : "add", retReg, "=" + incSize);
	}

	@Override
	public void compileUnaryPlusMinusOperator(Assembler asm, Scope scope, Vstack vstack,
			boolean plus) throws IOException
	{
		// Unary minus is no-op;
		if (plus)
			return;

		// Load operand to register.
		Register topReg = vstack.loadTopValue(asm);

		// Negative in two's complement: negate all bits and add 1. For unary plus do nothing.
		asm.emit("xor", topReg, "=-1");
		asm.emit("add", topReg, "=1");
	}
}
