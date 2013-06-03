package titocc.compiler;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Manages active and available registers. Active registers are the ones used for current
 * instruction, and there's is always at least one active register, which is used for returning
 * expression's value. Number of active registers can be increased with allocate(). When evaluating
 * subexpressions, active registers containing temporary values can be removed without deallocating
 * them with removeFirst().
 */
public class Registers
{
	/**
	 * Active registers that are used for current operation.
	 */
	private final LinkedList<Register> activeRegisters = new LinkedList<Register>();

	/**
	 * Registers that don't contain important data.
	 */
	private final LinkedList<Register> freeRegisters = new LinkedList<Register>();

	/**
	 * Registers that have been pushed to the program stack.
	 */
	private final LinkedList<Register> pushedRegisters = new LinkedList<Register>();

	/**
	 * Registers that are in use, but are inactive. These are available for reallocation by storing
	 * them to memory.
	 */
	private final LinkedList<Register> reservedRegisters = new LinkedList<Register>();

	/**
	 * Constructs a new register manager that starts with one active register.
	 */
	public Registers()
	{
		// Use all general purpose registers except R0 because it behaves differently.
		freeRegisters.addFirst(Register.R5);
		freeRegisters.addFirst(Register.R4);
		freeRegisters.addFirst(Register.R3);
		freeRegisters.addFirst(Register.R2);
		activeRegisters.addFirst(Register.R1);
	}

	/**
	 * Returns the number of active registers.
	 *
	 * @return number of activeregisters
	 */
	public int getActiveRegisterCount()
	{
		return activeRegisters.size();
	}

	/**
	 * Returns an active register with the index idx. 0 returns the first active register etc.
	 *
	 * @param idx index of the active register
	 * @return the requested register
	 */
	public Register get(int idx)
	{
		if (idx >= activeRegisters.size())
			throw new InternalCompilerException("Using an unallocated register.");
		return activeRegisters.get(idx);
	}

	/**
	 * Removes the first register from the list of active registers.
	 */
	public void removeFirst()
	{
		if (activeRegisters.isEmpty())
			throw new InternalCompilerException("No active registers to remove.");
		reservedRegisters.addLast(activeRegisters.removeFirst());
	}

	/**
	 * Adds back or "reactivates" the register removed by previous call to
	 * removeFirst().
	 */
	public void addFirst()
	{
		if (reservedRegisters.isEmpty())
			throw new InternalCompilerException("No registers to reactivate.");
		activeRegisters.addFirst(reservedRegisters.removeLast());
	}

	/**
	 * Increases the number of currently active registers. If there are not enough available
	 * registers, then pushes one of the reserved registers to stack and it will be added as the
	 * last to available registers.
	 *
	 * @param asm assembler used for emitting the push instruction
	 * @throws IOException if assembler throws
	 */
	public void allocate(Assembler asm) throws IOException
	{
		// Free up a register if there's none.
		Register pushedRegister = null;
		if (freeRegisters.isEmpty()) {
			// Get first non-available register.
			if (reservedRegisters.isEmpty())
				throw new InternalCompilerException("Too many registers allocated.");
			pushedRegister = reservedRegisters.removeFirst();

			freeRegisters.addLast(pushedRegister);

			// Push chosen register to stack.
			asm.emit("push", "sp", pushedRegister.toString());
		}
		pushedRegisters.push(pushedRegister);

		activeRegisters.addLast(freeRegisters.removeFirst());
	}

	/**
	 * Decreases the number of active registers by deallocating the register allocated by the
	 * previous call to allocate(). If allocate() pushed a register to stack, then popRegister will
	 * emit the corresponding pop instruction and remove the register from available registers.
	 *
	 * @param asm assembler used for emitting the pop instruction
	 * @throws IOException if assembler throws
	 */
	public void deallocate(Assembler asm) throws IOException
	{
		if (activeRegisters.isEmpty())
			throw new InternalCompilerException("No registers to deallocate.");

		freeRegisters.addFirst(activeRegisters.removeLast());

		Register pushedRegister = pushedRegisters.pop();
		if (pushedRegister != null) {
			// Pop register from stack.
			asm.emit("pop", "sp", pushedRegister.toString());

			// Put back to reserved registers.
			reservedRegisters.addFirst(freeRegisters.removeLast());
		}
	}
}
