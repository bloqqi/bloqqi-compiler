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
		BlockStmt b = new BlockStmt();
		f1.setBlockStmt(b);
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
		BlockStmt b =  new BlockStmt();
		b.addStmt(new AssignStmt(new IdFExpr("out"), new IdFExpr("in")));
		f1.setBlockStmt(b);
		assertEquals(1, f1.getNumInParameter());
		StringBuilder sb = new StringBuilder();
		f1.prettyPrint(sb);
		String s = 
				"function funcName(in1: Int => out: Int) {\n" +
				"  out = in;\n" +
				"}\n";
		assertEquals(s, sb.toString());
	}
	@Test
	public void testFunction5() {
		String s = "function Add2(in1: Int, in2: Int => out: Int, out2: Int) {\n" +
	               "  out = -in1;\n" +
	               "  out2 = in2;\n" +
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
	               "  out = in1 + in2;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionAdd2() {
		String s = "function Add2(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "  v1: Int;\n" +
				   "  v1 = in1 + in2;\n" +
	               "  out = v1 + in3;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionAdd3() {
		String s = "function Add2(in1: Real, in2: Real => out: Real) {\n" +
	               "  out = in1 + in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionSub1() {
		String s = "function Sub2(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 - in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionSub2() {
		String s = "function Sub2(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 - in2 - 1 + 3 - 2;\n" +
				   "}";
//		Program p = parseProgram(s);
//	    p.printAST();
		parseAndCheck(s);
	}
	@Test
	public void testFunctionMul1() {
		String s = "function Mul2(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 * in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionMul2() {
		String s = "function Mul2(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "  out = in1 * in2 * in3;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionDiv1() {
		String s = "function Div2(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 / in2;\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionBinExpr1() {
		String s = "function fexpr(in1: Int, in2: Int, in3: Int, in4: Int => out: Int) {\n" +
	               "  out = in1 / in2 + in3 * in4;\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionBinComparisons() {
		String s = "function ge(in1: Int, in2: Int => out: Bool) {\n" +
	               "  out = in1 > 3;\n" +
	               "  out = in1 < in2;\n" +
	               "  out = in1 <= 2;\n" +
	               "  out = in1 != in2;\n" +
	               "  out = !out;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionWhileStmt() {
		String s = "function whileStmt(in1: Int => out: Int) {\n" +
		           "  out = 0;\n" +
	               "  while (out < in1) {\n" +
	               "    out = out + 1;\n" +
	               "  }\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionIfStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "  if (in1 < in2) {" + "\n" +
	               "    out = in1 < in2;\n" +
				   "  }\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionIfElseStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "  if (in1 < in2) {" + "\n" +
	               "    out = in1 < in2;\n" +
				   "  }\n" +
	               "  else {\n" +
	               "    out = in2 != in1;\n" +
				   "  }\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionIfElseIfStatements() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Bool) {\n" +
	               "  if (in1 < in2) {" + "\n" +
	               "    out = in1 < in2;\n" +
				   "  }\n" +
	               "  else {\n" +
	               "    if (in1 == in2) {\n" +
	               "      out = in2 != in1;\n" +
				   "    }\n" +
				   "  }\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionAndOrStatements() {
		String s = "function ifFunc(in1: Bool, in2: Bool, in3: Bool => out: Bool) {\n" +
	               "  if (in1 && in2 || in3) {" + "\n" +
	               "    out = in1 || in2;\n" +
				   "  }\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionExprPar() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 + 2 + in2;\n" +
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
				   "  if (true) {\n" +
					"    return;\n" +
				   "  }\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionNameAnalysis() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "  i: Int;\n" +
				   "  i = 4;\n" +
				   "  x = 4;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		DeclStmt d = (DeclStmt)f.getBlockStmt().getStmt(0);
		AssignStmt a = (AssignStmt)f.getBlockStmt().getStmt(1);
		assertSame(d, a.getLeft().decl());
		assertEquals("Line 4, column 3: The declaration of x was not found", cu.errors().iterator().next().toString());
	}

	@Test
	public void testFunctionNameAnalysis1() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
				   "  i = 4;\n" +
	               "  i: Int;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		Function f = (Function) p.getCompilationUnit(0).typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlockStmt().getStmt(0);
		assertNull(a.getLeft().decl());
	}

	@Test
	public void testFunctionNameAnalysis2() {
		String s = "function ifFunc(in1: Int, in2: Int => out: Int) {\n" +
	               "  i: Int;\n" +
	               "  if (true) {\n" +
				   "    i = 4;\n" +
				   "    out = 4;\n" +
				   "    in2 = 5;\n" +
				   "  }\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		DeclStmt d = (DeclStmt)f.getBlockStmt().getStmt(0);
		IfStmt i = (IfStmt)f.getBlockStmt().getStmt(1);
		AssignStmt a = (AssignStmt)i.getBlockStmt().getStmt(0);
		assertSame(d, a.getLeft().decl());
		AssignStmt a2 = (AssignStmt)i.getBlockStmt().getStmt(1);
		assertSame(f.getOutParameter(0), a2.getLeft().decl());
		AssignStmt a3 = (AssignStmt)i.getBlockStmt().getStmt(2);
		assertSame(f.getInParameter(1), a3.getLeft().decl());
	}

	@Test
	public void testFunctionTypeAnalysis1() {
		String s = "function iffunc(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = in1 + 2;\n" +
				   "}";
		Program p = parseAndCheck(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlockStmt().getStmt(0);
		FExpr expr = a.getRight(); 
		assertSame(p.intType(), expr.fType());	
	}

	@Test
	public void testFunctionTypeAnalysis2() {
		String s = "function iffunc(in1: Bool, in2: Int => out: Bool) {\n" +
	               "  out = in1 || false;\n" +
				   "}";
		Program p = parseAndCheck(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		AssignStmt a = (AssignStmt)f.getBlockStmt().getStmt(0);
		FExpr expr = a.getRight(); 
		assertSame(p.boolType(), expr.fType());		
	}
	
	@Test
	public void testFunctionTypeAnalysis3() {
		String s = "function EqNewfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
	               "  out = in1 == in2;\n" +
	               "  out = in1 != in2;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis1() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "  out = (in1 + in2) * in3;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis2() {
		String s = "function parfunc(in1: Bool, in2: Bool, in3: Bool, in4: Bool => out: Bool) {\n" +
	               "  out = (in1 || in2) && in3 || in4;\n" +
				   "}";
		parseAndCheck(s);
	}

	@Test
	public void testFunctionParanthesis3() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int => out: Int) {\n" +
	               "  out = in1 - (in2 - in3);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionParanthesis4() {
		String s = "function parfunc(in1: Int, in2: Int, in3: Int, in4: Int => out: Int) {\n" +
	               "  out = (in1 + in2) * (in3 - in4);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionGenerateC1() {
		String s = "function parfunc(in1: Bool, in2: Bool, in3: Bool, in4: Bool => out: Bool) {\n" +
	               "  out = (in1 || in2) && in3 || in4;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Bool in1, Bool in2, Bool in3, Bool in4) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  _result.out = (in1 || in2) && in3 || in4;\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}

	@Test
	public void testFunctionGenerateC2() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
				   "  if (in1 > in2) {\n" +
				   "    return;\n" +
				   "  }\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Bool in1, Bool in2) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  if (in1 > in2) {\n" +
                          "    return _result;\n" +
						  "  }\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC3() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
	               "  out = !(!(in1 || in2));\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Bool in1, Bool in2) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  _result.out = !(!(in1 || in2));\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC4() {
		String s = "function parfunc(in1: Bool, in2: Bool => out: Bool) {\n" +
				   "  while (in1 > in2) {\n" +
				   "    in2 = in2 + 1;\n" +
				   "  }\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Bool in1, Bool in2) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  while (in1 > in2) {\n" +
                          "    in2 = in2 + 1;\n" +
						  "  }\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}

	@Test
	public void testFunctionGenerateC5() {
		String s = "function parfunc(in1: Int => out: Int) {\n" +
				   "  var1: Int = 3;\n" +
	               "  out = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Int in1) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  Int var1 = 3;\n" +
						  "  _result.out = in1 + var1;\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	
	@Test
	public void testFunctionGenerateC6() {
		String s = "function parfunc(in1: Int => out: Int) {\n" +
				   "  var1: Int = in1 * in1 + 3;\n" +
	               "  out = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Int in1) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  Int var1 = in1 * in1 + 3;\n" +
						  "  _result.out = in1 + var1;\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	@Test
	public void testFunctionGenerateC7() {
		String s = "function parfunc(in1: Real => out: Real) {\n" +
				   "  var1: Real = in1 * in1 + 3.2;\n" +
	               "  out = in1 + var1;\n" +
				   "}";
		Program p = parsePrettyPrint(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		Function f = (Function) cu.typeDecls().get(0);
		StringBuilder sb = new StringBuilder();
		f.genFunctionDefinitionsC(sb);
		String expected = "parfunc_RES parfunc(Real in1) {\n" +
						  "  parfunc_RES _result = {0};\n" +
						  "  Real var1 = in1 * in1 + 3.2;\n" +
						  "  _result.out = in1 + var1;\n" +
                          "  return _result;\n" +
						  "}\n";
		assertEquals(expected, sb.toString());
	}
	@Test
	public void testFunctionRealValues() {
		String s = "function Add2(in1: Real, in2: Real => out: Real) {\n" +
	               "  out = -(in1 + 25.12 - 2.123e-2);\n" +
				   "}";
		parseAndCheck(s);
	}
	@Test
	public void testFunctionUnaryMinus() {
		String s = "function Add2(in1: Int, in2: Int => out: Int) {\n" +
	               "  out = -(in1 + in2);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionUnaryNot() {
		String s = "function Add2(in1: Int, in2: Int => out: Bool) {\n" +
	               "  out = !(in1 >= in2);\n" +
				   "}";
		parseAndCheck(s);
	}
	
	@Test
	public void testFunctionUnaryNot2() {
		String s = "function Add2(in1: Int, in2: Int => out: Bool) {\n" +
	               "  out = !(!(in1 >= in2));\n" +
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
