package titocc.compiler.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.DeclarationType;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.StorageClass;
import titocc.compiler.Symbol;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Function definition that defines the type, storage class and name of the function, and
 * executed statements. Functions are parsed using a declarator syntax similar to variable
 * declarations. Declaration specifiers and declarator together specify the return type, storage
 * class, function name and parameters. The declarator must be a function declarator (checked in
 * compilation phase). Function definition also requires a compound statement body.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION_DEFINITION = TYPE_SPECIFIER DECLARATOR COMPOUND_STATEMENT
 */
public class FunctionDefinition extends ExternalDeclaration
{
	/**
	 * Declaration specifiers that specify the storage class and part of the return type. The
	 * actual return type is further modified by the declarator.
	 */
	private final DeclarationSpecifiers declarationSpecifiers;

	/**
	 * Declarator that modifies the return type, and specifies the function name and parameters.
	 */
	private final Declarator declarator;

	/**
	 * Function body.
	 */
	private final CompoundStatement body;

	/**
	 * Symbol for return value. Set when compiling the function.
	 */
	private Symbol retValSymbol;

	/**
	 * Symbol for function end location. Set when compiling the function.
	 */
	private Symbol endSymbol;

	/**
	 * Constructs a FunctionDefinition.
	 *
	 * @param declarationSpecifiers return type specifier
	 * @param declarator declarator
	 * @param body body of the function
	 * @param position starting position of the function definition
	 */
	public FunctionDefinition(DeclarationSpecifiers declarationSpecifiers, Declarator declarator,
			CompoundStatement body, Position position)
	{
		super(position);
		this.declarationSpecifiers = declarationSpecifiers;
		this.declarator = declarator;
		this.body = body;
	}

	@Override
	public void compile(Assembler asm, IntermediateCompiler ic_, Scope scope, StackAllocator sa_)
			throws IOException, SyntaxException
	{
		asm.addEmptyLines(1);

		// Reset register spill counter.
		StackAllocator stack = new StackAllocator();

		// Create new scope.
		Scope functionScope = new Scope(scope, declarator.getName() + "_");
		scope.addSubScope(functionScope);

		// Get function type and declare parameters.
		List<Symbol> parameters = new ArrayList<Symbol>();
		DeclarationType declType = declarationSpecifiers.compile(scope);
		declType.type = declarator.compile(declType.type, functionScope, parameters);

		// Check that the declarator actually declares a function.
		if (!declType.type.isFunction())
			throw new SyntaxException("Missing function parameter list.", getPosition());
		CType returnType = ((FunctionType) declType.type).getReturnType();

		// Declare the function and its symbols. Point of declaration is right
		// after the function's declarator, i.e. the function name cannot be
		// used in parameter list.
		Symbol funcSym = addSymbol(scope, declType);
		addInternalSymbols(functionScope, returnType);

		// Constants for return value and parameters.
		int paramTotalSize = addParameterConstants(asm, parameters);

		// Compile body before prologue because we want to know all the local
		// variables in the prologue.
		IntermediateCompiler bodyIc = new IntermediateCompiler();
		compileBody(bodyIc, functionScope, stack);
		bodyIc.compile(stack);
		List<Symbol> localVariables = getLocalVariables(functionScope);

		compilePrologue(asm, localVariables, stack.getSpillCount(), funcSym.getReference());
		bodyIc.sendToAssembler(asm);
		compileEpilogue(asm, localVariables, stack.getSpillCount(), paramTotalSize);
	}

