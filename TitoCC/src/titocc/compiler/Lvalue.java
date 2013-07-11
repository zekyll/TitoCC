package titocc.compiler;

/**
 * A temporary value that can have its address taken and can be assigned to (unless array or
 * function). Implemented as an address contained in a virtual register.
 */
public class Lvalue
{
	/**
	 * Address register.
	 */
	final private VirtualRegister addressRegister;

	/**
	 * Constructor.
	 *
	 * @param addressRegister address register
	 */
	public Lvalue(VirtualRegister addressRegister)
	{
		this.addressRegister = addressRegister;
	}

	/**
	 * Get the address register.
	 *
	 * @return
	 */
	public VirtualRegister getRegister()
	{
		return addressRegister;
	}
}
