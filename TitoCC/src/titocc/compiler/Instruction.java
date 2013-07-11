package titocc.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Intermediate presentation of a machine instruction, using virtual registers.
 */
class Instruction
{
	/**
	 * Set of all pseudo-instructions.
	 */
	static final Set<String> pseudoInstructions = new HashSet<String>(
			Arrays.asList("dc", "ds", "equ"));

	/**
	 * Optional label.
	 */
	String label = null;

	/**
	 * Mnemonic. ("load", "add" etc.)
	 */
	String mnemonic = null;

	/**
	 * The operand of single-operand pseudo instruction ("ds", "dc", "equ").
	 */
	int pseudoOperand = 0;

	/**
	 * Left operand.
	 */
	VirtualRegister leftReg = null;

	/**
	 * Right register operand.
	 */
	VirtualRegister rightReg = null;

	/**
	 * Right immediate operand, including addressing mode.
	 */
	String immediateOperand = null;

	/**
	 * Constructs an instruction that has no operands ("nop").
	 *
	 * @param label optional label; null if not used
	 * @param mnemonic mnemonic
	 */
	Instruction(String label, String mnemonic)
	{
		if (!mnemonic.equals("nop"))
			throw new InternalCompilerException("Constructing an illegal 0-operand instruction.");
		this.label = label;
		this.mnemonic = mnemonic;
	}

	/**
	 * Constructs a pseudo instruction with one operand.
	 *
	 * @param label required label (name of declared data area or constant)
	 * @param mnemonic mnemonic
	 * @param pseudoOperand value ("equ", "dc") or size ("ds")
	 */
	Instruction(String label, String mnemonic, int pseudoOperand)
	{
		if (!pseudoInstructions.contains(mnemonic) || label == null)
			throw new InternalCompilerException("Constructing an illegal pseudo instruction.");
		this.label = label;
		this.mnemonic = mnemonic;
		this.pseudoOperand = pseudoOperand;
	}

	/**
	 * Constructs a normal instruction. Left operand is a register and right operand consists of
	 * immediate value or register, one of which can be omitted.
	 *
	 * @param label optinoal label; null if not used
	 * @param mnemonic mnemonic
	 * @param leftReg required LHS register operand
	 * @param immediateOperand RHS immediate value; null if not used
	 * @param rightReg RHS register operand; null if not used
	 */
	Instruction(String label, String mnemonic, VirtualRegister leftReg,
			String immediateOperand, VirtualRegister rightReg)
	{
		this.label = label;
		this.mnemonic = mnemonic;
		this.leftReg = leftReg;
		this.rightReg = rightReg;
		this.immediateOperand = immediateOperand;

		if (mnemonic == null)
			throw new InternalCompilerException("Missing mnemonic for instruction.");
		if (pseudoInstructions.contains(mnemonic))
			throw new InternalCompilerException("Too many operands for pseudo instruction.");
		if (leftReg == null || (immediateOperand == null && rightReg == null))
			throw new InternalCompilerException("Missing operand for instruction.");
		// Only allow SP as LHS for pop.
		if (mnemonic.equals("pop") && (rightReg == null || leftReg != VirtualRegister.SP))
			throw new InternalCompilerException("Invalid pop instruction.");
		// Prevent jumps to non-const addresses to make flow analysis possible.
		if (isJumpInstruction() && (rightReg != null || immediateOperand.startsWith("@")))
			throw new InternalCompilerException("Non-constant address in jump instruction.");
	}

	/**
	 * Get the register that is modified by this instructions.
	 *
	 * @return modified register or null if doesn't modify any registers.
	 */
	public VirtualRegister getModifiedRegister()
	{
		if (mnemonic.equals("pop"))
			return rightReg;
		if (mnemonic.equals("store") || mnemonic.equals("out") || mnemonic.equals("comp")
				|| isJumpInstruction())
			return null;
		return leftReg;
	}

	/**
	 * Checks if the behavior of this instruction depends on earlier value of the LHS register.
	 *
	 * @return true if the earlier value of LHS register is ignored
	 */
	public boolean discardsLhs()
	{
		return mnemonic.equals("load") || mnemonic.equals("in");
	}

	/**
	 * Checks id the behavior of this instruction depends on earlier value of the RHS register.
	 *
	 * @return true if the earlier value of RHS register is ignored
	 */
	public boolean discardsRhs()
	{
		return mnemonic.equals("load") || mnemonic.equals("in");
	}

	/**
	 * Checks whether the instruction is a jump instruction.
	 *
	 * @return true if jump instruction
	 */
	public boolean isJumpInstruction()
	{
		return mnemonic.charAt(0) == 'j';
	}

	/**
	 * Get the full RHS operand as string.
	 *
	 * @return string representation of RHS
	 */
	public String getRhsString()
	{
		String ret = immediateOperand != null ? immediateOperand : "";
		if (rightReg != null) {
			if (ret.isEmpty())
				ret = rightReg.realRegister.toString();
			else
				ret += "(" + rightReg.realRegister.toString() + ")";
		}
		return ret;
	}
}
