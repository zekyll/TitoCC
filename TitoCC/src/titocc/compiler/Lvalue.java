package titocc.compiler;

import java.io.IOException;

/**
 * Represents the result of evaluating an expression as lvalue. The purpose is
 * to allow lvalue result to be either a variable reference or a pointer value
 * in a register. However, even the lvalue is just a variable reference, a
 * register still needs to be reserved for it in case the address is later
 * needed in a register. loadAddressToRegister() method can be called to force
 * loading of the address into a register
 */
public class Lvalue
{
	/**
	 * Register reserved for this lvalue.
	 */
	private final Register register;
	/**
	 * Reference to a named object if any.
	 */
	private final String reference;

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
	 * Loads the address of the object in the reserved register. If the address
	 * is already in the reserved register then does nothing.
	 *
	 * @param asm assembler used for emitting the load instruction
	 * @throws IOException if the assembler throws
	 */
	public void loadAddressToRegister(Assembler asm) throws IOException
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
	 * Returns a symbolic reference to the lvalue. If the lvalue is unnamed then
	 * it's referenced through the register (e.g. "0(R2)").
	 *
	 * @return the symbolic reference
	 */
	public String getReference()
	{
		// Titokone has some weird behavior with addressing modes here.
		// One would think that "load R1, @R2" and "store R1, @R2" would access
		// same memory locations, but they don't. But if "0(R2)" is used instead
		// then it works as expected.
		return reference != null ? reference : "0(" + register + ")";
	}
}
