package org.bloqqi.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class FunctionTestsCode extends TestSuite{
	@Test
	public void testFunction1() {
		Function f1 = new Function();
		f1.setID("funcName");
		f1.addInParameter(new InParameter(new TypeUse("Int"), "in1"));
		f1.addOutParameter(new OutParameter(new TypeUse("Int"), "out"));
		Block b = new Block();
		f1.setBlock(b);
		assertEquals(1, f1.getNumInParameter());
		StringBuilder sb = new StringBuilder();
		f1.prettyPrint(sb);
		String s = 
				"function funcName(in1: Int => out: Int) {\n" +
				"}\n";
		assertEquals(s, sb.toString());
	}
	
	@Test
	public void testFunction2() {
		String s = "function Add2(in1: Int => out: Int) {\n}";
		parseAndCheck(s);
	}
	@Test
	public void testFunction3() {
		String s = "function Add2(in1: Int, in2: Int => out: Int, out2: Int) {\n}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunction4() {
		Function f1 = new Function();
		f1.setID("funcName");
		f1.addInParameter(new InParameter(new TypeUse("Int"), "in1"));
		f1.addOutParameter(new OutParameter(new TypeUse("Int"), "out"));
		Block b =  new Block();
		b.addStmt(new AssignStmt(new IdFExpr("out"), new IdFExpr("in")));
		f1.setBlock(b);
		assertEquals(1, f1.getNumInParameter());
		StringBuilder sb = new StringBuilder();
		f1.prettyPrint(sb);
		String s = 
				"function funcName(in1: Int => out: Int) {\n" +
				"\tout = in;\n" +
				"}\n";
		assertEquals(s, sb.toString());
	}
	@Test
	public void testFunction5() {
		String s = "function Add2(in1: Int, in2: Int => out: Int, out2: Int) {\n" +
	               "\tout = -in1;\n" +
	               "\tout2 = in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunction6() {
		String s = "function Add2(in1: Real => out: Real) {\n}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionAdd1() {
		String s = "function Add2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 + in2;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionAdd2() {
		String s = "function Add2(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "\tv1: Int;\n" +   
				   "\tv1 = in1 + in2;\n" +
	               "\tout = v1 + in3;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionAdd3() {
		String s = "function Add2(in1: Real, in2: Real => out: Real) {\n" +
	               "\tout = in1 + in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionSub1() {
		String s = "function Sub2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 - in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionSub2() {
		String s = "function Sub2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 - in2 - 1 + 3 - 2;\n" +
				   "}";
//		Program p = parseProgram(s);
//	    p.printAST();
		parseAndCheck(s);
	}
	@Test
	public void testFunctionMul1() {
		String s = "function Mul2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 * in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionMul2() {
		String s = "function Mul2(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "\tout = in1 * in2 * in3;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionDiv1() {
		String s = "function Div2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 / in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionBinExpr1() {
		String s = "function fexpr(in1: Int, in2: Int, in3: Int, in4: Int => out: Int) {\n" +
	               "\tout = in1 / in2 + in3 * in4;\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionBinComparisons() {
		String s = "function ge(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tout = in1 > 3;\n" +
	               "\tout = in1 < in2;\n" +
	               "\tout = in1 <= 2;\n" +
	               "\tout = in1 != in2;\n" +
	               "\tout = !out;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionWhileStmt() {
		String s = "function whileStmt(in1: Int => out: Int) {\n" +
		           "\tout = 0;\n" +
	               "\twhile (out < in1) {\n" +
	               "\t\tout = out + 1;\n" +
	               "\t}\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionIfStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tif (in1 < in2) {" + "\n" +
	               "\t\tout = in1 < in2;\n" +
				   "\t}\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionIfElseStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tif (in1 < in2) {" + "\n" +
	               "\t\tout = in1 < in2;\n" +
				   "\t}\n" +
	               "\telse {\n" +
	               "\t\tout = in2 != in1;\n" +
				   "\t}\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionIfElseIfStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tif (in1 < in2) {" + "\n" +
	               "\t\tout = in1 < in2;\n" +
				   "\t}\n" +
	               "\telse {\n" +
	               "\t\tif (in1 == in2) {\n" +
	               "\t\t\tout = in2 != in1;\n" +
				   "\t\t}\n" +
				   "\t}\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionAndOrStatements() {
		String s = "function ifFunc(in1: Bool, in2: Bool, in3: Bool => out: Bool) {\n" +
	               "\tif (in1 && in2 || in3) {" + "\n" +
	               "\t\tout = in1 || in2;\n" +
				   "\t}\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionExprPar() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 + 2 + in2;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionNoOut() {
		String s = "function ifFunc(in1: Int, in2: Int) {\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionNoIn() {
		String s = "function getFunc( => out1: Int, out2: Int) {\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionReturn() {
		String s = "function getFunc( => out1: Int, out2: Int) {\n" +
				   "\tif (true) {\n" +
					"\t\treturn;\n" +
				   "\t}\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionNameAnalysis() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "\ti: Int;\n" +
				   "\ti = 4;\n" +
				   "\tx = 4;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		DeclStmt d = (DeclStmt)f.getBlock().getStmt(0);
		AssignStmt a = (AssignStmt)f.getBlock().getStmt(1);
		assertSame(d, a.getLeft().decl());
		assertEquals("Line 4, column 2: 1001: The declaration of x was not found", cu.errors().iterator().next().toString());
	}

	@Test
	public void testFunctionNameAnalysis1() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
				   "\ti = 4;\n" +
	               "\ti: Int;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		Function f = (Function) p.getCompilationUnit(0).typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlock().getStmt(0);
		assertNull(a.getLeft().decl());
	}

	@Test
	public void testFunctionNameAnalysis2() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "\ti: Int;\n" +
	               "\tif (true) {\n" +
				   "\t\ti = 4;\n" +
				   "\t\tout = 4;\n" +
				   "\t\tin2 = 5;\n" +
				   "\t}\n" + 
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		DeclStmt d = (DeclStmt)f.getBlock().getStmt(0);
		IfStmt i = (IfStmt)f.getBlock().getStmt(1);
		AssignStmt a = (AssignStmt)i.getBlock().getStmt(0);
		assertSame(d, a.getLeft().decl());
		AssignStmt a2 = (AssignStmt)i.getBlock().getStmt(1);
		assertSame(f.getOutParameter(0), a2.getLeft().decl());
		AssignStmt a3 = (AssignStmt)i.getBlock().getStmt(2);
		assertSame(f.getInParameter(1), a3.getLeft().decl());
	}

	@Test
	public void testFunctionTypeAnalysis1() {
		String s = "function iffunc(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = in1 + 2;\n" +
				   "}";
		Program p = parseAndCheck(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlock().getStmt(0);
		FExpr expr = a.getRight(); 
		assertSame(p.intType(), expr.fType());	
	}

	@Test
	public void testFunctionTypeAnalysis2() {
		String s = "function iffunc(in1: Bool, in2: Int => out: Bool) {\n" +
	               "\tout = in1 || false;\n" +
				   "}";
		Program p = parseAndCheck(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlock().getStmt(0);
		FExpr expr = a.getRight(); 
		assertSame(p.boolType(), expr.fType());		
	}
	
	@Test
	public void testFunctionTypeAnalysis3() {
		String s = "function EqNewfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
	               "\tout = in1 == in2;\n" +
	               "\tout = in1 != in2;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis1() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "\tout = (in1 + in2) * in3;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis2() {
		String s = "function parfunc(in1: Bool, in2: Bool, in3: Bool, in4: Bool => out: Bool) {\n" +
	               "\tout = (in1 || in2) && in3 || in4;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis3() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "\tout = in1 - (in2 - in3);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionParanthesis4() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int, in4: Int => out: Int) {\n" +
	               "\tout = (in1 + in2) * (in3 - in4);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionGenerateC1() {
		String s = "function parfunc(in1: Bool, in2: Bool, in3: Bool, in4: Bool => out: Bool) {\n" +
	               "\tout = (in1 || in2) && in3 || in4;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tbool out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(bool in1, bool in2, bool in3, bool in4){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\t_p.out = (in1 || in2) && in3 || in4;\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}

	@Test
	public void testFunctionGenerateC2() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
				   "\tif (in1 > in2) {\n" +
				   "\t\treturn;\n" +
				   "\t}\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tbool out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(bool in1, bool in2){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\tif (in1 > in2) {\n" +
                          "\t\treturn _p;\n" +
						  "\t}\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC3() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
	               "\tout = !(!(in1 || in2));\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tbool out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(bool in1, bool in2){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\t_p.out = !(!(in1 || in2));\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC4() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
				   "\twhile (in1 > in2) {\n" +
				   "\t\tin2 = in2 + 1;\n" +
				   "\t}\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tbool out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(bool in1, bool in2){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\twhile (in1 > in2) {\n" +
                          "\t\tin2 = in2 + 1;\n" +
						  "\t}\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}

	@Test
	public void testFunctionGenerateC5() {
		String s = "function parfunc(in1: Int => out: Int) {\n" +
				   "\tvar1: Int = 3;\n" +
	               "\tout = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tint out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(int in1){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\tint var1 = 3;\n" +
						  "\t_p.out = in1 + var1;\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC6() {
		String s = "function parfunc(in1: Int => out: Int) {\n" +
				   "\tvar1: Int = in1 * in1 + 3;\n" +
	               "\tout = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tint out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(int in1){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\tint var1 = in1 * in1 + 3;\n" +
						  "\t_p.out = in1 + var1;\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	@Test
	public void testFunctionGenerateC7() {
		String s = "function parfunc(in1: Real => out: Real) {\n" +
				   "\tvar1: Real = in1 * in1 + 3.2;\n" +
	               "\tout = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.generateC(sb);
		String expected = "typedef struct {\n" +
						  "\tfloat out;\n" +
				          "} parfunc_RES;\n" +
						  "parfunc_RES parfunc(float in1){\n" +
						  "\tparfunc_RES _p;\n" +
						  "\tfloat var1 = in1 * in1 + 3.2;\n" +
						  "\t_p.out = in1 + var1;\n" +
                          "\treturn _p;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	@Test
	public void testFunctionRealValues() {
		String s = "function Add2(in1: Real, in2: Real => out: Real) {\n" +
	               "\tout = -(in1 + 25.12 - 2.123e-2);\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionUnaryMinus() {
		String s = "function Add2(in1: Int, in2: Int => out: Int) {\n" +
	               "\tout = -(in1 + in2);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionUnaryNot() {
		String s = "function Add2(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tout = !(in1 >= in2);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionUnaryNot2() {
		String s = "function Add2(in1: Int, in2: Int => out: Bool) {\n" +
	               "\tout = !(!(in1 >= in2));\n" +
				   "}";
		parseAndCheck(s);
	}

	private Program parsePrettyPrint(String s) {
		Program p = parseProgram(s);
	    CompilationUnit cu = p.getCompilationUnit(0);
	    assertEquals(s, cu.prettyPrint().trim());
	    return p;
	}

	private Program parseAndCheck(String s) {
	    Program p = parsePrettyPrint(s);
	    assertEquals("[]", p.getCompilationUnit(0).errors().toString());
		return p;
	}
	
}
