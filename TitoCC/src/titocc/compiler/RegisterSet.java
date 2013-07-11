package titocc.compiler;

import java.util.EnumSet;

/**
 * Set of register available for register allcator. This includes general purpose registers R1-R4,
 * but not R0 and R5 which are used as auxiliary registers for loading and storing spilled values.
 */
public class RegisterSet
{
	/**
	 * Currently available registers.
	 */
	private EnumSet<Register> available = EnumSet.of(Register.R1, Register.R2, Register.R3,
			Register.R4);

	/**
	 * Allocates a new register.
	 *
	 * @return the allocated register or null if no registers are available.
	 */
	public Register allocate()
	{
		if (available.isEmpty())
			return null;
		Register reg = available.iterator().next();
		available.remove(reg);
		return reg;
	}

	/**
	 * Deallocates the given register.
	 */
	public void deallocate(Register reg)
	{
		if (!available.add(reg))
			throw new InternalCompilerException("Deallocating unallocated register.");
	}
}