	private Symbol addSymbol(Scope scope, DeclarationType declType) throws SyntaxException
	{
		String name = declarator.getName();
		Symbol sym = new Symbol(name, declType.type, Symbol.Category.Function,
				StorageClass.Extern, false);
		sym = scope.add(sym);
		if (!sym.define())
			throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());
		return sym;
	}

	private void addInternalSymbols(Scope scope, CType returnType)
	{
		// Add symbol for the function end so that return statements can jump to it.
		endSymbol = new Symbol("__End", CType.VOID, Symbol.Category.Internal,
				null, false);
		scope.add(endSymbol);

		// Add symbol for location of the return value.
		retValSymbol = new Symbol("__Ret", returnType, Symbol.Category.Internal,
				StorageClass.Auto, false);
		scope.add(retValSymbol);
	}

	/**
	 * Emit constants for return value and parameters. Returs total size of the parameters.
	 */
	private int addParameterConstants(Assembler asm, List<Symbol> parameters) throws IOException
	{
		int paramTotalSize = 0;
		for (Symbol p : parameters)
			paramTotalSize += p.getType().getSize();

		asm.addLabel(retValSymbol.getGlobalName());
		asm.emit("equ", "-" + (paramTotalSize + 2));

		int paramOffset = -1 - paramTotalSize;
		for (Symbol p : parameters) {
			asm.addLabel(p.getGlobalName());
			asm.emit("equ", "" + paramOffset);
			paramOffset += p.getType().getSize();
		}

		return paramTotalSize;
	}

	private void compilePrologue(Assembler asm, List<Symbol> localVariables, int spillCount,
			String startLabel) throws IOException, SyntaxException
	{
		// Define constants for local variables, which are placed after register spill locations.
		// 0(fp) is old program counter and local data starts from 1(fp).
		int varOffset = spillCount;
		for (Symbol var : localVariables) {
			asm.addLabel(var.getGlobalName());
			asm.emit("equ", "" + (1 + varOffset));
			varOffset += var.getType().getSize();
		}

		// Label for function entry point.
		asm.addLabel(startLabel);

		// Allocate stack space for local variables.
		if (varOffset > 0)
			asm.emit("add", Register.SP, "=" + varOffset);

		// Push registers.
		asm.emit("pushr", "SP");
	}

	private void compileBody(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		// Compile statements directly, so that CompoundStatement doesn't create
		// new scope, and the statements are in the same scope as parameters.
		for (Statement st : body.getStatements())
			st.compile(ic, scope, stack);
		ic.addLabel(endSymbol.getReference());
	}

	private void compileEpilogue(Assembler asm, List<Symbol> localVariables, int spillCount,
			int paramTotalSize) throws IOException, SyntaxException
	{
		// Pop registers from stack.
		asm.emit("popr", "SP");

		// Calculate total size of local variables and register spill locations on stack.
		int localDataSize = spillCount;
		for (Symbol var : localVariables)
			localDataSize += var.getType().getSize();

		// Pop all function local data from program stack.
		if (localDataSize > 0)
			asm.emit("sub", Register.SP, "=" + localDataSize);

		// Exit from function.
		asm.emit("exit", Register.SP, "=" + paramTotalSize);
	}

	private List<Symbol> getLocalVariables(Scope scope)
	{
		List<Symbol> localVariables = new ArrayList<Symbol>();

		for (Symbol symbol : scope.getSymbols()) {
			if (symbol.getCategory() == Symbol.Category.LocalVariable)
				localVariables.add(symbol);
		}

		for (Scope subscope : scope.getSubScopes())
			localVariables.addAll(getLocalVariables(subscope));

		return localVariables;
	}

	@Override
	public String toString()
	{
		return "(FUNC " + declarationSpecifiers + " " + declarator + " " + body + ")";
	}

	/**
	 * Attempts to parse a function definition from token stream. If parsing fails the stream is
	 * reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return FunctionDefinition object or null if tokens don't form a valid function definition
	 */
	public static FunctionDefinition parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		FunctionDefinition function = null;

		DeclarationSpecifiers declSpecifiers = DeclarationSpecifiers.parse(tokens);

		if (declSpecifiers != null) {
			Declarator declarator = Declarator.parse(tokens, true, false);
			if (declarator != null) {
				CompoundStatement body = CompoundStatement.parse(tokens);
				if (body != null)
					function = new FunctionDefinition(declSpecifiers, declarator, body, pos);
			}
		}

		tokens.popMark(function == null);
		return function;
	}
}
