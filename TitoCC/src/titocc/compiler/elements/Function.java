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
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Function declaration and definition. Forward declarations are not currently
 * supported so this is always both declaration and definition. Functions
 * consist of return type, function name, parameter list and a CompoundStatement
 * body.
 *
 * <p> EBNF definition:
 *
 * <br> FUNCTION = TYPE_SPECIFIER IDENTIFIER PARAMETER_LIST COMPOUND_STATEMENT
 */
public class Function extends Declaration
{
	/**
	 * Return type specifier. Note that this is just type specifier (void or
	 * int) because abstract declarators are not supported.
	 */
	private final TypeSpecifier returnType;

	/**
	 * Function name.
	 */
	private final String name;

	/**
	 * List of paremeters.
	 */
	private final ParameterList parameterList;

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
	 * Type of the function. Set when compiling the function.
	 */
	private CType type;

	/**
	 * Constructs a Function.
	 *
	 * @param returnType return type
	 * @param name function name
	 * @param parameterList parameter list
	 * @param body body of the function
	 * @param position starting position of the function
	 */
	public Function(TypeSpecifier returnType, String name,
			ParameterList parameterList, CompoundStatement body,
			Position position)
	{
		super(position);
		this.returnType = returnType;
		this.name = name;
		this.parameterList = parameterList;
		this.body = body;
	}

	/**
	 * Returns the return type.
	 *
	 * @return the return type
	 */
	public CType getReturnType()
	{
		return returnType.getType();
	}

	/**
	 * Returns the function name.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the function body.
	 *
	 * @return the function body
	 */
	public CompoundStatement getBody()
	{
		return body;
	}

	/**
	 * Returns the number of parameters.
	 *
	 * @return number of parameters
	 */
	public int getParameterCount()
	{
		return parameterList.getParameters().size();
	}

	@Override
	public void compile(Assembler asm, Scope scope, Registers regs)
			throws IOException, SyntaxException
	{
		asm.addEmptyLines(1);

		// Create new scope.
		Scope functionScope = new Scope(scope, name + "_");
		scope.addSubScope(functionScope);

		addInternalSymbols(functionScope);
		List<CType> paramTypes = compileParameters(asm, functionScope);
		type = new FunctionType(returnType.getType(), paramTypes);

		// Point of declaration is right after function's declarator.
		Symbol sym = addSymbol(scope);

		// Compile body before prologue because we want to know all the local
		// variables in the prologue.
		StringWriter bodyWriter = new StringWriter();
		Assembler bodyAsm = new Assembler(bodyWriter);
		compileBody(bodyAsm, functionScope, regs);
		List<Symbol> localVariables = getLocalVariables(functionScope);
		bodyAsm.finish();

		compilePrologue(asm, localVariables, sym.getReference());
		asm.getWriter().append(bodyWriter.toString());
		compileEpilogue(asm, localVariables);
	}

	private Symbol addSymbol(Scope scope) throws SyntaxException
	{
		Symbol sym = new Symbol(name, type, scope, "", Symbol.Category.Function);
		if (!scope.add(sym))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());
		return sym;
	}

	private void addInternalSymbols(Scope scope)
	{
		// Add symbol for the function end so that return statements can jump to
		// it.
		endSymbol = new Symbol("End", new VoidType(), scope,
				"", Symbol.Category.Internal); //__End
		scope.add(endSymbol);

		// Add symbol for location of the return value.
		retValSymbol = new Symbol("Ret", returnType.getType(), scope,
				"(fp)", Symbol.Category.Internal); //__Ret
		scope.add(retValSymbol);
	}

	private List<CType> compileParameters(Assembler asm, Scope scope)
			throws IOException, SyntaxException
	{
		// Define constants for return value and parameters and add their
		// symbols.
		asm.addLabel(retValSymbol.getGlobalName());
		asm.emit("equ", "-" + (getParameterCount() + 2));
		return parameterList.compile(asm, scope);
	}

	private void compilePrologue(Assembler asm, List<Symbol> localVariables,
			String startLabel) throws IOException, SyntaxException
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

	private void compileEpilogue(Assembler asm, List<Symbol> localVariables)
			throws IOException, SyntaxException
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
		asm.emit("exit", "sp", "=" + getParameterCount());
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
		return "(FUNC " + returnType + " " + name + " " + parameterList
				+ " " + body + ")";
	}

	/**
	 * Attempts to parse a function from token stream. If parsing fails the
	 * stream is reset to its initial position.
	 *
	 * @param tokens source token stream
	 * @return Function object or null if tokens don't form a valid function
	 */
	public static Function parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Function function = null;

		TypeSpecifier retType = TypeSpecifier.parse(tokens);

		if (retType != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				ParameterList paramList = ParameterList.parse(tokens);
				if (paramList != null) {
					CompoundStatement body = CompoundStatement.parse(tokens);
					if (body != null) {
						function = new Function(retType, id.toString(), paramList,
								body, pos);
					}
				}
			}
		}

		tokens.popMark(function == null);
		return function;
	}
}
