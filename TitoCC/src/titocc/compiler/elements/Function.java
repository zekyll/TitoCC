package titocc.compiler.elements;

import java.io.IOException;
import titocc.compiler.Assembler;
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
	public void compile(Assembler asm, Scope scope) throws IOException, SyntaxException
	{
		scope.add(this);
		Scope functionScope = new Scope(scope);

		compilePrologue(asm, functionScope);
		compileBody(asm, functionScope);
		compileEpilogue(asm, functionScope);
	}

	private void compilePrologue(Assembler asm, Scope scope) throws IOException, SyntaxException
	{
		asm.emit("__" + name + "_ret", "equ", "-" + (parameterCount() + 2));
		parameterList.compile(asm, scope);
		asm.emit(name, "pushr", "sp");
	}

	private void compileBody(Assembler asm, Scope scope) throws IOException, SyntaxException
	{
		// Compile statements directly, so that BlockStatement doesn't create
		// new scope, and the statements are in the same scope as parameters.
		for (Statement st : body.getStatements())
			st.compile(asm, scope);
	}

	private void compileEpilogue(Assembler asm, Scope scope) throws IOException, SyntaxException
	{
		String endLabel = "__" + name + "_end";
		asm.emit(endLabel, "popr", "sp");
		asm.emit("", "exit", "sp", "=" + parameterCount());
	}

	@Override
	public String getReference()
	{
		return name;
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
