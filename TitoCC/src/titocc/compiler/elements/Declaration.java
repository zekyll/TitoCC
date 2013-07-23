package titocc.compiler.elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import titocc.compiler.Assembler;
import titocc.compiler.DeclarationResult;
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
			declType = declarator.compile(declType, scope, null);
			if (!declType.type.isObject() && !declType.type.isFunction()) {
				throw new SyntaxException("Declaration does not specify an object or a function.",
						getPosition());
			}

			Symbol sym = declare(scope, declarator.getName(), declType);

			define(scope, sym);

			if (initializer != null) {
				checkInitializer(scope, sym);
				if (sym.getType().isObject()) {
					if (scope.isGlobal() || sym.getStorageClass() == StorageClass.Static)
						compileStaticObject(asm, ic, scope, sym);
					else
						compileAutomaticObject(ic, scope, stack, sym, sym.getType());
				}
			}
		}

		private Symbol declare(Scope scope, String name, DeclarationType declType)
				throws SyntaxException
		{
			// Determine category and storage class.
			StorageClass storageCls = declType.storageClass;
			if (declType.type.isFunction()) {
				// Block scope functions can only have extern or no storage class. ($6.7.1/5)
				if (!scope.isGlobal() && storageCls != null && storageCls != StorageClass.Extern) {
					throw new SyntaxException("Illegal storage class in block scope function "
							+ "declaration.", getPosition());
				}
			} else if (!scope.isGlobal()) {
				// No storage class on local variables is the same as "auto" storage class.
				if (declType.storageClass == null)
					storageCls = StorageClass.Auto;
			}

			// Auto and register not allowed in external declarations. ($6.9/2)
			if (scope.isGlobal() && (storageCls == StorageClass.Auto
					|| storageCls == StorageClass.Register)) {
				throw new SyntaxException("Illegal storage class in external declaration.",
						getPosition());
			}

			Symbol sym = new Symbol(name, declType.type, storageCls, declType.inline);

			DeclarationResult declRes = scope.add(sym);
			if (declRes.symbol == null)
				throw new SyntaxException(declRes.msg, getPosition());

			return declRes.symbol;
		}

		private void define(Scope scope, Symbol sym) throws SyntaxException
		{
			if (!sym.getType().isObject())
				return;

			if (initializer != null || sym.getStorageClass() == StorageClass.Auto
					|| sym.getStorageClass() == StorageClass.Register) {
				// Local object declaration, or global declaration with initializer is considered
				// a definition.
				if (!sym.define()) {
					throw new SyntaxException("Redefinition of \"" + sym.getName() + "\".",
							getPosition());
				}
			} else if (sym.getStorageClass() == StorageClass.Static
					|| sym.getStorageClass() == null) {
				// File-scope object declaration without initializer and with static or no
				// storage class is a "tentative definition" ($6.9.2/2)
				sym.defineTentatively();
			}
		}

		private void checkInitializer(Scope scope, Symbol sym) throws SyntaxException
		{
			Position pos = initializer.getPosition();
			if (sym.getType().isFunction())
				throw new SyntaxException("Initializer in function declaration.", pos);
			if (sym.getType() instanceof ArrayType)
				throw new SyntaxException("Array initializers are not supported.", pos);
			if (!initializer.isAssignableTo(sym.getType(), scope))
				throw new SyntaxException("Initializer type doesn't match variable type.", pos);
			// Block-scope objects with linkage may not have an initializer. ($6.7.8/5)
			if (!scope.isGlobal() && sym.getStorageClass() == StorageClass.Extern)
				throw new SyntaxException("Initializer on block-scope object with linkage.", pos);
		}

		private void compileStaticObject(Assembler asm, IntermediateCompiler ic, Scope scope,
				Symbol sym) throws SyntaxException, IOException
		{
			// ($6.7.8/4)
			BigInteger initValue = initializer.getCompileTimeValue(scope);
			if (initValue == null) {
				throw new SyntaxException("Initializer for static object is not a constant.",
						initializer.getPosition());
			}

			if (asm != null) {
				asm.addEmptyLines(1);
				asm.addLabel(sym.getGlobalName());
				if (sym.getType() instanceof ArrayType)
					asm.emit("ds", "" + sym.getType().getSize());
				else
					asm.emit("dc", "" + initValue.intValue());
			} else {
				ic.addLabel(sym.getGlobalName());
				if (sym.getType() instanceof ArrayType)
					ic.emit("ds", sym.getType().getSize());
				else
					ic.emit("dc", initValue.intValue());
			}
		}

		private void compileAutomaticObject(IntermediateCompiler ic, Scope scope,
				StackAllocator stack, Symbol sym, CType variableType) throws SyntaxException
		{
			Rvalue initVal = initializer.compileWithConversion(ic, scope, variableType);
			ic.emit("store", initVal.getRegister(), sym.getRhsOperand(false));
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
	public void compile(Assembler asm, Scope scope) throws SyntaxException, IOException
	{
		compile(asm, null, scope, null);
	}

	/**
	 * Compiles a block-scope declaration.
	 *
	 * @param ic intermediate compiler used for code generation
	 * @param scope scope of the declaration
	 * @param stack stack allocator
	 * @throws SyntaxException
	 */
	public void compile(IntermediateCompiler ic, Scope scope, StackAllocator stack)
			throws SyntaxException
	{
		try {
			compile(null, ic, scope, stack);
		} catch (IOException e) {
			// Never thrown with local declarations.
		}
	}

	private void compile(Assembler asm, IntermediateCompiler ic, Scope scope, StackAllocator stack)
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
