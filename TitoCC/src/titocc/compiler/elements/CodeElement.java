package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;

/**
 * Abstract base for all the code elements. Stores the position of the element
 * within the source file so that it can be used by compiler messages. Also
 * provides functions for pushing/popping registers to/from stack (which should
 * probably be refactored somewhere else).
 */
public abstract class CodeElement
{
	private int line, column;

	/**
	 * Constructs a code element.
	 *
	 * @param line starting line number of the element
	 * @param column starting column/character of the element
	 */
	public CodeElement(int line, int column)
	{
		this.line = line;
		this.column = column;
	}

	/**
	 * Returns the line number where the code element starts.
	 *
	 * @return line number
	 */
	public int getLine()
	{
		return line;
	}

	/**
	 * Returns the column/character where the code element starts.
	 *
	 * @return column
	 */
	public int getColumn()
	{
		return column;
	}

	/**
	 * Allocates an additional register. Assumes that at least one is available.
	 * If two or more are available then does nothing. If only one is available,
	 * then pushes one of the reserved registers to stack and it will be added
	 * to available registers. The new register will be added so that it is used
	 * after the original first one, so that the first one can be used for
	 * returning the expression's value.
	 *
	 * @param asm assembler used for emitting the push instruction
	 * @param registers available registers; must contain at least one register
	 * @return register that was pushed to stack or null if no register was
	 * pushed
	 * @throws IOException if assembler throws
	 */
	protected Register pushRegister(Assembler asm, Stack<Register> registers)
			throws IOException
	{
		Register pushedRegister = null;

		// Free up a register if there's not at least two available.
		if (registers.size() == 1) {
			// Find first non-available register.
			for (Register r : Arrays.asList(Register.values()))
				if (r != registers.peek()) {
					pushedRegister = r;
					break;
				}

			// Add to available registers and reverse the order, because we want
			// the already free register to remain the first one.
			Register tmp = registers.pop();
			registers.push(pushedRegister);
			registers.push(tmp);

			// Push chosen register to stack.
			asm.emit("push", "sp", pushedRegister.toString());
		}

		return pushedRegister;
	}

	/**
	 * Deallocates the register reserved by the call to pushRegister. If
	 * pushRegister pushed a register to stack, then popRegister will emit the
	 * corresponding pop instruction and remove the register from available
	 * registers. Otherwise does nothing.
	 *
	 * @param asm assembler used for emitting the pop instruction
	 * @param registers available registers
	 * @param pushedRegister return value of pushRegister()
	 * @throws IOException if assembler throws
	 */
	protected void popRegister(Assembler asm, Stack<Register> registers, Register pushedRegister)
			throws IOException
	{
		if (pushedRegister != null) {
			// Pop register from stack.
			asm.emit("pop", "sp", pushedRegister.toString());

			// Undo the changes made by pushRegister().
			Register tmp = registers.pop();
			registers.pop();
			registers.push(tmp);
		}
	}
}
