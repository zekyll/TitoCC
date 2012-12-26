package titocc.compiler.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.Register;

public abstract class CodeElement
{
	private int line, column;

	public CodeElement(int line, int column)
	{
		this.line = line;
		this.column = column;
	}

	public int getLine()
	{
		return line;
	}

	public int getColumn()
	{
		return column;
	}

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
			// to use the same register for the left expression and the binary
			// expression.
			Register tmp = registers.pop();
			registers.push(pushedRegister);
			registers.push(tmp);

			// Push chosen register to stack.
			asm.emit("push", "sp", pushedRegister.toString());
		}

		return pushedRegister;
	}

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
