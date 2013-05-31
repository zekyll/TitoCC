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

	@Test
	public void matchVariableDeclaration() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR abc) null))",
				parse("int abc;"));
		assertEquals("(TRUNIT (VAR_DECL (TYPE void) (DCLTOR _cba123) null))",
				parse("void _cba123;")); // legal in parser
	}

	@Test
	public void matchVariableDeclarationWithInitializer() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR xyz) (INT_EXPR 0 U)))",
				parse("int xyz = 0U;"));
	}

	@Test
	public void matchArrayDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR (DCLTOR (DCLTOR xyz) (ID_EXPR a)) (INT_EXPR 3)) null))",
				parse("int xyz[a][3];"));
	}

	@Test
	public void matchPointerDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR (DCLTOR (DCLTOR xyz))) null))",
				parse("int * * xyz;"));
	}

	@Test
	public void matchBraceDeclarator() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR xyz) null))",
				parse("int (xyz);"));
	}

	@Test
	public void matchComplexDeclaration() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR (DCLTOR (DCLTOR (DCLTOR xyz) (INT_EXPR 2))) (ID_EXPR a)) null))",
				parse("int (*xyz[2])[a];"));
	}

	@Test
	public void matchIdentifierExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (ID_EXPR a)))",
				parse("int x = a;"));
	}

	@Test
	public void matchBraceExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (ID_EXPR a)))",
				parse("int x = (a);"));
	}

	@Test
	public void matchFunctionCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (FCALL_EXPR (ID_EXPR bar) (ARG_LIST (ID_EXPR a) (INT_EXPR 7)))))",
				parse("int x = bar(a, 7);"));
	}

	@Test
	public void matchIntrinsicCallExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (INTR_EXPR out (ARG_LIST (INT_EXPR 2)))))",
				parse("int x = out(2);"));
	}

	@Test
	public void matchBinaryExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (BIN_EXPR +"
				+ " (BIN_EXPR - (ID_EXPR a) (INT_EXPR 7)) (INT_EXPR 4))))",
				parse("int x = a - 7 + 4;"));
	}

	@Test
	public void matchPrefixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (PRE_EXPR ! (PRE_EXPR -- (ID_EXPR a)))))",
				parse("int x = !--a;"));
	}

	@Test
	public void matchPostfixExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (POST_EXPR ++ (ID_EXPR y))))",
				parse("int x = y++;"));
	}

	@Test
	public void matchSubscriptExpression() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (SUBSCR_EXPR (ID_EXPR a) (INT_EXPR 2))))",
				parse("int x = a[2];"));
	}

	@Test
	public void matchChainedPostfixExpressions() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (VAR_DECL (TYPE int) (DCLTOR x) (POST_EXPR -- (SUBSCR_EXPR"
				+ " (FCALL_EXPR (POST_EXPR -- (POST_EXPR ++ (ID_EXPR y))) (ARG_LIST)) (INT_EXPR 2)))))",
				parse("int x = y++--()[2]--;"));
	}

	@Test
	public void matchFunction() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) foo (PRM_LIST) (BLK_ST)))",
				parse("void foo() {}"));
	}

	@Test
	public void matchFunctionWithParameters() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) foo (PRM_LIST (PRM (TYPE int)"
				+ " (DCLTOR a)) (PRM (TYPE int) (DCLTOR b))) (BLK_ST)))",
				parse("void foo(int a, int b) {}"));
	}

	@Test
	public void matchDeclaratorsInParameters() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) foo (PRM_LIST (PRM (TYPE int)"
				+ " (DCLTOR (DCLTOR (DCLTOR a) (INT_EXPR 2))))) (BLK_ST)))",
				parse("void foo(int *a[2]) {}"));
	}

	@Test
	public void matchEmptyStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (BLK_ST))))",
				parse("void f() { ; }"));
	}

	@Test
	public void matchCompoundStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (BLK_ST"
				+ " (DECL_ST (VAR_DECL (TYPE int) (DCLTOR x) null))))))",
				parse("void f() { { int x; } }"));
	}

	@Test
	public void matchReturnStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (RET (INT_EXPR 2)))))",
				parse("void f() { return 2; }"));
	}

	@Test
	public void matchDeclarationStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (DECL_ST"
				+ " (VAR_DECL (TYPE int) (DCLTOR x) null)))))",
				parse("void f() { int x; }"));
	}

	@Test
	public void matchEmptyReturnStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (RET null))))",
				parse("void f() { return; }"));
	}

	@Test
	public void matchIfStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (IF"
				+ " (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) null))))",
				parse("void f() { if(a) ++a; }"));
	}

	@Test
	public void matchIfElseStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (IF"
				+ " (ID_EXPR a) (EXPR_ST (PRE_EXPR ++ (ID_EXPR a))) (BLK_ST)))))",
				parse("void f() { if(a) ++a; else ; }"));
	}

	@Test
	public void matchWhileStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (WHILE"
				+ " (BIN_EXPR == (ID_EXPR y) (INT_EXPR 2)) (BLK_ST)))))",
				parse("void f() { while(y == 2) {} }"));
	}

	@Test
	public void matchEmptyForStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (FOR (BLK_ST) null null (BLK_ST)))))",
				parse("void f() { for(;;) {} }"));
	}

	@Test
	public void matchForStatement() throws IOException, SyntaxException
	{
		assertEquals("(TRUNIT (FUNC (TYPE void) f (PRM_LIST) (BLK_ST (FOR"
				+ " (DECL_ST (VAR_DECL (TYPE int) (DCLTOR i) (INT_EXPR 0)))"
				+ " (BIN_EXPR < (ID_EXPR i) (ID_EXPR n)) (PRE_EXPR ++"
				+ " (ID_EXPR i)) (BLK_ST)))))",
				parse("void f() { for(int i = 0; i < n; ++i) {} }"));
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

	private void testFailure(String code, String unexpectedToken, int line, int column) throws IOException
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

//	@Test
//	public void failAtFunctionName() throws IOException, SyntaxException
//	{
//		testFailure("\nvoid () { }", "(", 1, 5);
//	}
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
	public void testEverything() throws IOException, SyntaxException
	{
		String code = "int foo(int a, int b) {"
				+ ";"
				+ "int a;"
				+ "int b = (3);"
				+ "5 * 2 + b--;"
				+ "b /= 5;"
				+ "{"
				+ "if(1u) a;"
				+ "if(0) return b; else {}"
				+ "}"
				+ "while(--i) f(i);"
				+ "return 7;"
				+ "}"
				+ "int x;";
		Tokenizer t = new Tokenizer(new StringReader(code));
		TranslationUnit tunit = Parser.parse(t.tokenize());
		assertEquals(""
				+ "(TRUNIT "
				+ "(FUNC (TYPE int) foo "
				+ "(PRM_LIST (PRM (TYPE int) (DCLTOR a)) (PRM (TYPE int) (DCLTOR b))) "
				+ "(BLK_ST "
				+ "(BLK_ST) "
				+ "(DECL_ST (VAR_DECL (TYPE int) (DCLTOR a) null)) "
				+ "(DECL_ST (VAR_DECL (TYPE int) (DCLTOR b) (INT_EXPR 3))) "
				+ "(EXPR_ST (BIN_EXPR + (BIN_EXPR * (INT_EXPR 5) (INT_EXPR 2)) (POST_EXPR -- (ID_EXPR b)))) "
				+ "(EXPR_ST (ASGN_EXPR /= (ID_EXPR b) (INT_EXPR 5))) "
				+ "(BLK_ST "
				+ "(IF (INT_EXPR 1 u) (EXPR_ST (ID_EXPR a)) null) "
				+ "(IF (INT_EXPR 0) (RET (ID_EXPR b)) (BLK_ST))"
				+ ") "
				+ "(WHILE (PRE_EXPR -- (ID_EXPR i)) (EXPR_ST (FCALL_EXPR (ID_EXPR f) (ARG_LIST (ID_EXPR i))))) "
				+ "(RET (INT_EXPR 7))"
				+ ")) "
				+ "(VAR_DECL (TYPE int) (DCLTOR x) null)"
				+ ")",
				tunit.toString());
	}
}
