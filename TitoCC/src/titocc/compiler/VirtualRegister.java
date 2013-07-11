package titocc.compiler;

/**
 * A virtual register. In the first phase all code is generated using virtual registers; then
 * register allocation maps them to physical registers, and some may need to be spilled to stack.
 * There is an unlimited number of virtual registers.
 */
public class VirtualRegister
{
	/**
	 * Virtual register mapping to stack pointer (SP).
	 */
	public static final VirtualRegister SP = new VirtualRegister(Register.SP);

	/**
	 * Virtual register mapping to frame pointer (FP).
	 */
	public static final VirtualRegister FP = new VirtualRegister(Register.FP);

	/**
	 * Virtual register mapping to R0. Auxiliary register used when temporarily loading spilled
	 * values to register. (Can only be used as the left operand of instructions; when used
	 * in right operand it always has value of 0.)
	 */
	public static final VirtualRegister R0 = new VirtualRegister(Register.R0);

	/**
	 * Virtual register mapping to R5. Auxiliary register used when temporarily loading spilled
	 * values to register.
	 */
	public static final VirtualRegister R5 = new VirtualRegister(Register.R5);

	/**
	 * Physical register used by this virtual register or null if not mapped yet.
	 */
	public Register realRegister;

	/**
	 * Index of the instruction where the register is first used. Used by register allocation.
	 */
	int liveRangeStart = -1;

	/**
	 * Index of the instruction where the register is last used, plus one. Used by register
	 * allocation.
	 */
	int liveRangeEnd = -1;

	/**
	 * Index of the spill slot on stack where the value is stored. Used by register allocation.
	 */
	int spillIdx = -1;

	/*
	 * Constructs a virtual register that will be later mapped to a physical register.
	 */
	public VirtualRegister()
	{
		this.realRegister = null;
	}

	/**
	 * Constructs a virtual register that maps to specific physical register.
	 *
	 * @param realRegister the physical register.
	 */
	public VirtualRegister(Register realRegister)
	{
		this.realRegister = realRegister;
	}
}
