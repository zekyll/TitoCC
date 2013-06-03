package titocc.compiler.elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.Registers;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.compiler.types.CType;
import titocc.compiler.types.FunctionType;
import titocc.compiler.types.VoidType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Function declaration and definition. Forward declarations are not currently supported so this is
 * always both declaration and definition. Functions are parsed using the declarator syntax similar
 * to object declarations, where the type specifier and declarator together specify the return type,
 * function name and parameters. The declarator must specify a function type (checked during
 * semantic analysis). Function definition also needs to have a compound statement as the function
 * body.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION = TYPE_SPECIFIER DECLARATOR COMPOUND_STATEMENT
 */
public class Function extends Declaration
{
	/**
	 * Return type specifier. The actual return type is further modified by the declarator.
	 */
	private final TypeSpecifier returnTypeSpecifier;

	/**
	 * Declarator that specifies the function name and parameter types.
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
	 * Constructs a Function.
	 *
	 * @param returnTypeSpecifier return type specifier
	 * @param declarator declarator
	 * @param body body of the function
	 * @param position starting position of the function
	 */
	public Function(TypeSpecifier returnTypeSpecifier, Declarator declarator,
			CompoundStatement body, Position position)
	{
		super(position);
		this.returnTypeSpecifier = returnTypeSpecifier;
		this.declarator = declarator;
		this.body = body;
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		asm.addEmptyLines(1);

		// Create new scope.
		Scope functionScope = new Scope(scope, declarator.getName() + "_");
		scope.addSubScope(functionScope);

		// Get function type and declare parameters.
		List<Symbol> parameters = new ArrayList<Symbol>();
		CType type = declarator.compile(returnTypeSpecifier.getType(), functionScope, parameters);

		// Check that the declarator actually declares a function.
		if (!(type instanceof FunctionType))
			throw new SyntaxException("Missing function parameter list.", getPosition());
		CType returnType = ((FunctionType) type).getReturnType();

		// Declare the function and its symbols. Point of declaration is right
		// after the function's declarator, i.e. the function name cannot be
		// used in parameter list.
		Symbol sym = addSymbol(scope, type);
		addInternalSymbols(functionScope, returnType);

		// Constants for return value and parameters.
		int paramTotalSize = addParameterConstants(asm, parameters);

		// Compile body before prologue because we want to know all the local
		// variables in the prologue.
		StringWriter bodyWriter = new StringWriter();
		Assembler bodyAsm = new Assembler(bodyWriter);
		compileBody(bodyAsm, functionScope, regs);
		List<Symbol> localVariables = getLocalVariables(functionScope);
		bodyAsm.finish();

		compilePrologue(asm, localVariables, sym.getReference());
		asm.getWriter().append(bodyWriter.toString());
		compileEpilogue(asm, localVariables, paramTotalSize);
	}

	private Symbol addSymbol(Scope scope, CType type) throws SyntaxException
	{
		String name = declarator.getName();
		Symbol sym = new Symbol(name, type, scope, "", Symbol.Category.Function);
		if (!scope.add(sym))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());
		return sym;
	}

	private void addInternalSymbols(Scope scope, CType returnType)
	{
		// Add symbol ("__End") for the function end so that return statements can jump to it.
		endSymbol = new Symbol("End", new VoidType(), scope, "", Symbol.Category.Internal);
		scope.add(endSymbol);

		// Add symbol ("__Ret") for location of the return value.
		retValSymbol = new Symbol("Ret", returnType, scope, "(fp)", Symbol.Category.Internal);
		scope.add(retValSymbol);
	}

	/**
	 * Emit constants for return value and parameters. Returs total size of tha parameters.
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

	private void compilePrologue(Assembler asm, List<Symbol> localVariables, String startLabel)
			throws IOException, SyntaxException
	{
		// Define constants for local variables.
		int varOffset = 0;
		for (Symbol var : localVariables) {
			asm.addLabel(var.getGlobalName());
			asm.emit("equ", "" + (1 + varOffset));
			varOffset += var.getType().getSize();
		}

		// Label for function entry point.
		asm.addLabel(startLabel);

		// Allocate stack space for local variables.
		if (varOffset > 0)
			asm.emit("add", "sp", "=" + varOffset);

		// Push registers.
		asm.emit("pushr", "sp");
	}

	private void compileBody(Assembler asm, Scope scope, Registers registers)
			throws IOException, SyntaxException
	{
		// Compile statements directly, so that CompoundStatement doesn't create
		// new scope, and the statements are in the same scope as parameters.
		for (Statement st : body.getStatements())
			st.compile(asm, scope, registers);
	}

	private void compileEpilogue(Assembler asm, List<Symbol> localVariables,
			int paramTotalSize) throws IOException, SyntaxException
	{
		// Pop registers from stack.
		asm.addLabel(endSymbol.getReference());
		asm.emit("popr", "sp");

		// Remove local variables from stack.
		int localVarTotalSize = 0;
		for (Symbol var : localVariables)
			localVarTotalSize += var.getType().getSize();
		if (localVarTotalSize > 0)
			asm.emit("sub", "sp", "=" + localVarTotalSize);

		// Exit from function.
		asm.emit("exit", "sp", "=" + paramTotalSize);
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
		return "(FUNC " + returnTypeSpecifier + " " + declarator
				+ " " + body + ")";
	}

	/**
	 * Attempts to parse a function from token stream. If parsing fails the stream is reset to its
	 * initial position.
	 *
	 * @param tokens source token stream
	 * @return Function object or null if tokens don't form a valid function
	 */
	public static Function parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Function function = null;

		TypeSpecifier retTypeSpec = TypeSpecifier.parse(tokens);

		if (retTypeSpec != null) {
			Declarator declarator = Declarator.parse(tokens, false);
			if (declarator != null) {
				CompoundStatement body = CompoundStatement.parse(tokens);
				if (body != null)
					function = new Function(retTypeSpec, declarator, body, pos);
			}
		}

		tokens.popMark(function == null);
		return function;
	}
}
