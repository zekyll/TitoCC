package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
import titocc.compiler.DeclarationType;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.StorageClass;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Declares and defines a global or local variable. Consists of a type and a name (given by
 * declaration specifiers and a declarator) and an optional initializer expression. For global
 * variables the initializer must be a compile time constant expression.
 *
 * <p> EBNF definition:
 *
 * <br> VARIABLE_DECLARATION = DECLARATION_SPECIFIERS DECLARATOR ["=" EXPRESSION] ";"
 */
public class VariableDeclaration extends Declaration
{
	/**
	 * Declaration specifiers, giving the storage class and part of the type.
	 */
	private final DeclarationSpecifiers declarationSpecifiers;

	/**
	 * Declarator which modifies the type and gives the variable name.
	 */
	private final Declarator declarator;

	/**
	 * Optional initializer expression. Null if not used.
	 */
	private final Expression initializer;

	/**
	 * Constructs a VariableDeclaration.
	 *
	 * @param declarationSpecifiers declaration specifiers
	 * @param declarator declarator of the variable
	 * @param initializer initializer expression or null if the variable is left uninitialized
	 * @param position starting position of the variable declaration
	 */
	public VariableDeclaration(DeclarationSpecifiers declarationSpecifiers, Declarator declarator,
			Expression initializer, Position position)
	{
		super(position);
		this.declarationSpecifiers = declarationSpecifiers;
		this.declarator = declarator;
		this.initializer = initializer;
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
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws SyntaxException, IOException
	{
		DeclarationType declType = declarationSpecifiers.compile(scope);

		declType.type = declarator.compile(declType.type, scope, null);
		if (!declType.type.isObject())
			throw new SyntaxException("Variable must have object type.", getPosition());

		if (declType.type instanceof ArrayType && initializer != null)
			throw new SyntaxException("Array initializers are not supported.", getPosition());

		Symbol sym = addSymbol(scope, declType);

		if (initializer != null && !initializer.isAssignableTo(declType.type, scope)) {
			throw new SyntaxException("Initializer type doesn't match variable type.",
					getPosition());
		}

		if (scope.isGlobal())
			compileGlobalVariable(asm, scope, sym);
		else
			compileLocalVariable(asm, scope, sym, regs);
	}

	private Symbol addSymbol(Scope scope, DeclarationType declType) throws SyntaxException
	{
		String name = declarator.getName();

		Symbol sym;
		if (scope.isGlobal()) {
			sym = new Symbol(name, declType.type, Symbol.Category.GlobalVariable,
					null, false);
		} else {
			sym = new Symbol(name, declType.type, Symbol.Category.LocalVariable,
					StorageClass.Auto, false);
		}

		if (!scope.add(sym))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());

		return sym;
	}

	@Override
	public String toString()
	{
		return "(VAR_DECL " + declarationSpecifiers + " " + declarator + " " + initializer + ")";
	}

	private void compileGlobalVariable(Assembler asm, Scope scope, Symbol sym)
			throws SyntaxException, IOException
	{
		Integer initValue = 0;
		if (initializer != null) {
			initValue = initializer.getCompileTimeValue();
			if (initValue == null) {
				throw new SyntaxException("Global variable must be initialized with a compile"
						+ " time constant.", getPosition());
			}
		}

		asm.addLabel(sym.getGlobalName());
		if (sym.getType() instanceof ArrayType)
			asm.emit("ds", "" + sym.getType().getSize());
		else
			asm.emit("dc", "" + initValue);
	}

	private void compileLocalVariable(Assembler asm, Scope scope, Symbol sym, Registers regs)
			throws SyntaxException, IOException
	{
		if (initializer != null) {
			initializer.compile(asm, scope, regs);
			asm.emit("store", regs.get(0).toString(), sym.getReference());
		}
	}

	/**
	 * Attempts to parse a variable declaration from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return VariableDeclaration object or null if tokens don't form a valid variable declaration
	 */
	public static VariableDeclaration parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		VariableDeclaration varDeclaration = null;

		DeclarationSpecifiers declSpecifiers = DeclarationSpecifiers.parse(tokens);

		if (declSpecifiers != null) {
			Declarator declarator = Declarator.parse(tokens, false);
			if (declarator != null) {
				Expression init = parseInitializer(tokens);
				if (tokens.read().toString().equals(";"))
					varDeclaration = new VariableDeclaration(declSpecifiers, declarator, init, pos);
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
