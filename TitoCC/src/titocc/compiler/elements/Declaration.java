package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.DeclarationType;
import titocc.compiler.IntermediateCompiler;
import titocc.compiler.Rvalue;
import titocc.compiler.Scope;
import titocc.compiler.StackAllocator;
import titocc.compiler.StorageClass;
import titocc.compiler.Symbol;
import titocc.compiler.types.ArrayType;
import titocc.compiler.types.CType;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.TokenStream;
import titocc.util.Position;

/**
 * Declares a function or an object, and in some cases defines the object. Consists of declaration
 * specifiers (giving the base type) and list of one or more init-declarators. Each init-declarator
 * has a declarator (modifies the base type and gives a variable name), and an optional initializer
 * expression. For global variables the initializer must be a compile time constant expression.
 *
 * <br> EBNF definition:
 *
 * <br> DECLARATION = DECLARATION_SPECIFIERS INIT_DECLARATOR_LIST ";"
 *
 * <br> INIT_DECLARATOR_LIST = INIT_DECLARATOR {"=" INIT_DECLARATOR}
 *
 * <br> INIT_DECLARATOR = DECLARATOR ["=" INIT_DECLARATOR]
 */
public class Declaration extends ExternalDeclaration
{
	private static class InitDeclarator extends CodeElement
	{
		/**
		 * Declarator which modifies the type and gives the variable name.
		 */
		final Declarator declarator;

		/**
		 * Optional initializer expression. Null if not used.
		 */
		final Expression initializer;

		InitDeclarator(Declarator declarator, Expression initializer, Position pos)
		{
			super(pos);
			this.declarator = declarator;
			this.initializer = initializer;
		}

		void compile(Assembler asm, IntermediateCompiler ic, Scope scope,
				StackAllocator stack, DeclarationType declType) throws SyntaxException, IOException
		{
			CType finalType = declarator.compile(declType.type, scope, null);
			if (!finalType.isObject())
				throw new SyntaxException("Variable must have object type.", getPosition());

			if (finalType instanceof ArrayType && initializer != null) {
				throw new SyntaxException("Array initializers are not supported.",
						initializer.getPosition());
			}

			Symbol sym = addSymbol(scope, declarator.getName(), finalType);

			if (initializer != null && !initializer.isAssignableTo(finalType, scope)) {
				throw new SyntaxException("Initializer type doesn't match variable type.",
						initializer.getPosition());
			}

			if (scope.isGlobal())
				compileGlobalVariable(asm, scope, sym);
			else
				compileLocalVariable(ic, scope, stack, sym, finalType);
		}

		private Symbol addSymbol(Scope scope, String name, CType type) throws SyntaxException
		{
			Symbol sym;
			if (scope.isGlobal())
				sym = new Symbol(name, type, Symbol.Category.GlobalVariable, null, false);
			else {
				sym = new Symbol(name, type, Symbol.Category.LocalVariable,
						StorageClass.Auto, false);
			}
			sym = scope.add(sym);

			if (!sym.define())
				throw new SyntaxException("Redefinition of \"" + name + "\".", getPosition());

			return sym;
		}

		private void compileGlobalVariable(Assembler asm, Scope scope, Symbol sym)
				throws SyntaxException, IOException
		{
			BigInteger initValue;
			if (initializer != null) {
				initValue = initializer.getCompileTimeValue(scope);
				if (initValue == null) {
					throw new SyntaxException("Global variable must be initialized with a compile"
							+ " time constant.", initializer.getPosition());
				}
			} else
				initValue = BigInteger.ZERO;

			asm.addLabel(sym.getGlobalName());
			if (sym.getType() instanceof ArrayType)
				asm.emit("ds", "" + sym.getType().getSize());
			else
				asm.emit("dc", "" + initValue.intValue());
		}

		private void compileLocalVariable(IntermediateCompiler ic, Scope scope, StackAllocator stack,
				Symbol sym, CType variableType) throws SyntaxException
		{
			if (initializer != null) {
				Rvalue initVal = initializer.compileWithConversion(ic, scope, variableType);
				ic.emit("store", initVal.getRegister(), sym.getRhsOperand(false));
			}
		}
	}

	/**
	 * Declaration specifiers, giving the storage class and part of the type.
	 */
	private final DeclarationSpecifiers declarationSpecifiers;

	/**
	 * Init-declarator list.
	 */
	List<InitDeclarator> initDeclList;

	/**
	 * Constructs a Declaration.
	 *
	 * @param declarationSpecifiers declaration specifiers
	 * @param declarator declarator of the variable
	 * @param initializer initializer expression or null if the variable is left uninitialized
	 * @param position starting position of the declaration
	 */
	public Declaration(DeclarationSpecifiers declarationSpecifiers,
			List<InitDeclarator> initDeclList, Position position)
	{
		super(position);
		this.declarationSpecifiers = declarationSpecifiers;
		this.initDeclList = initDeclList;
	}

	@Override
	public void compile(Assembler asm, IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException, IOException
	{
		DeclarationType declType = declarationSpecifiers.compile(scope);

		for (InitDeclarator initDecl : initDeclList)
			initDecl.compile(asm, ic, scope, stack, declType);
	}

	@Override
	public String toString()
	{
		String str = "(VAR_DECL " + declarationSpecifiers;
		for (InitDeclarator initDecl : initDeclList)
			str += " " + initDecl.declarator + " " + initDecl.initializer;
		return str + ")";
	}

	/**
	 * Attempts to parse a declaration from token stream. If parsing fails the stream is reset to
	 * its initial position.
	 *
	 * @param tokens source token stream
	 * @return Declaration object or null if tokens don't form a valid declaration
	 */
	public static Declaration parse(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		Declaration varDeclaration = null;

		DeclarationSpecifiers declSpecifiers = DeclarationSpecifiers.parse(tokens);
		if (declSpecifiers != null) {
			List<InitDeclarator> initDeclList = parseInitDeclaratorList(tokens);
			if (initDeclList != null) {
				if (tokens.read().toString().equals(";"))
					varDeclaration = new Declaration(declSpecifiers, initDeclList, pos);
			}
		}

		tokens.popMark(varDeclaration == null);
		return varDeclaration;
	}

	private static List<InitDeclarator> parseInitDeclaratorList(TokenStream tokens)
	{
		tokens.pushMark();
		Expression init = null;

		List<InitDeclarator> initDeclList = null;
		InitDeclarator initDecl = parseInitDeclarator(tokens);
		while (initDecl != null) {
			if (initDeclList == null)
				initDeclList = new ArrayList<InitDeclarator>();
			initDeclList.add(initDecl);

			tokens.pushMark();
			initDecl = null;
			if (tokens.read().toString().equals(","))
				initDecl = parseInitDeclarator(tokens);
			tokens.popMark(initDecl == null);
		}

		tokens.popMark(initDeclList == null);
		return initDeclList;
	}

	private static InitDeclarator parseInitDeclarator(TokenStream tokens)
	{
		Position pos = tokens.getPosition();
		tokens.pushMark();
		InitDeclarator initDecl = null;

		Declarator declarator = Declarator.parse(tokens, true, false);
		if (declarator != null)
			initDecl = new InitDeclarator(declarator, parseInitializer(tokens), pos);

		tokens.popMark(initDecl == null);
		return initDecl;
	}

	private static Expression parseInitializer(TokenStream tokens)
	{
		tokens.pushMark();
		Expression initializer = null;

		if (tokens.read().toString().equals("="))
			initializer = AssignmentExpression.parse(tokens);

		tokens.popMark(initializer == null);
		return initializer;
	}
}
