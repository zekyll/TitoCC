package titocc.compiler;

import java.io.IOException;
import java.util.LinkedList;

/**
 * A virtual stack of temporary values where the top-most elements are backed by registers. The
 * bottom values are pushed to program stack when there are no free registers to allocate.
 */
public class Vstack
{
	/**
	 * Register allocator.
	 */
	private final RegisterAllocator regAllocator;

	/**
	 * A stack of temporary values. (first = top).
	 */
	private final LinkedList<TmpValue> values = new LinkedList<TmpValue>();

	/**
	 * Constructs a virtual stack.
	 *
	 * @param regAllocator register allocator
	 */
	public Vstack(RegisterAllocator regAllocator)
	{
		this.regAllocator = regAllocator;
	}

	/**
	 * Pushes a new register rvalue onto the virtual stack, allocating a new register for it.
	 *
	 * @param asm assembler used for push instructions if registers are spilled
	 * @return the allocated register
	 */
	public Register pushRegisterRvalue(Assembler asm) throws IOException
	{
		Register reg = regAllocator.allocate(asm);
		values.push(new TmpValue(null, reg, null));
		return reg;
	}

	/**
	 * Pushes a new symbolic value onto the virtual stack.
	 *
	 * @param symbolicValue
	 */
	public void pushSymbolicValue(String symbolicValue) throws IOException
	{
		values.push(new TmpValue(symbolicValue, null, null));
	}

	/**
	 * Removes the top value from the virtual stack and deallocates any registers reserved by it.
	 */
	public void pop() throws IOException
	{
		if (values.isEmpty())
			throw new InternalCompilerException("Attempting to pop from empty virtual stack.");
		values.getFirst().deallocate(regAllocator);
		values.pop();
	}

	/**
	 * Returns a symbolic reference to a stack element that can be used as the RHS operand of an
	 * instruction.
	 *
	 * @param idx index of the stack item starting from the top
	 * @return RHS operand for instruction
	 */
	public String top(int idx)
	{
		return values.get(idx).getRhsOperand();
	}

	/**
	 * Calls loadValue() on top-most stack value.
	 *
	 * @param asm assembler used for store instructions if registers are spilled
	 * @return register allocated for the value
	 */
	public Register loadTopValue(Assembler asm) throws IOException
	{
		return values.getFirst().loadValue(regAllocator, asm);
	}

	/**
	 * Calls replaceWithAddress() on top-most stack value.
	 *
	 * @param asm assembler used for store instructions if registers are spilled
	 */
	public void replaceTopWithAddress(Assembler asm) throws IOException
	{
		values.getFirst().replaceWithAddress(regAllocator, asm);
	}

	/**
	 * Calls dereference() on top-most stack value.
	 *
	 * @param asm assembler used for store instructions if registers are spilled
	 */
	public void dereferenceTop(Assembler asm) throws IOException
	{
		values.getFirst().dereference(regAllocator, asm);
	}

	/**
	 * Enters a new virtual stack frame. Has no other effect except marking the frame start
	 * position for exitFrame().
	 */
	public void enterFrame()
	{
		regAllocator.enterFrame();
	}

	/**
	 * Exits a virtual stack frame. Reloads all spilled registers from the *previous* frame.
	 *
	 * @param asm assembler used for reloading spilled registers
	 */
	public void exitFrame(Assembler asm) throws IOException
	{
		regAllocator.exitFrame(asm);
	}

	/**
	 * Returns the register allocator.
	 *
	 * @return register allocator
	 */
	public RegisterAllocator getRegisterAllocator()
	{
		return regAllocator;
	}

	/**
	 * Returns the number of values in the virtual stack.
	 *
	 * @return stack size
	 */
	public int size()
	{
		return values.size();
	}
}
