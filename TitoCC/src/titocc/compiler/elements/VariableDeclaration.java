package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Declares and defines global or local variable. Consists of a type, a name and
 * an optional initializer expression. For global variables the initializer must
 * be a compile time constant expression (thanks to C standard).
 *
 * <p> EBNF definition:
 *
 * <br> VARIABLE_DECLARATION = TYPE_SPECIFIER DECLARATOR ["=" EXPRESSION] ";"
 */
public class VariableDeclaration extends Declaration implements Symbol
{
	/**
	 * Whether the variable is global. Used in the compilation phase
	 */
	private boolean isGlobal;
	/**
	 * Type specifier for the declaration. (void or int)
	 */
	private final TypeSpecifier typeSpecifier;
	/**
	 * Type of the variable. Set when compiling the function.
	 */
	private CType type;
	/**
	 * Declarator which has the variable name and which modifies the type
	 * specifier.
	 */
	private final Declarator declarator;
	/**
	 * Optional initializer expression. Null if not used.
	 */
	private final Expression initializer;
	/**
	 * Globally unique name for the variable symbol. Set when compiling the
	 * function.
	 */
	private String globallyUniqueName;

	/**
	 * Constructs a VariableDeclaration.
	 *
	 * @param typeSpecifier specifier of the variable
	 * @param declarator declarator of the variable
	 * @param initializer initializer expression or null if the variable is left
	 * uninitialized
	 * @param position starting position of the variable declaration
	 */
	public VariableDeclaration(TypeSpecifier typeSpecifier, Declarator declarator,
			Expression initializer, Position position)
	{
		super(position);
		this.typeSpecifier = typeSpecifier;
		this.declarator = declarator;
		this.initializer = initializer;
	}

	/**
	 * Returns the type of the variable declaration.
	 *
	 * @return the type
	 */
	public CType getType()
	{
		return type;
	}

	@Override
	public String getName()
	{
		return declarator.getName();
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
		type = declarator.getModifiedType(typeSpecifier.getType());
		if (!type.isObject())
			throw new SyntaxException("Variable must have object type.", getPosition());

		if (type instanceof ArrayType && initializer != null)
			throw new SyntaxException("Array initializers are not supported.", getPosition());

		if (!scope.add(this))
			throw new SyntaxException("Redefinition of \"" + getName() + "\".", getPosition());
		globallyUniqueName = scope.makeGloballyUniqueName(getName());

		if (initializer != null && !initializer.isAssignableTo(type, scope))
			throw new SyntaxException("Initializer type doesn't match variable type.", getPosition());

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
		return "(VAR_DECL " + typeSpecifier + " " + declarator + " " + initializer + ")";
	}

	private void compileGlobalVariable(Assembler asm, Scope scope)
			throws SyntaxException, IOException
	{
		Integer initValue = 0;
		if (initializer != null) {
			initValue = initializer.getCompileTimeValue();
			if (initValue == null)
				throw new SyntaxException("Global variable must be initialized with a compile time constant.", getPosition());
		}

		asm.addLabel(globallyUniqueName);
		if (type instanceof ArrayType)
			asm.emit("ds", "" + type.getSize());
		else
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
		Position pos = tokens.getPosition();
		tokens.pushMark();
		VariableDeclaration varDeclaration = null;

		TypeSpecifier type = TypeSpecifier.parse(tokens);

		if (type != null) {
			Declarator declarator = Declarator.parse(tokens);
			if (declarator != null) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(type, declarator, init, pos);
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
