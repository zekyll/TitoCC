package titocc.compiler.elements;

import java.io.IOException;
import java.util.Stack;
import titocc.compiler.Assembler;
import titocc.compiler.InternalSymbol;
import titocc.compiler.Register;
import titocc.compiler.Scope;
import titocc.compiler.Symbol;
import titocc.tokenizer.IdentifierToken;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Token;
import titocc.tokenizer.TokenStream;

public class Function extends Declaration implements Symbol
{
	private Type returnType;
	private String name;
	private ParameterList parameterList;
	private BlockStatement body;
	private String globallyUniqueName;
	InternalSymbol retValSymbol, endSymbol;

	public Function(Type returnType, String name, ParameterList parameterList,
			BlockStatement body, int line, int column)
	{
		super(line, column);
		this.returnType = returnType;
		this.name = name;
		this.parameterList = parameterList;
		this.body = body;
	}

	public Type getReturnType()
	{
		return returnType;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public ParameterList getParameterList()
	{
		return parameterList;
	}

	public BlockStatement getBody()
	{
		return body;
	}

	public int parameterCount()
	{
		return parameterList.getParameters().size();
	}

	@Override
	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		if (!scope.add(this))
			throw new SyntaxException("Redefinition of \"" + name + "\".", getLine(), getColumn());
		globallyUniqueName = scope.makeGloballyUniqueName(name);
		Scope functionScope = new Scope(scope, name + "_");

		// Add symbol for the function end so that return statements can jump
		// to it. Note that this must be done before compiling the body!
		endSymbol = new InternalSymbol("End", functionScope, ""); //__End
		functionScope.add(endSymbol);

		compilePrologue(asm, functionScope, registers);
		compileBody(asm, functionScope, registers);
		compileEpilogue(asm, functionScope, registers);
	}

	private void compilePrologue(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		// Add symbol for location of the return value
		retValSymbol = new InternalSymbol("Ret", scope, "(fp)"); //__Ret
		scope.add(retValSymbol);

		// Define constants for parameters and add their symbols
		asm.emit(retValSymbol.getGlobalName(), "equ", "-" + (parameterCount() + 2));
		parameterList.compile(asm, scope, registers);

		// Push registers
		asm.emit(getReference(), "pushr", "sp");
	}

	private void compileBody(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		// Compile statements directly, so that BlockStatement doesn't create
		// new scope, and the statements are in the same scope as parameters.
		for (Statement st : body.getStatements())
			st.compile(asm, scope, registers);
	}

	private void compileEpilogue(Assembler asm, Scope scope, Stack<Register> registers)
			throws IOException, SyntaxException
	{
		// Pop registers from stack
		asm.emit(endSymbol.getReference(), "popr", "sp");

		// Exit function
		asm.emit("", "exit", "sp", "=" + parameterCount());
	}

	@Override
	public String getGlobalName()
	{
		return globallyUniqueName;
	}

	@Override
	public String getReference()
	{
		return globallyUniqueName;
	}

	@Override
	public String toString()
	{
		return "(FUNC " + returnType + " " + name + " " + parameterList
				+ " " + body + ")";
	}

	public static Function parse(TokenStream tokens)
	{
		int line = tokens.getLine(), column = tokens.getColumn();
		tokens.pushMark();
		Function function = null;

		Type retType = Type.parse(tokens);

		if (retType != null) {
			Token id = tokens.read();
			if (id instanceof IdentifierToken) {
				ParameterList paramList = ParameterList.parse(tokens);
				if (paramList != null) {
					BlockStatement body = BlockStatement.parse(tokens);
					if (body != null) {
						function = new Function(retType, id.toString(), paramList,
								body, line, column);
					}
				}
			}
		}

		tokens.popMark(function == null);
		return function;
	}
}
