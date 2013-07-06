package titocc.compiler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages available registers. The registers are allocated in a circular fashion; when there are
 * no more free registers, the oldest register currently in use is stored (spilled) to program stack
 * making it available for reallocation. Spilled registers are stored starting from position 1(fp)
 * of the current program stack frame, right before local variables.
 *
 * <br> Allocated registers are organized as a stack; i.e. only the last allocated register can be
 * deallocated. The stack is divided into frames. Deallocating registers does not immediately reload
 * spilled registers; instead reloading is delayed until next frame exit, at which point all spilled
 * registers from the previous frame are reloaded.
 */
public class RegisterAllocator
{
	/**
	 * Registers available for the allocator. Uses all general purpose registers except R0 because
	 * it has special behavior.
	 */
	private static final Register allRegisters[] = new Register[]{
		Register.R1, Register.R2, Register.R3, Register.R4, Register.R5
	};

	/**
	 * Toal number of allocated registers.
	 */
	private int allocCount = 0;

	/**
	 * Total number of spilled registers.
	 */
	private int spillCount = 0;

	/**
	 * Biggest number of spilled registers since last reset.
	 */
	private int maxSpillCount = 0;

	/**
	 * A stack of frame start positions.
	 */
	private Deque<Integer> frameStartPositions = new ArrayDeque<Integer>();

	/**
	 * Constructs a new register allocator that starts with all register available for allocation.
	 */
	public RegisterAllocator()
	{
		frameStartPositions.push(0);
	}

	/**
	 * Enters new frame in the register stack. Has no other effect except marking the frame start
	 * position for exitFrame().
	 */
	public void enterFrame()
	{
		frameStartPositions.push(allocCount);
	}

	/**
	 * Exits a register stack frame. Reloads all spilled registers from the *previous* frame.
	 *
	 * @param asm assembler used for reloading spilled registers
	 */
	public void exitFrame(Assembler asm) throws IOException
	{
		frameStartPositions.pop();

		while (spillCount > frameStartPositions.peek()) {
			--spillCount;
			Register reloadReg = allRegisters[spillCount % allRegisters.length];
			asm.emit("load", reloadReg, (1 + spillCount) + "(fp)");
		}
	}

	/**
	 * Returns the biggest number of spilled registers since the last call to resetMaxSpillCount.
	 *
	 * @return max spill count
	 */
	public int getMaxSpillCount()
	{
		return maxSpillCount;
	}

	/**
	 * Resets the spilled register counter.
	 */
	public void resetMaxSpillCount()
	{
		maxSpillCount = spillCount;
	}

	/**
	 * Allocates a new register. If there are no free registers, one is made available by
	 * temporarily storing oldest used register to stack.
	 *
	 * @param asm assembler used when storing spilled registers to stack
	 * @return the allocated register
	 */
	public Register allocate(Assembler asm) throws IOException
	{
		Register allocatedReg = allRegisters[allocCount % allRegisters.length];
		++allocCount;

		if (allocCount - spillCount > allRegisters.length) {
			asm.emit("store", allocatedReg, (1 + spillCount) + "(fp)");
			++spillCount;
			maxSpillCount = Math.max(spillCount, maxSpillCount);
		}

		return allocatedReg;
	}

	/**
	 * Deallocates the last register that was allocated.
	 */
	public void deallocate()
	{
		if (allocCount <= spillCount)
			throw new InternalCompilerException("No registers to deallocate.");
		--allocCount;
	}
}
