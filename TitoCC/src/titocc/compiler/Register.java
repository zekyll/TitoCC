package titocc.compiler;

/**
 * Enumeration of the registers in ttk-91 machine. Names are chosen so that
 * .toString() method can be used to output register names in the assembly code.
 * R0 is not included because it has different behavior, and all the registers
 * defined here need to be usable by the register allocator.
 */
public enum Register
{
	R1, R2, R3, R4, R5
};
