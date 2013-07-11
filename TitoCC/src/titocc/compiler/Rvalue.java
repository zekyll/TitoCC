package titocc.compiler;

/**
 * Temporary result of an expression that is located in a register.
 */
public class Rvalue
{
	/**
	 * Virtual register where the value is stored.
	 */
	private final VirtualRegister valueRegister;

	/**
	 * Constructor.
	 *
	 * @param valueRegister
	 */
	public Rvalue(VirtualRegister valueRegister)
	{
		this.valueRegister = valueRegister;
	}

	/**
	 * Get the register.
	 *
	 * @return
	 */
	public VirtualRegister getRegister()
	{
		return valueRegister;
	}
}
