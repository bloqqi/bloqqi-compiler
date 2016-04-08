package org.bloqqi.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class EditingTests extends TestSuite {
	@Test
	public void changeInParameters() {
		Program p = createParameterTest();
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) p.getCompilationUnit(0).typeDecls().get(1);
		
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());
		
		InParameter in2 = new InParameter(new TypeUse("Int"), "in2");
		dtB.addLocalInParameter(in2);
		p.flushAllAttributes();
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());

		dtB.getLocalInParameters().removeChild(in2);
		p.flushAllAttributes();
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());
	}
	
	@Test
	public void changeOutParameters() {
		Program p = createParameterTest();
		
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) p.getCompilationUnit(0).typeDecls().get(1);
		
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());

		OutParameter out2 = new OutParameter(new TypeUse("Int"), "out2");
		dtB.addLocalOutParameter(out2);
		p.flushAllAttributes();
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());
		
		dtB.getLocalOutParameters().removeChild(out2);
		p.flushAllAttributes();
		assertEquals("connect(in, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in)", dtA.getFlowDecl(1).prettyPrint());
	}
	
	@Test
	public void changeParameterName() {
		Program p = createParameterTest();
		
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) p.getCompilationUnit(0).typeDecls().get(1);
		
		assertFalse(dtA.localParameterLookup("in").changeName("in2"));
		assertFalse(dtB.localParameterLookup("in").changeName("out"));
		
		assertTrue(dtA.localParameterLookup("in").changeName("in1"));
		assertEquals("connect(in1, b1.in)", dtA.getFlowDecl(0).prettyPrint());
		
		dtB.addLocalInParameter(new InParameter(new TypeUse("Int"), "in2"));
		p.flushAllAttributes();
		assertTrue(dtB.localParameterLookup("in").changeName("in1"));
		
		assertEquals("connect(in1, out)", dtB.getFlowDecl(0).prettyPrint());
		assertEquals("connect(in1, b1.in1)", dtA.getFlowDecl(0).prettyPrint());
		assertEquals("connect(b1.out, B_2.in1)", dtA.getFlowDecl(1).prettyPrint());
	}
	
	@Test
	public void changeDiagramTypeName() {
		Program p = createParameterTest();
		
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) p.getCompilationUnit(0).typeDecls().get(1);
		
		assertFalse(dtA.changeName("B"));
		assertFalse(dtA.changeName("Int"));
		assertFalse(dtA.changeName("F"));
		assertFalse(dtA.changeName("S"));
		assertFalse(dtB.changeName("A"));
		
		assertTrue(dtB.changeName("B2"));
		assertEquals("b1: B2", dtA.getLocalComponent(0).prettyPrint());
		assertEquals("B2_2", dtA.getLocalComponent(1).prettyPrint());
		assertEquals("B2_2.in", ((Connection) dtA.getFlowDecl(1)).getTarget().prettyPrint());
	}
	
	@Test
	public void changeDiagramTypeName2() {
		String s =
			"diagramtype Main {" +
			"	a: A;" +
			"}" +
			"diagramtype SubMain extends Main {" +
			"	redeclare a: A { };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B extends A {" +
			"}";
		Program p = parseValidProgram(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		DiagramType dtA = (DiagramType) cu.typeDecls().get(2);
		assertTrue(dtA.changeName("NewA"));

		// Check that the supertype of the anonymous type is NewA
		DiagramType dtSubMain = (DiagramType) cu.typeDecls().get(1);
		RedeclareComponent rc = (RedeclareComponent) dtSubMain.getLocalComponent(0);
		assertSame("NewA", rc.anonymousDiagramType().directSuperTypes().iterator().next().getID());
		
		String expected =
			"diagramtype Main {\n" +
			"  a: NewA;\n" +
			"}\n" +
			"diagramtype SubMain extends Main {\n" +
			"  redeclare a: NewA {\n" +
			"  };\n" +
			"}\n" +
			"diagramtype NewA {\n" +
			"}\n" +
			"diagramtype B extends NewA {\n" +
			"}\n";
		assertEquals(expected, cu.prettyPrint());
	}
	
	@Test
	public void changeComponentName() {
		Program p = createParameterTest();
		
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		
		assertFalse(dtA.getLocalComponent(1).changeName("b1"));
		assertFalse(dtA.getLocalComponent(1).changeName("in"));
		assertFalse(dtA.getLocalComponent(1).changeName("in2"));
		
		assertTrue(dtA.getLocalComponent(1).changeName("b2"));
		assertEquals("b2: B", dtA.getLocalComponent(1).prettyPrint());
		assertEquals("connect(b1.out, b2.in)", dtA.getFlowDecl(1).prettyPrint());
	}
	
	@Test
	public void changeNamesUpdateRecommendation() {
		String s =
			"diagramtype A(Int in) {" +
			"	B b;" +
			"	connect(in, b.in1);" +
			"}" +
			"diagramtype B(Int in1, Int in2 => Int out) {" +
			"}" +
			"diagramtype C(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"wiring C[=> Int v] {" +
			"	intercept v with C, C;" +
			"}" +
			"recommendation A {" +
			"	C[b.in1] c;" +
			"}";
		Program p = parseValidProgram(s);
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) p.getCompilationUnit(0).typeDecls().get(1);
		DiagramType dtC = (DiagramType) p.getCompilationUnit(0).typeDecls().get(2);

		assertTrue(dtA.getLocalComponent(0).changeName("newB"));
		assertTrue(dtA.changeName("NewA"));
		assertTrue(dtB.getLocalInParameter(0).changeName("newIn1"));
		assertTrue(dtC.changeName("NewC"));

		Program p2 = parseValidProgram(p.getCompilationUnit(0).prettyPrint());
		String expected =
			"recommendation NewA {\n" +
			"  c: NewC[newB.newIn1];\n" +
			"}\n";
		assertEquals(expected, p2.getCompilationUnit(0).getDeclaration(3).prettyPrint());
	}
	
	@Test
	public void changeNamesUpdateWiring() {
		String s =
			"diagramtype A(in: Int, in2: Int => out: Int, out2: Int) {" +
			"}" +
			"wiring A[=> Int v1, => Int v2] {" +
			"	intercept v1 with A.in, A.out;" +
			"	intercept v1 with A.in2, A.out2;" +
			"}";
		Program p = parseValidProgram(s);
		DiagramType dtA = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);

		assertTrue(dtA.getLocalInParameter(0).changeName("newIn"));
		assertTrue(dtA.getLocalInParameter(1).changeName("newIn2"));
		assertTrue(dtA.getLocalOutParameter(0).changeName("newOut"));
		assertTrue(dtA.getLocalOutParameter(1).changeName("newOut2"));

		Program p2 = parseValidProgram(p.getCompilationUnit(0).prettyPrint());
		String expected =
			"diagramtype A(newIn: Int, newIn2: Int => newOut: Int, newOut2: Int) {\n" +
			"}\n" +
			"wiring A[=>v1: Int, =>v2: Int] {\n" +
			"  intercept v1 with A.newIn, A.newOut;\n" +
			"  intercept v1 with A.newIn2, A.newOut2;\n" +
			"}\n";
		assertEquals(expected, p2.getCompilationUnit(0).prettyPrint());
	}
	

	private Program createParameterTest() {
		String s = 
			"diagramtype A(Int in, Int in2) {" +
			"	B b1;" +
			"	B_2;" +
			"	connect(in, b1);" +
			"	connect(b1, B_2);" + 
			"}" +
			"diagramtype B(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"external F();" +
			"struct S {" +
			"	Int f;" +
			"}";
		return parseValidProgram(s);
	}
}
