package titocc.compiler;

import java.io.IOException;

/**
 * Represents a temporary value in the virtual stack that is the result of an expression. There
 * are 4 types of temporary values:
 * <br> 1) Symbolic lvalues that refer to a variable or function. ("x(fp)", "f")
 * <br> 2) Symbolic rvalues that hold an integer/address literal. ("=13", "=x(fp)")
 * <br> 3) Register rvalues, which are values contained in one or more registers and have no
 * variables associated with them.
 * <br> 4) Register lvalues, which are basically an address in a single register, allowing access
 * to an actual memory object by dereferencing the pointer in the register.
 *
 * <br> The purpose is to delay code generation for RHS operands of instructions as long as
 * possible, so that fewer instructions are needed. E.g. instead of loading variable address
 * in register the variable name can be used directly: "load R2, =myvar; store R1, 0(R2)" becomes
 * simply "store R1, myvar". Or avoiding loading literals: "load R2, =13; add R1, R2" can be
 * optimized to "add R1, =13".
 *
 * <br> LoadValue() can be called to force the value to be loaded into a register, when that value
 * is needed as the LHS operand of an instruction. Deallocate() must always be called when the
 * TmpValue is no longer used to free allocated registers if any.
 *
 * <br> Representations of different value types and operations on them:
 * <code>
 *              ref   vreg  areg  lvalue   getRhs  loadVal?   replaceWithAddr
 *                                                            ref    vreg areg
 * Global var:  x     -     -     yes      x       yes        =x     -    -
 * Local var:   x(fp) -     -     yes      x(fp)   yes        =x(fp) -    -
 * Parameter:   x(fp) -     -     yes      x(fp)   yes        =x(fp) -    -
 * Array:       x     -     -     (yes)    x       illegal    =x     -    -
 * Array decay: =x    -     -     no       =x      yes        illegal
 * Function:    f     -     -     (yes)    f       illegal    =f     -    -
 * Func decay:  =f    -     -     no       f       yes        illegal
 * Literal:     =13   -     -     no       =13     yes        illegal
 * Reg. rvalue: -     Rx    -     no       Rx      no-op      illegal
 * Reg. lvalue: -     -     Rx    yes      0(Rx)   yes        -      Rx   -
 * //TODO structs, register storage class
 * </code>
 */
public class TmpValue
{
	/**
	 * Register used by a register rvalue.
	 */
	private Register valueReg;

	/**
	 * Register used by a register lvalue, containing the address of an object.
	 */
	private Register addressReg;

	/**
	 * Symbolic symbolicValue to a named object etc, or an integer/address literal ("x", "=13",
	 * "=x").
	 */
	private String symbolicValue;

	/**
	 * Constructs a new temporary value. The type of the value is determined by the non-null
	 * argument.
	 *
	 * @param symbolicValue symbolic symbolicValue to value or null if not a symbolic value
	 * @param valueRegister register containing the value or null if not a register rvalue
	 * @param addressRegister register containing an address or null if not a register lvalue
	 */
	public TmpValue(String symbolicValue, Register valueRegister, Register addressRegister)
	{
		this.symbolicValue = symbolicValue;
		this.valueReg = valueRegister;
		this.addressReg = addressRegister;
		checkValidity();
	}

	/**
	 * Loads a symbolic value or register lvalue into a register. The value is turned into a
	 * register rvalue and a register is allocated for it. If this value already is a register
	 * rvalue then the function does nothing.
	 *
	 * @param regs register allocator
	 * @param asm assembler used for emitting possible load instruction
	 * @return the register in which the value is loaded
	 */
	public Register loadValue(RegisterAllocator regs, Assembler asm) throws IOException
	{
		checkValidity();

		if (valueReg == null) {
			valueReg = addressReg == null ? regs.allocate(asm) : addressReg;
			asm.emit("load", valueReg, getRhsOperand());
			symbolicValue = null;
			addressReg = null;
		}
		return valueReg;
	}

	/**
	 * Replaces an lvalue with its address, turning it into a rvalue. Only valid for lvalues.
	 *
	 * @param regs register allocator
	 * @param asm assembler used for emitting the load instruction
	 */
	public void replaceWithAddress(RegisterAllocator regs, Assembler asm) throws IOException
	{
		checkValidity();

		if (valueReg != null)
			throw new InternalCompilerException("Taking address of a register rvalue.");
		else if (symbolicValue != null) {
			if (symbolicValue.startsWith("="))
				throw new InternalCompilerException("Taking address of a symbolic rvalue.");
			symbolicValue = "=" + symbolicValue;
		} else {
			valueReg = addressReg;
			addressReg = null;
		}
	}

	/**
	 * Turns a pointer value into a register lvalue. If necessary, allocates a register for the
	 * address.
	 *
	 * @param regs register allocator
	 * @param asm assembler used for emitting the load instruction
	 */
	public void dereference(RegisterAllocator regs, Assembler asm) throws IOException
	{
		checkValidity();
		//TODO use @ for symbolic values?

		if (valueReg == null)
			loadValue(regs, asm);

		addressReg = valueReg;
		valueReg = null;
		symbolicValue = null;
	}

	/**
	 * Deallocates any registers used by this temporary value.
	 *
	 * @param regs register allocator
	 */
	public void deallocate(RegisterAllocator regs) throws IOException
	{
		checkValidity();

		if (valueReg != null) {
			regs.deallocate();
			valueReg = null;
		}
		if (addressReg != null) {
			regs.deallocate();
			addressReg = null;
		}
	}

	/**
	 * Returns a reference to the value that can be used as the RHS operand of an instruction. For
	 * non-symbolic values the value is referenced through the register (i.e. "R1" for register
	 * rvalues or "0(R1)" for register lvalues).
	 *
	 * @return the symbolic value
	 */
	public String getRhsOperand()
	{
		if (symbolicValue != null)
			return symbolicValue;
		else
			return addressReg != null ? "0(" + addressReg + ")" : valueReg.toString();
	}

	private void checkValidity()
	{
		if ((symbolicValue != null ? 1 : 0) + (valueReg != null ? 1 : 0)
				+ (addressReg != null ? 1 : 0) != 1)
			throw new InternalCompilerException("Invalid temporary value.");
	}
}
