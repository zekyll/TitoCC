package titocc.compiler;

/**
 * Stores all the information about right hand side of TTK-91 instruction.
 */
public class RhsOperand
{
	/**
	 * Real addressing mode (0-2) that takes account the reduced addressing mode in call, jump etc.
	 */
	final int addrMode;

	/**
	 * Immediate value. Can be a symbolic or integer value or null.
	 */
	final String immediateValue;

	/**
	 * Virtual register of null if not used.
	 */
	final VirtualRegister register;

	/**
	 * Constructs an RHS operand.
	 *
	 * @param addrMode real addressing mode, 0-2
	 * @param immediateValue immediate value; null if not used
	 * @param register virtual register; null if not used
	 */
	public RhsOperand(int addrMode, String immediateValue, VirtualRegister register)
	{
		this.addrMode = addrMode;
		this.immediateValue = immediateValue;
		this.register = register;
	}
}
