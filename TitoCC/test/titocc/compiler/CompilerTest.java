package titocc.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import static org.junit.Assert.*;
import org.junit.Test;
import titocc.compiler.Compiler;
import titocc.tokenizer.SyntaxException;
import titocc.util.Position;

public class CompilerTest
{
	private String compile(String s) throws IOException, SyntaxException
	{
		Compiler c = new Compiler(new StringReader(s));
		StringWriter writer = new StringWriter();
		c.compile(writer);
		return writer.toString();
	}

	private void testErr(String src, String msg, int line, int column) throws IOException
	{
		try {
			compile(src);
		} catch (SyntaxException e) {
			assertEquals(msg, e.getMessage());
			assertEquals(new Position(line, column), e.getPosition());
		}
	}

	@Test
	public void errorWhenMainNotFound() throws IOException
	{
		testErr("int main2() {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenMainReturnTypeWrong() throws IOException
	{
		testErr("void main2() {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenMainNotFunction() throws IOException
	{
		testErr("\nint main;", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenMainParamCountWrong() throws IOException
	{
		testErr("\nint main(int a) {}", "Function \"int main()\" was not found.", 0, 0);
	}

	@Test
	public void errorWhenAssigningToLiteral() throws IOException
	{
		testErr("\nint main() { 0 = 1; }", "Operation requires an lvalue.", 1, 13);
	}

	@Test
	public void errorWhenAssigningToExpression() throws IOException
	{
		testErr("\nint main() { (1 + 2) = 1; }", "Operation requires an lvalue.", 1, 14);
	}

	@Test
	public void errorWhenAssigningToFunction() throws IOException
	{
		testErr("\nint main() { main = 1; }", "Incompatible operands for operator =.", 1, 13);
	}

	@Test
	public void errorWhenUsingUndeclaredVariable() throws IOException
	{
		testErr("\nint main() { x; }", "Undeclared identifier \"x\".", 1, 13);
		testErr("\nint main() { { int x; } x; }", "Undeclared identifier \"x\".", 1, 24);
	}

	@Test
	public void errorWhenUsingUndeclaredFunction() throws IOException
	{
		testErr("\nint main() { f(); }", "Undeclared identifier \"f\".", 1, 13);
	}

	@Test
	public void errorWhenRedefiningAFunction() throws IOException
	{
		testErr("\nint main() { }\nvoid main() {}", "Redefinition of \"main\".", 2, 0);
	}

	@Test
	public void errorWhenVoidUsedInExpression() throws IOException
	{
		testErr("\nvoid f() { 2 + f(); }", "Incompatible operands for operator +.", 1, 11);
		testErr("\nvoid f() { !f(); }", "Operator ! requires a scalar type.", 1, 11);
		testErr("\nvoid f() { int x; x = f(); }", "Incompatible operands for operator =.", 1, 18);
		testErr("\nvoid f() { if(f()); }", "Scalar expression required.", 1, 14);
		testErr("\nvoid f() { while(f()); }",
				"While loop control expression must have scalar type.", 1, 17);
		testErr("\nvoid f() { int x = f(); }",
				"Initializer type doesn't match variable type.", 1, 11);
		testErr("\nvoid f(int a) { f(out()); }",
				"Argument type doesn't match type of the parameter.", 1, 18);
	}

	@Test
	public void errorWhenCallingNonFunction() throws IOException
	{
		testErr("\nvoid f() { 0(); }",
				"Expression does not evaluate to a function pointer.", 1, 11);
		testErr("\nvoid f() { int x; x(); }",
				"Expression does not evaluate to a function pointer.", 1, 18);
	}

	@Test
	public void errorWhenWrongNumberOfArguments() throws IOException
	{
		testErr("\nvoid f() { f(1); }",
				"Number of arguments doesn't match the number of parameters.", 1, 12);
		testErr("\nvoid f(int a) { f(); }",
				"Number of arguments doesn't match the number of parameters.", 1, 17);
	}

	@Test
	public void errorWhenWrongNumberOfArgumentsForIntrinsics() throws IOException
	{
		testErr("\nvoid f() { in(1); }",
				"Number of arguments doesn't match the number of parameters.", 1, 11);
		testErr("\nvoid f() { out(); }",
				"Number of arguments doesn't match the number of parameters.", 1, 11);
	}

	@Test
	public void errorWhenUsingSuffixOnLiteral() throws IOException
	{
		testErr("\nvoid f() { 1u; }", "Suffixes on literals are not supported.", 1, 11);
	}

	@Test
	public void errorWhenUsingVoidParameter() throws IOException
	{
		testErr("\nvoid f(void a) { }", "Parameter must have object type.", 1, 7);
	}

	@Test
	public void errorWhenRedefiningAParameter() throws IOException
	{
		// should this be col 18 instead?
		testErr("\nvoid f(int a, int a) { }", "Redefinition of \"a\".", 1, 14);
	}

	@Test
	public void errorWhenIncrementingNonLValue() throws IOException
	{
		testErr("\nvoid f() { 0++; }", "Operation requires an lvalue.", 1, 11);
		testErr("\nvoid f() { ++0; }", "Operation requires an lvalue.", 1, 13);
	}

	@Test
	public void errorWhenDecrementingNonLValue() throws IOException
	{
		testErr("\nvoid f() { 0--; }", "Operation requires an lvalue.", 1, 11);
		testErr("\nvoid f() { --0; }", "Operation requires an lvalue.", 1, 13);
	}

	@Test
	public void errorWhenRedefiningAVariable() throws IOException
	{
		String msg = "Redefinition of \"a\".";
		testErr("\nvoid f() { int a = 0; int a; }", msg, 1, 22);
		testErr("\nvoid f(int a) { int a; }", msg, 1, 16);
		testErr("\nvoid f() { if(1){int a; int a;} }", msg, 1, 24);
		testErr("\nvoid f() { if(1);else{int a; int a;} }", msg, 1, 29);
		testErr("\nvoid f() { for(;;) {int a; int a;} }", msg, 1, 27);
		testErr("\nvoid f() { while(1) {int a; int a;} }", msg, 1, 28);
		testErr("\nvoid f() { do{int a; int a;}while(1); }", msg, 1, 21);
		testErr("\nvoid f() { {int a; int a;} }", msg, 1, 19);
	}

	@Test
	public void errorWhenGlobalVariableInitializerNotConstant() throws IOException
	{
		testErr("\nint a; int b = a;",
				"Global variable must be initialized with a compile time constant.", 1, 7);
	}

	@Test
	public void errorWhenArrayHasInitializer() throws IOException
	{
		testErr("\nint a[2] = 0;", "Array initializers are not supported.", 1, 0);
	}

	@Test
	public void errorWhenVariableIsNotObject() throws IOException
	{
		testErr("\nvoid a;", "Variable must have object type.", 1, 0);
	}

	@Test
	public void errorWhenArrayElementIsNotObject() throws IOException
	{
		testErr("\nvoid a[2];", "Array elements must have object type.", 1, 5);
	}

	@Test
	public void errorWhenArraySizeNotConstant() throws IOException
	{
		testErr("\nint s = 2; int a[s];", "Array length must be a compile time constant.", 1, 15);
	}

	@Test
	public void errorWhenArraySizeIsNotPositive() throws IOException
	{
		testErr("\nint a[0];", "Array length must be a positive integer.", 1, 4);
		testErr("\nint a[-1];", "Array length must be a positive integer.", 1, 4);
	}

	@Test
	public void errorWhenParameterIsArray() throws IOException
	{
		testErr("\nvoid f(int *x[2]) {}", "Array parameters are not supported.", 1, 7);
	}

//	@Test
//	public void errorWhenUsingFunctionNameInParameterList() throws IOException
//	{
//
//	}
//	@Test
//	public void errorWhenUsingVariableNameInDeclarator() throws IOException
//	{
//		testErr("\nint a[a];", "", 1, 1);
//	}
	@Test
	public void errorWhenIllegalOperandsForIncrement() throws IOException
	{
		String msg = "Operator ++ requires an arithmetic or object pointer type.";
		testErr("\nint f() { void* a; a++; }", msg, 1, 19);
		testErr("\nint f() { void* a; ++a; }", msg, 1, 19);
		testErr("\nint f() { f++; }", msg, 1, 10);
		testErr("\nint f() { ++f; }", msg, 1, 10);
		testErr("\nvoid f() { f()++; }", msg, 1, 11);
		testErr("\nvoid f() { ++f(); }", msg, 1, 11);
		String msg2 = "Array cannot be used as an lvalue.";
		testErr("\nint f() { int a[2]; ++a; }", msg2, 1, 22);
		testErr("\nint f() { int a[2]; a++; }", msg2, 1, 20);
	}

	@Test
	public void errorWhenIllegalOperandsForDecrement() throws IOException
	{
		String msg = "Operator -- requires an arithmetic or object pointer type.";
		testErr("\nint f() { void* a; a--; }", msg, 1, 19);
		testErr("\nint f() { void* a; --a; }", msg, 1, 19);
		testErr("\nint f() { f--; }", msg, 1, 10);
		testErr("\nint f() { --f; }", msg, 1, 10);
		testErr("\nvoid f() { f()--; }", msg, 1, 11);
		testErr("\nvoid f() { --f(); }", msg, 1, 11);
		String msg2 = "Array cannot be used as an lvalue.";
		testErr("\nint f() { int a[2]; --a; }", msg2, 1, 22);
		testErr("\nint f() { int a[2]; a--; }", msg2, 1, 20);
	}

	@Test
	public void errorWhenIllegalOperandsForAssignment() throws IOException
	{
		String msg = "Incompatible operands for operator =.";
		testErr("\nint f() { int a[2]; a=1; }", msg, 1, 20);
		testErr("\nint f() { int* a; a=1; }", msg, 1, 18);
		testErr("\nint f() { void* a; a=1; }", msg, 1, 19);
		testErr("\nint f() { int* a; int** b; a=b; }", msg, 1, 27);
		testErr("\nint f() { int* a; int b[2][2]; a=b; }", msg, 1, 31);
		testErr("\nvoid f() { int a; a = f(); }", msg, 1, 18);
		testErr("\nvoid f() { f() = f(); }", msg, 1, 11);
		String msg2 = "Array cannot be used as an lvalue.";
		testErr("\nint f() { int a[2]; a = 0; }", msg2, 1, 20);
		testErr("\nint f() { int a[2]; int b[2]; a = b; }", msg2, 1, 30);
		testErr("\nint f() { int a[2]; int* b; a = b; }", msg2, 1, 28);
	}

	@Test
	public void errorWhenIllegalInitializerType() throws IOException
	{
		String msg = "Initializer type doesn't match variable type.";
		testErr("\nint f() { int* a = 1; }", msg, 1, 10);
		testErr("\nint f() { void* a = 1; }", msg, 1, 10);
		testErr("\nint f() { int** b; int* a = b; }", msg, 1, 19);
		testErr("\nint f() { int b[2][2]; int *a = b; }", msg, 1, 23);
		testErr("\nvoid f() { int a = f(); }", msg, 1, 11);
	}

	@Test
	public void errorWhenIllegalReturnExpression() throws IOException
	{
		testErr("\nint f() { return; }", "Function must return a value.", 1, 10);
		testErr("\nvoid f() {} void g() { return f(); }",
				"Returned expression doesn't match return value type.", 1, 23);
		testErr("\nint f() { int*a; return a; }",
				"Returned expression doesn't match return value type.", 1, 17);
	}

	@Test
	public void errorWhenIllegalArgumentType() throws IOException
	{
		String msg = "Argument type doesn't match type of the parameter.";
		testErr("\nvoid f(int a) { f(f()); }", msg, 1, 18);
		testErr("\nvoid f(int* a) { f(1); }", msg, 1, 19);
		testErr("\nvoid f(void* a) { f(1); }", msg, 1, 20);
		testErr("\nint f(int*a) { int**b; f(b); }", msg, 1, 25);
		testErr("\nint f(int*a) { int b[2][3]; f(b); }", msg, 1, 30);
	}

	@Test
	public void errorWhenIllegalOperandsForAddAssignment() throws IOException
	{
		String msg = "Incompatible operands for operator +=.";
		testErr("\nint f() { int a[2]; int b[2]; a+=b; }", msg, 1, 30);
		testErr("\nint f() { int* a; int* b; a+=b; }", msg, 1, 26);
		testErr("\nint f() { void* a; a+=1; }", msg, 1, 19);
		String msg2 = "Array cannot be used as an lvalue.";
		testErr("\nint f() { int a[2]; a+=1; }", msg2, 1, 20);
	}

	@Test
	public void errorWhenIllegalOperandsForSubtractAssignment() throws IOException
	{
		String msg = "Incompatible operands for operator -=.";
		testErr("\nint f() { int a[2]; int b[2]; a-=b; }", msg, 1, 30);
		testErr("\nint f() { int* a; int* b; a-=b; }", msg, 1, 26);
		testErr("\nint f() { void* a; a-=1; }", msg, 1, 19);
		String msg2 = "Array cannot be used as an lvalue.";
		testErr("\nint f() { int a[2]; a-=1; }", msg2, 1, 20);
	}

	@Test
	public void errorWhenIllegalOperandsForBitwiseLogicalAssignment() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nint f() { int* a; a &= 1; }", msg + "&=.", 1, 18);
		testErr("\nint f() { int* a; int*b; a |= b; }", msg + "|=.", 1, 25);
		testErr("\nint f() { int a; int*b; a ^= b; }", msg + "^=.", 1, 24);
		testErr("\nvoid f() { int a; a &= f(); }", msg + "&=.", 1, 18);
	}

	@Test
	public void errorWhenIllegalOperandsForArithmeticAssignment() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nint f() { int* a; a <<= 1; }", msg + "<<=.", 1, 18);
		testErr("\nint f() { int* a; int*b; a %= b; }", msg + "%=.", 1, 25);
		testErr("\nint f() { int a; int*b; a >>= b; }", msg + ">>=.", 1, 24);
		testErr("\nvoid f() { int a; a /= f(); }", msg + "/=.", 1, 18);
		testErr("\nvoid f() { int a; int b[2]; a /= b; }", msg + "/=.", 1, 28);
		testErr("\nvoid f() { int a[2]; int b; a *= b; }", msg + "*=.", 1, 28);
	}

	@Test
	public void errorWhenIllegalOperandForUnaryPlus() throws IOException
	{
		String msg = "Operator + requires an arithmetic type.";
		testErr("\nvoid f() { int* a;   +a;   }", msg, 1, 21);
		testErr("\nvoid f() { int a[2]; +a;   }", msg, 1, 21);
		testErr("\nvoid f() { int a[2]; +f(); }", msg, 1, 21);
	}

	@Test
	public void errorWhenIllegalOperandUnaryMinus() throws IOException
	{
		String msg = "Operator - requires an arithmetic type.";
		testErr("\nvoid f() { int* a;   -a;   }", msg, 1, 21);
		testErr("\nvoid f() { int a[2]; -a;   }", msg, 1, 21);
		testErr("\nvoid f() { int a[2]; -f(); }", msg, 1, 21);
	}

	@Test
	public void errorWhenIllegalOperandForLogicalNegation() throws IOException
	{
		testErr("\nvoid f() { !f(); }", "Operator ! requires a scalar type.", 1, 11);
	}

	@Test
	public void errorWhenIllegalOperandForBitwiseNegation() throws IOException
	{
		testErr("\nvoid f() { int* a;   ~a;   }", "Operator ~ requires an integer type.", 1, 21);
		testErr("\nvoid f() { int a[2]; ~a;   }", "Operator ~ requires an integer type.", 1, 21);
		testErr("\nvoid f() { int a[2]; ~f(); }", "Operator ~ requires an integer type.", 1, 21);
	}

	@Test
	public void errorWhenIllegalOperandForAddressOf() throws IOException
	{
		testErr("\nvoid f() { &2;   }", "Operation requires an lvalue.", 1, 12);
		testErr("\nvoid f() { &f(); }", "Operation requires an lvalue.", 1, 12);
	}

	@Test
	public void errorWhenIllegalOperandForDereference() throws IOException
	{
		String msg = "Invalid operand for operator *. Pointer type required.";
		testErr("\nvoid f() { *3;   }", msg, 1, 11);
		testErr("\nvoid f() { *f(); }", msg, 1, 11);
	}

	@Test
	public void errorWhenIllegalOperandsForSubscript() throws IOException
	{
		String msg = "Operator [] requires an object pointer.";
		testErr("\nvoid f() {                   3[2];       }", msg, 1, 29);
		testErr("\nvoid f() {                   f()[2];       }", msg, 1, 29);
		testErr("\nvoid f() {                   2[f()];       }", msg, 1, 29);
		String msg2 = "Operator [] requires an integer operand.";
		testErr("\nvoid f() { int* a; int* b;   a[b];       }", msg2, 1, 29);
		testErr("\nvoid f() { int* a; int b[2]; b[a];      }", msg2, 1, 29);
		testErr("\nvoid f() { int* a;           a[f()];  }", msg2, 1, 29);
	}

	@Test
	public void errorWhenIllegalOperandsForLogicalBinaryOperator() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() { 1 && f(); }", msg + "&&.", 1, 11);
		testErr("\nvoid f() { f() || 1; }", msg + "||.", 1, 11);
	}

	@Test
	public void errorWhenIllegalOperandsForEqualityOperator() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                      1 == f(); }", msg + "==.", 1, 32);
		testErr("\nvoid f() {                      f() != 1; }", msg + "!=.", 1, 32);
		testErr("\nvoid f() { void * a;            1 == a;   }", msg + "==.", 1, 32);
		testErr("\nvoid f() { void * a;            a != 1;   }", msg + "!=.", 1, 32);
		testErr("\nvoid f() { int* a; int (*b)[2]; b == a;   }", msg + "==.", 1, 32);
		testErr("\nvoid f() { int* a; int (*b)[2]; a != b;   }", msg + "!=.", 1, 32);
	}

	@Test
	public void errorWhenIllegalOperandsForRelationalOperator() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                      1 < f();      }", msg + "<.", 1, 32);
		testErr("\nvoid f() {                      f() <= 1;     }", msg + "<=.", 1, 32);
		testErr("\nvoid f() { void * a;            1 > a;        }", msg + ">.", 1, 32);
		testErr("\nvoid f() { void * a;            a >= 1;       }", msg + ">=.", 1, 32);
		testErr("\nvoid f() { int* a; int (*b)[2]; b < a;        }", msg + "<.", 1, 32);
		testErr("\nvoid f() { int* a; int (*b)[2]; a > b;        }", msg + ">.", 1, 32);
		testErr("\nvoid f() { int a[2];            a[0] < &a[0]; }", msg + "<.", 1, 32);
		testErr("\nvoid f() { int a[2];            &a[0] > a[0]; }", msg + ">.", 1, 32);
		testErr("\nvoid f() { int* a; void* b;     b < a;        }", msg + "<.", 1, 32);
		testErr("\nvoid f() { int* a; void* b;     a > b;        }", msg + ">.", 1, 32);
	}

	@Test
	public void errorWhenIllegalOperandsForAdd() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                  0 + f(); }", msg + "+.", 1, 28);
		testErr("\nvoid f() {                  f() + 0; }", msg + "+.", 1, 28);
		testErr("\nvoid f() { void * a;        0 + a;   }", msg + "+.", 1, 28);
		testErr("\nvoid f() { void * a;        a + 0;   }", msg + "+.", 1, 28);
		testErr("\nvoid f() { int* a; void* b; b + a;   }", msg + "+.", 1, 28);
		testErr("\nvoid f() { int* a; void* b; a + b;   }", msg + "+.", 1, 28);
		testErr("\nvoid f() { int* a; int* b;  a + b;   }", msg + "+.", 1, 28);
	}

	@Test
	public void errorWhenIllegalOperandsForSubtract() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                  0 - f();      }", msg + "-.", 1, 28);
		testErr("\nvoid f() {                  f() - 0;      }", msg + "-.", 1, 28);
		testErr("\nvoid f() { void * a;        0 - a;        }", msg + "-.", 1, 28);
		testErr("\nvoid f() { void * a;        a - 0;        }", msg + "-.", 1, 28);
		testErr("\nvoid f() { int* a; void* b; b - a;        }", msg + "-.", 1, 28);
		testErr("\nvoid f() { int* a; void* b; a - b;        }", msg + "-.", 1, 28);
		testErr("\nvoid f() { int a[2];        a[0] - &a[0]; }", msg + "-.", 1, 28);
		testErr("\nvoid f() { int a[2][2];     &a[0] - a[0]; }", msg + "-.", 1, 28);
	}

	@Test
	public void errorWhenIllegalOperandsBitwiseOperator() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                  0 & f(); }", msg + "&.", 1, 28);
		testErr("\nvoid f() {                  f() | 0; }", msg + "|.", 1, 28);
		testErr("\nvoid f() { int a[2];        a | 0;   }", msg + "|.", 1, 28);
		testErr("\nvoid f() { int a[2];        0 & a;   }", msg + "&.", 1, 28);
		testErr("\nvoid f() { void * a;        0 ^ a;   }", msg + "^.", 1, 28);
		testErr("\nvoid f() { void * a;        a & 0;   }", msg + "&.", 1, 28);
		testErr("\nvoid f() { int* a; int* b;  b << a;  }", msg + "<<.", 1, 28);
		testErr("\nvoid f() { int* a; int* b;  a >> b;  }", msg + ">>.", 1, 28);
	}

	@Test
	public void errorWhenIllegalOperandsMultiplicativeOperator() throws IOException
	{
		String msg = "Incompatible operands for operator ";
		testErr("\nvoid f() {                  0 * f(); }", msg + "*.", 1, 28);
		testErr("\nvoid f() {                  f() % 0; }", msg + "%.", 1, 28);
		testErr("\nvoid f() { int a[2];        a / 0;   }", msg + "/.", 1, 28);
		testErr("\nvoid f() { int a[2];        0 * a;   }", msg + "*.", 1, 28);
		testErr("\nvoid f() { void * a;        0 % a;   }", msg + "%.", 1, 28);
		testErr("\nvoid f() { void * a;        a / 0;   }", msg + "/.", 1, 28);
		testErr("\nvoid f() { int* a; int* b;  b * a;  }", msg + "*.", 1, 28);
		testErr("\nvoid f() { int* a; int* b;  a % b;  }", msg + "%.", 1, 28);
	}

	@Test
	public void errorWhenIllegalTestExpressionForIf() throws IOException
	{
		testErr("\nvoid f() { if(f()); }", "Scalar expression required.", 1, 14);
	}

	@Test
	public void errorWhenIllegalTestExpressionForWhile() throws IOException
	{
		testErr("\nvoid f() { while(f()); }",
				"While loop control expression must have scalar type.", 1, 17);
	}

	@Test
	public void errorWhenIllegalTestExpressionInFor() throws IOException
	{
		testErr("\nvoid f() { for(;f();); }",
				"For loop control expression must have scalar type.", 1, 16);
	}
//	@Test
//	public void errorForLoopVariableRedeclared() throws IOException
//	{
//		testErr("\nvoid f() { for(int i,i;;); }", "", 1, 16);
//	}

	@Test
	public void errorWhenBreakUsedOutsideLoopOrSwitch() throws IOException
	{
		String msg = "Break used outside loop or switch.";
		testErr("\nvoid f() { break; for(;;) {} }", msg, 1, 11);
		testErr("\nvoid f() { do ; while(1); if(1) { break; } }", msg, 1, 34);
	}

	@Test
	public void errorWhenContinueUsedOutsideLoop() throws IOException
	{
		String msg = "Continue used outside of loop.";
		testErr("\nvoid f() { continue; while(1){} }", msg, 1, 11);
		testErr("\nvoid f() { {for(;;); if (1) ; else { continue; } } }", msg, 1, 37);
	}
}
