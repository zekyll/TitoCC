package titocc.compiler;

import java.io.IOException;

public class Lvalue
{
	private Register register;
	private String reference;

	/**
	 * Constructs an unnamed lvalue whose address is stored in a register.
	 *
	 * @param register register that contains the address of the object
	 */
	public Lvalue(Register register)
	{
		this.register = register;
		this.reference = null;
	}

	/**
	 * Constructs an named lvalue that can be accessed by assembly language
	 * reference (e.g. "myVar(fp)").
	 *
	 * @param reservedRegister - register that is reserved for the address
	 * @param reference symbolic reference to the object
	 */
	public Lvalue(Register reservedRegister, String reference)
	{
		this.register = reservedRegister;
		this.reference = reference;
	}

	/**
	 * Loads the address of the object in the reserved register.
	 *
	 * @param asm assembler used for emitting the load instruction
	 * @throws IOException if the assembler throws
	 */
	public void loadToRegister(Assembler asm) throws IOException
	{
		if (reference != null)
			asm.emit("load", register.toString(), "=" + reference);
	}

	/**
	 * Returns the register reserved for the address of the lvalue.
	 *
	 * @return the reserved register
	 */
	public Register getRegister()
	{
		return register;
	}

	/**
	 * Returns a reference to the lvalue. If the lvalue is unnamed then it's
	 * referenced through the register (e.g. @R1).
	 *
	 * @return
	 */
	public String getReference()
	{
		return reference != null ? reference : "0(" + register + ")";
	}
}
