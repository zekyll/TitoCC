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
	 * Left operand
	 */
	VirtualRegister leftReg = null;

	VirtualRegister rightReg = null;

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
		if (mnemonic == null || pseudoInstructions.contains(mnemonic) || leftReg == null
				|| (immediateOperand == null && rightReg == null))
			throw new InternalCompilerException("Constructing an illegal instruction.");
		this.label = label;
		this.mnemonic = mnemonic;
		this.leftReg = leftReg;
		this.rightReg = rightReg;
		this.immediateOperand = immediateOperand;
	}

	/**
	 * Checks whether the instruction modifies the left register. Note that RHS register can never
	 * ne modified.
	 *
	 * @return true if modifies LHS
	 */
	public boolean modifiesLhs()
	{
		if (mnemonic.equals("store") || mnemonic.equals("out")
				|| mnemonic.equals("comp") || mnemonic.charAt(0) == 'j')
			return false;
		return leftReg != null;
	}

	/**
	 * Checks the behavior of this instruction depends on earlier value of the left hand side
	 * register.
	 *
	 * @return true if the earlier value of LHS register is ignored
	 */
	public boolean discardsLhs()
	{
		return mnemonic.equals("load") || mnemonic.equals("in");
	}
}
