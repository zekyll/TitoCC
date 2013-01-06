package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

/**
 * Declares and defines global or local variable. Consists of a type, a name and
 * an optional initializer expression. For global variables the initializer must
 * be a compile time constant expression (thanks to C standard).
 *
 * <p> EBNF definition:
 *
 * <br> VARIABLE_DECLARATION = TYPE IDENTIFIER ["=" EXPRESSION] ";"
 */
public class VariableDeclaration extends Declaration implements Symbol
{
	private boolean isGlobal; // Used in the compilation phase
	private Type type;
	private String name;
	private Expression initializer;
	private String globallyUniqueName;

	/**
	 * Constructs a VariableDeclaration.
	 *
	 * @param type type of the variable
	 * @param name name of the variable
	 * @param initializer initializer expression or null if the variable is left
	 * uninitialized
	 * @param line starting line number of the variable declaration
	 * @param column starting column/character of the variable declaration
	 */
	public VariableDeclaration(Type type, String name,
			Expression initializer, int line, int column)
	{
		super(line, column);
		this.type = type;
		this.name = name;
		this.initializer = initializer;
	}

	/**
	 * Returns the type of the variable declaration.
	 *
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the initializer expression.
	 *
	 * @return initializer expression or null if there isn't one
	 */
	public Expression getInitializer()
	{
		return initializer;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs) throws SyntaxException, IOException
	{
		if (!scope.add(this))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getLine(), getColumn());
		globallyUniqueName = scope.makeGloballyUniqueName(name);

		isGlobal = scope.isGlobal();
		if (isGlobal)
			compileGlobalVariable(asm, scope);
		else
			compileLocalVariable(asm, scope, regs);
	}

	@Override
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	@Override
	public String getReference()
	{
		return globallyUniqueName + (isGlobal ? "" : "(fp)");
	}

	@Override
	public String toString()
	{
		return "(VAR_DECL " + type + " " + name + " " + initializer + ")";
	}

	private void compileGlobalVariable(Assembler asm, Scope scope)
			throws SyntaxException, IOException
	{
		Integer initValue = 0;
		if (initializer != null) {
			initValue = initializer.getCompileTimeValue();
			if (initValue == null)
				throw new SyntaxException("Global variable must be initialized with a compile time constant.", getLine(), getColumn());
		}

		asm.addLabel(globallyUniqueName);
		asm.emit("dc", "" + initValue);
	}

	private void compileLocalVariable(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		if (initializer != null) {
			initializer.compile(asm, scope, regs);
			asm.emit("store", regs.get(0).toString(), getReference());
		}
	}

	/**
	 * Attempts to parse a variable declaration from token stream. If parsing
	 * fails the stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return VariableDeclaration object or null if tokens don't form a valid
	 * variable declaration
	 */
	public static VariableDeclaration parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		VariableDeclaration varDeclaration = null;

		Type type = Type.parse(tokens);

		if (type != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(type, id.toString(), init, line, column);
			}
		}

		tokens.popMark(varDeclaration == null);
		return varDeclaration;
	}

	private static Expression parseInitializer(TokenStream tokens)
	{
		tokens.pushMark();
		Expression init = null;

		if (tokens.read().toString().equals("="))
			init = Expression.parse(tokens);

		tokens.popMark(init == null);
		return init;
	}
}
