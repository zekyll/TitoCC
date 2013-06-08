package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;
import org.junit.Test;
import titocc.compiler.elements.TranslationUnit;
import titocc.tokenizer.SyntaxException;
import titocc.tokenizer.Tokenizer;
import titocc.util.Position;

public class ParserTest
{
	private Tokenizer tokenizer;

	private String parse(String code) throws IOException, SyntaxException
	{
		tokenizer = new Tokenizer(new StringReader(code));
		TranslationUnit translationUnit = Parser.parse(tokenizer.tokenize());
		return translationUnit.toString();
	}

	private String inFunc(String s)
	{
		return "(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR f) (PRM_LIST)) (BLK_ST " + s + ")))";
	}

	@Test
	public void matchVariableDeclaration() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR abc) null))",
				parse("int abc;"));
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR _cba123) null))",
				parse("void _cba123;")); // legal in parser
	}

	@Test
	public void matchVariableDeclarationWithInitializer() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR xyz) (INT_EXPR 0 U)))",
				parse("int xyz = 0U;"));
	}

	@Test
	public void matchArrayDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR (DCLTOR xyz) (ID_EXPR a)) (INT_EXPR 3)) null))",
				parse("int xyz[a][3];"));
	}

	@Test
	public void matchPointerDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR (DCLTOR xyz))) null))",
				parse("int * * xyz;"));
	}

	@Test
	public void matchFunctionDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS int)"
				+ " (DCLTOR a)) (PRM (DS void) (DCLTOR b)))) null))",
				parse("int f(int a, void b);"));
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS int)"
				+ " (DCLTOR null)) (PRM (DS void) (DCLTOR null)))) null))",
				parse("int f(int, void);"));
	}

	@Test
	public void matchFunctionPointer() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR (DCLTOR (DCLTOR p)) (PRM_LIST (PRM"
				+ " (DS int) (DCLTOR null)))) null))",
				parse("void (*p)(int);"));
	}

	@Test
	public void matchReturnValueDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR (DCLTOR (DCLTOR f) (PRM_LIST)) (PRM_LIST"
				+ " (PRM (DS int) (DCLTOR null)))) null))",
				parse("void f()(int);"));
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR (DCLTOR f) (PRM_LIST))"
				+ " (INT_EXPR 6)) null))",
				parse("int f()[6];"));
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR (DCLTOR f) (PRM_LIST))) null))",
				parse("int* f();"));
	}

	@Test
	public void matchAbstractFunctionDeclartor() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS int)"
				+ " (DCLTOR null)))) null))",
				parse("void f(int());"));
	}

	@Test
	public void matchAbstractArrayDeclartor() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS void)"
				+ " (DCLTOR (DCLTOR null) (INT_EXPR 0))))) null))",
				parse("void f(void[0]);"));
	}

	@Test
	public void matchAbstractPointerDeclartor() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS void) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS int)"
				+ " (DCLTOR (DCLTOR null))))) null))",
				parse("void f(int*);"));
	}

	@Test
	public void matchBraceDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR xyz) null))",
				parse("int (xyz);"));
	}

	@Test
	public void matchComplexDeclaration() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR (DCLTOR (DCLTOR (DCLTOR xyz) (INT_EXPR 2))) (ID_EXPR a)) null))",
				parse("int (*xyz[2])[a];"));
	}

	@Test
	public void matchIdentifierExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (ID_EXPR a)))",
				parse("int x = a;"));
	}

	@Test
	public void matchBraceExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (ID_EXPR a)))",
				parse("int x = (a);"));
	}

	@Test
	public void matchFunctionCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (FCALL_EXPR"
				+ " (ID_EXPR bar) (ARG_LIST (ID_EXPR a) (INT_EXPR 7)))))",
				parse("int x = bar(a, 7);"));
	}

	@Test
	public void matchIntrinsicCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (INTR_EXPR out (ARG_LIST"
				+ " (INT_EXPR 2)))))",
				parse("int x = out(2);"));
	}

	@Test
	public void matchBinaryExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (BIN_EXPR +"
				+ " (BIN_EXPR - (ID_EXPR a) (INT_EXPR 7)) (INT_EXPR 4))))",
				parse("int x = a - 7 + 4;"));
	}

	@Test
	public void matchPrefixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (PRE_EXPR ! (PRE_EXPR --"
				+ " (ID_EXPR a)))))",
				parse("int x = !--a;"));
	}

	@Test
	public void matchPostfixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (POST_EXPR ++ (ID_EXPR y))))",
				parse("int x = y++;"));
	}

	@Test
	public void matchSubscriptExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (SUBSCR_EXPR (ID_EXPR a)"
				+ " (INT_EXPR 2))))",
				parse("int x = a[2];"));
	}

	@Test
	public void matchChainedPostfixExpressions() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (DS int) (DCLTOR x) (POST_EXPR -- (SUBSCR_EXPR"
				+ " (FCALL_EXPR (POST_EXPR -- (POST_EXPR ++ (ID_EXPR y))) (ARG_LIST))"
				+ " (INT_EXPR 2)))))",
				parse("int x = y++--()[2]--;"));
	}

	@Test
	public void matchFunction() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR foo) (PRM_LIST)) (BLK_ST)))",
				parse("void foo() {}"));
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR f) (BLK_ST)))",
				parse("void f {}"));
		assertEquals("(TRUNIT (FUNC (DS int) (DCLTOR (DCLTOR f) (INT_EXPR 7)) (BLK_ST)))",
				parse("int f[7] {}"));
	}

	@Test
	public void matchFunctionWithParameters() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR foo) (PRM_LIST (PRM"
				+ " (DS int) (DCLTOR a)) (PRM (DS int) (DCLTOR null)))) (BLK_ST)))",
				parse("void foo(int a, int) {}"));
	}

	@Test
	public void matchDeclaratorsInFunctionDefnParameters() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR f) (PRM_LIST (PRM"
				+ " (DS int) (DCLTOR (DCLTOR (DCLTOR a) (INT_EXPR 2)))))) (BLK_ST)))",
				parse("void f(int *a[2]) {}"));
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR f) (PRM_LIST (PRM (DS int)"
				+ " (DCLTOR null)))) (BLK_ST)))",
				parse("void f(int()) {}"));
	}

	@Test
	public void matchDeclaratorsInFunctionDefnReturnValue() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR (DCLTOR f) (PRM_LIST))) (BLK_ST)))",
				parse("void* f() {}"));
		assertEquals("(TRUNIT (FUNC (DS void) (DCLTOR (DCLTOR (DCLTOR (DCLTOR f) (PRM_LIST))"
				+ " (INT_EXPR 5)) (PRM_LIST (PRM (DS int) (DCLTOR null)))) (BLK_ST)))",
				parse("void f()[5](int) {}"));
	}

	@Test
	public void matchEmptyStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(BLK_ST)"),
				parse("void f() { ; }"));
	}

	@Test
	public void matchCompoundStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(BLK_ST (DECL_ST (VAR_DECL (DS int) (DCLTOR x) null)))"),
				parse("void f() { { int x; } }"));
	}

	@Test
	public void matchReturnStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(RET (INT_EXPR 2))"),
				parse("void f() { return 2; }"));
	}

	@Test
	public void matchDeclarationStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(DECL_ST (VAR_DECL (DS int) (DCLTOR x) null))"),
				parse("void f() { int x; }"));
		assertEquals(inFunc("(DECL_ST (VAR_DECL (DS void) (DCLTOR (DCLTOR a) (PRM_LIST)) null))"),
				parse("void f() { void a(); }"));
	}

	@Test
	public void matchEmptyReturnStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(RET null)"),
				parse("void f() { return; }"));
	}

	@Test
	public void matchIfStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(IF (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) null)"),
				parse("void f() { if(a) ++a; }"));
	}

	@Test
	public void matchIfElseStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(IF (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) (BLK_ST))"),
				parse("void f() { if(a) ++a; else ; }"));
	}

	@Test
	public void matchWhileStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(WHILE (BIN_EXPR == (ID_EXPR y) (INT_EXPR 2)) (BLK_ST))"),
				parse("void f() { while(y == 2) {} }"));
	}

	@Test
	public void matchDoStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(DO (CE (INT_EXPR 1) (ID_EXPR a)) (BLK_ST))"),
				parse("void f() { do ; while(1,a); }"));
	}

	@Test
	public void matchEmptyForStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(FOR (BLK_ST) null null (BLK_ST))"),
				parse("void f() { for(;;) {} }"));
	}

	@Test
	public void matchForStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(FOR (DECL_ST (VAR_DECL (DS int) (DCLTOR i) (INT_EXPR 0)))"
				+ " (BIN_EXPR < (ID_EXPR i) (ID_EXPR n)) (PRE_EXPR ++ (ID_EXPR i)) (BLK_ST))"),
				parse("void f() { for(int i = 0; i < n; ++i) {} }"));
		assertEquals(inFunc("(FOR (EXPR_ST (ASGN_EXPR = (ID_EXPR a) (ID_EXPR b))) null null"
				+ " (BLK_ST))"),
				parse("void f() { for(a = b; ; ) {} }"));
	}

	@Test
	public void matchBreakStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(BRK)"),
				parse("void f() { break; }"));
		assertEquals(inFunc("(FOR (BLK_ST) null null (BRK))"),
				parse("void f() { for(;;) break; }"));
	}

	@Test
	public void matchContinueStatement() throws IOException, SyntaxException
	{
		assertEquals(inFunc("(CONT)"),
				parse("void f() { continue; }"));
		assertEquals(inFunc("(WHILE (ID_EXPR a) (BLK_ST (CONT)))"),
				parse("void f() { while(a) { continue; } }"));
	}

	@Test
	public void throwsOnIllegalSyntax() throws IOException, SyntaxException
	{
		try {
			parse("void f()");
			fail("SyntaxException not thrown.");
		} catch (SyntaxException e) {
		}
	}

	private void testFailure(String code, String unexpectedToken, int line, int column)
			throws IOException
	{
		try {
			parse(code);
			fail("SyntaxException not thrown for following code: " + code);
		} catch (SyntaxException e) {
			assertEquals("Unexpected token \"" + unexpectedToken + "\".", e.getMessage());
			assertEquals(new Position(line, column), e.getPosition());
		}
	}

	@Test
	public void failAtArgumentListExpression() throws IOException, SyntaxException
	{
		testFailure("\nint x = foo(a,);", ")", 1, 14);
	}

	@Test
	public void failAtArgumentListClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nint x = foo(a;", ";", 1, 13);
	}

	@Test
	public void failAtAssignmentExprRhs() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { a = ; }", ";", 1, 17);
	}

	@Test
	public void failAtBinaryExprRhs() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { a + ; }", ";", 1, 17);
	}

	@Test
	public void failAtCompoundStatementClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { ; ", "<End of file>", 1, 15);
	}

	@Test
	public void failAtArrayDeclaratorExpression() throws IOException, SyntaxException
	{
		testFailure("\nint a[];", "]", 1, 6);
	}

	@Test
	public void failAtArrayDeclaratorClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nint a[2;", ";", 1, 7);
	}

	@Test
	public void failAtPointerDeclaratorRhs() throws IOException, SyntaxException
	{
		testFailure("\nint *;", ";", 1, 5);
	}

	@Test
	public void failAtParenthesizedDeclaratorContent() throws IOException, SyntaxException
	{
		testFailure("\nint ();", ")", 1, 5);
	}

	@Test
	public void failAtParenthesizedDeclaratorClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nint (a;", ";", 1, 6);
	}

	@Test
	public void failAtExpressionStatementSemicolon() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { 2 }", "}", 1, 15);
	}

	@Test
	public void failAtFunctionName() throws IOException, SyntaxException
	{
		testFailure("\nvoid () { }", ")", 1, 6); // "(" matches parenthesized declarator
	}

	@Test
	public void failAtFunctionParameterList() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo) { }", ")", 1, 8);
	}

	@Test
	public void failAtFunctionBody() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo()", "<End of file>", 1, 10);
	}

	@Test
	public void failAtFunctionCallExprArgumentList() throws IOException, SyntaxException
	{
		testFailure("\nint a = b c);", "c", 1, 10);
	}

	@Test
	public void failAtIntrinsicCallExprArgumentList() throws IOException, SyntaxException
	{
		testFailure("\nint a = in c);", "c", 1, 11);
	}

	@Test
	public void failAtIfStatementOpeningBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { if a == b) foo(); }", "a", 1, 16);
	}

	@Test
	public void failAtIfStatementExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { if () foo(); }", ")", 1, 17);
	}

	@Test
	public void failAtIfStatementClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { if (a == b foo(); }", "foo", 1, 24);
	}

	@Test
	public void failAtIfStatementTrueStatement() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { if (p) else foo(); }", "else", 1, 20);
	}

	@Test
	public void failAtIfStatementElseStatement() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { if (p) ; else }", "}", 1, 27);
	}

	@Test
	public void failAtParameterDeclarator() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo(int { }", "{", 1, 13);
	}

	@Test
	public void failAtParameterListParameter() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo(int a,) { }", ")", 1, 15);
	}

	@Test
	public void failAtParameterListClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo(int a { }", "{", 1, 15);
	}

	@Test
	public void failAtPrefixExprRhs() throws IOException, SyntaxException
	{
		testFailure("\nint x = !;", ";", 1, 9);
	}

	@Test
	public void failAtParenthesizedExprContents() throws IOException, SyntaxException
	{
		testFailure("\nint x = ();", ")", 1, 9);
	}

	@Test
	public void failAtParenthesizedExprClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nint x = (a;", ";", 1, 10);
	}

	@Test
	public void failAtReturnStatementSemicolon() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { return }", "}", 1, 20);
	}

	@Test
	public void failAtReturnStatementSemicolonAfterExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { return 0 }", "}", 1, 22);
	}

	@Test
	public void failAtSubscriptExprSubscript() throws IOException, SyntaxException
	{
		testFailure("\nint a = b[];", "]", 1, 10);
	}

	@Test
	public void failAtSubscriptExprClosingBracket() throws IOException, SyntaxException
	{
		testFailure("\nint a = b[c;", ";", 1, 11);
	}

	@Test
	public void failAtVariableDeclarationName() throws IOException, SyntaxException
	{
		testFailure("\nint ;", ";", 1, 4);
	}

	@Test
	public void failAtVariableDeclarationInitializer() throws IOException, SyntaxException
	{
		testFailure("\nint a =;", ";", 1, 7);
	}

	@Test
	public void failAtWhileStatementOpeningBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { while a == b) foo(); }", "a", 1, 19);
	}

	@Test
	public void failAtWhileStatementExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { while () ; }", ")", 1, 20);
	}

	@Test
	public void failAtWhileStatementClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { while (a == b foo(); }", "foo", 1, 27);
	}

	@Test
	public void failAtWhileStatementBody() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { while (p) }", "}", 1, 23);
	}

	@Test
	public void failAtDoStatementBody() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do } while(0); }", "}", 1, 16);
	}

	@Test
	public void failAtDoStatementWhileKeyword() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do { } (0); }", "(", 1, 20);
	}

	@Test
	public void failAtDoStatementOpeningBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do { } while a); }", "a", 1, 26);
	}

	@Test
	public void failAtDoStatementTestExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do { } while(); }", ")", 1, 26);
	}

	@Test
	public void failAtDoStatementClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do { } while(1; }", ";", 1, 27);
	}

	@Test
	public void failAtDoStatementSemicolon() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { do { } while(1) }", "}", 1, 29);
	}

	@Test
	public void failAtForStatementOpeningBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for a == b) foo(); }", "a", 1, 17);
	}

	@Test
	public void failAtForStatementInitStatement() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for ({}a;) ; }", "{", 1, 18);
		testFailure("\nvoid foo() { for (break;;) ; }", "break", 1, 18);
	}

	@Test
	public void failAtForStatementSemicolon1() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (int i = 0 a;) ; }", "a", 1, 28);
	}

	@Test
	public void failAtForStatementTestExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (;int x;) ; }", "int", 1, 19);
		testFailure("\nvoid foo() { for (;return;) ; }", "return", 1, 19);
	}

	@Test
	public void failAtForStatementSemicolon2() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (;a b) ; }", "b", 1, 21);
	}

	@Test
	public void failAtForStatementIncrExpression() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (;;int x) ; }", "int", 1, 20);
		testFailure("\nvoid foo() { for (;;continue) ; }", "continue", 1, 20);
	}

	@Test
	public void failAtForStatementClosingBrace() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (;;a foo(); }", "foo", 1, 22);
	}

	@Test
	public void failAtForStatementBody() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { for (;;) }", "}", 1, 22);
	}

	@Test
	public void failAtBreakStatementSemicolon() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { break }", "}", 1, 19);
	}

	@Test
	public void failAtContinueStatementSemicolon() throws IOException, SyntaxException
	{
		testFailure("\nvoid foo() { continue }", "}", 1, 22);
	}
}
