package org.bloqqi.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class DFOTests extends TestSuite {
	@Test
	public void testDfo1() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			"  T C;" +
			"}" +
			"diagramtype T(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(2, dt.components().getChild(1).dfo());
		assertEquals(3, dt.components().getChild(2).dfo());
	}
	
	@Test
	public void testDfo2() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			"  connect(A, B);" +
			"}" +
			"diagramtype T(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(2, dt.components().getChild(1).dfo());
	}
	
	@Test
	public void testDfo3() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			"  connect(B, A);" +
			"}" +
			"diagramtype T(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(2, dt.components().getChild(0).dfo());
		assertEquals(1, dt.components().getChild(1).dfo());
	}
	
	@Test
	public void testDfo4() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			"  T C;" +
			"  connect(C, A);" +
			"}" +
			"diagramtype T(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(3, dt.components().getChild(0).dfo());
		assertEquals(1, dt.components().getChild(1).dfo());
		assertEquals(2, dt.components().getChild(2).dfo());
	}
	
	@Test
	public void testDfo5() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			
			"  T C;" +
			"  T2 D;" +
			"  T E;" +
			
			"  T F;" +
			"  T G;" +
			"  connect(A, C);" +
			"  connect(A, D.in1);" +
			"  connect(C, F);" +
			"  connect(C, G);" +
			"  connect(B, D.in2);" +
			"  connect(B, E);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(5, dt.components().getChild(1).dfo());
		assertEquals(2, dt.components().getChild(2).dfo());
		assertEquals(6, dt.components().getChild(3).dfo());
		assertEquals(7, dt.components().getChild(4).dfo());
		assertEquals(3, dt.components().getChild(5).dfo());
		assertEquals(4, dt.components().getChild(6).dfo());
	}

	@Test
	public void testDfo6() {
		String s = 
			"diagramtype Main() { " +
			"  T A;" +
			"  T B;" +
			
			"  T2 C;" +
			"  T D;" +
			"  T E;" +
			"  connect(A, C.in1);" +
			"  connect(A, D);" +
			"  connect(B, C.in2);" +
			"  connect(B, E);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(3, dt.components().getChild(1).dfo());
		assertEquals(4, dt.components().getChild(2).dfo());
		assertEquals(2, dt.components().getChild(3).dfo());
		assertEquals(5, dt.components().getChild(4).dfo());
	}

	

	@Test
	public void testDfo10() {
		String s =
			"diagramtype Main() { " + 
			"  T A;" +
			"  T B;" +
			
			"  T2 C;" +
			"  T D;" + 
			"  connect(A, C.in1);" +
			"  connect(A, D);" +
			"  connect(B, C.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		
		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(3, dt.components().getChild(1).dfo());
		assertEquals(4, dt.components().getChild(2).dfo());
		assertEquals(2, dt.components().getChild(3).dfo());
	}
	
	/** Same test as testDfo10, but C and D have switched declaration order */
	@Test
	public void testDfo11() {
		String s =
			"diagramtype M() { " + 
			"  T A;" +
			"  T B;" +
			
			"  T D;" +
			"  T2 C;" + 
			"  connect(A, C.in1);" +
			"  connect(A, D);" +
			"  connect(B, C.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		
		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(3, dt.components().getChild(1).dfo());
		assertEquals(2, dt.components().getChild(2).dfo());
		assertEquals(4, dt.components().getChild(3).dfo());
	}
	
	
	/** Added for the declarative description of DFO (sinkPred) */
	@Test
	public void testDfo12() {
		String s =
			"diagramtype Main() { " + 
			"  T A;" +
			"  T B;" +
			"  T2 C;" +
			"  T D;" + 
			"  T E;" +
			
			"  connect(A, B);" +
			"  connect(A, C.in1);" +
			"  connect(A, D);" +
			"  connect(D, E);" +
			"  connect(E, C.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		
		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(2, dt.components().getChild(1).dfo());
		assertEquals(5, dt.components().getChild(2).dfo());
		assertEquals(3, dt.components().getChild(3).dfo());
		assertEquals(4, dt.components().getChild(4).dfo());
	}
	@Test
	public void testDfo13() {
		String s =
			"diagramtype Main() { " + 
			"  T A;" +

			"  T B;" +
			"  T C;" +
			
			"  T D;" +	
			"  T2 E;" +
			
			"  T2 F;" +
			"  connect(A, B);" +
			"  connect(A, C);" +
			"  connect(B, D);" +
			"  connect(B, E.in1);" +
			"  connect(C, E.in2);" +
			"  connect(D, F.in1);" +
			"  connect(E, F.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
			
		assertEquals(1, dt.components().getChild(0).dfo());
		assertEquals(2, dt.components().getChild(1).dfo());
		assertEquals(4, dt.components().getChild(2).dfo());
		assertEquals(3, dt.components().getChild(3).dfo());
		assertEquals(5, dt.components().getChild(4).dfo());
		assertEquals(6, dt.components().getChild(5).dfo());
	}
	
	
	
	/** 
	 * Test cases that we sent to Ulf: 
	 **/
	@Test
	public void ulf1a() {
		String s =
			"diagramtype Main() { " + 
			"  T B;" +
			"  T C;" +
			"  T D;" +

			"  T3 E;" +
			"  T F;" +
			"  connect(B, E.in1);" +
			"  connect(C, E.in2);" +
			"  connect(D, E.in3);" +
			"  connect(C, F);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T3(Int in1, Int in2, Int in3 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
			
		assertEquals(1, dt.getComponent(0).dfo());
		assertEquals(2, dt.getComponent(1).dfo());
		assertEquals(4, dt.getComponent(2).dfo());
		assertEquals(5, dt.getComponent(3).dfo());
		assertEquals(3, dt.getComponent(4).dfo());
	}
	@Test
	public void ulf1b() {
		String s =
			"diagramtype Main() { " + 
			"  T A;" +
					
			"  T B;" +
			"  T C;" +
			"  T D;" +

			"  T3 E;" +
			"  T F;" +
			"  connect(A, B);" +
			"  connect(A, C);" +
			"  connect(A, D);" +
			"  connect(B, E.in1);" +
			"  connect(C, E.in2);" +
			"  connect(D, E.in3);" +
			"  connect(C, F);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T3(Int in1, Int in2, Int in3 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
			
		assertEquals(1, dt.getComponent(0).dfo());
		assertEquals(2, dt.getComponent(1).dfo());
		assertEquals(3, dt.getComponent(2).dfo());
		assertEquals(5, dt.getComponent(3).dfo());
		assertEquals(6, dt.getComponent(4).dfo());
		assertEquals(4, dt.getComponent(5).dfo());
	}
	@Test
	public void ulf2a() {
		String s =
			"diagramtype Main() { " + 
			"  T A;" +
					
			"  T B;" +
			"  T C;" +

			"  T D;" +
			"  T E;" +

			"  T F;" +
			"  T2 G;" +
			"  connect(A, B);" +
			"  connect(A, C);" +
			"  connect(B, E);" +
			"  connect(C, D);" +
			"  connect(D, G.in1);" +
			"  connect(E, F);" +
			"  connect(E, G.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		assertEquals(1, dt.getComponent(0).dfo());
		assertEquals(2, dt.getComponent(1).dfo());
		assertEquals(5, dt.getComponent(2).dfo());
		assertEquals(6, dt.getComponent(3).dfo());
		assertEquals(3, dt.getComponent(4).dfo());
		assertEquals(4, dt.getComponent(5).dfo());
		assertEquals(7, dt.getComponent(6).dfo());
	}
	/** Node F is moved compared to 2a) */
	@Test
	public void ulf2b() {
		String s =
			"diagramtype Maun() { " + 
			"  T A;" +
					
			"  T B;" +
			"  T C;" +

			"  T D;" +
			"  T E;" +

			"  T2 G;" +
			"  T F;" +
			"  connect(A, B);" +
			"  connect(A, C);" +
			"  connect(B, E);" +
			"  connect(C, D);" +
			"  connect(D, G.in1);" +
			"  connect(E, F);" +
			"  connect(E, G.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		assertEquals(1, dt.getComponent(0).dfo());
		assertEquals(2, dt.getComponent(1).dfo());
		assertEquals(5, dt.getComponent(2).dfo());
		assertEquals(6, dt.getComponent(3).dfo());
		assertEquals(3, dt.getComponent(4).dfo());
		assertEquals(7, dt.getComponent(5).dfo());
		assertEquals(4, dt.getComponent(6).dfo());
	}
	/** Node A is removed compared to 2b) */
	@Test
	public void ulf2c() {
		String s =
			"diagramtype Main() { " + 
			"  T B;" +
			"  T C;" +

			"  T D;" +
			"  T E;" +

			"  T2 G;" +
			"  T F;" +
			"  connect(B, E);" +
			"  connect(C, D);" +
			"  connect(D, G.in1);" +
			"  connect(E, F);" +
			"  connect(E, G.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		assertEquals(1, dt.getComponent(0).dfo());
		assertEquals(4, dt.getComponent(1).dfo());
		assertEquals(5, dt.getComponent(2).dfo());
		assertEquals(2, dt.getComponent(3).dfo());
		assertEquals(6, dt.getComponent(4).dfo());
		assertEquals(3, dt.getComponent(5).dfo());
	}
	
	@Test
	public void circularDataflow() {
		String s =
			"diagramtype T() { " +
			"  S A;" +
			"  S B;" +
			"  S C;" +
			"  connect(A, B);" +
			"  connect(B, C);" +
			"  connect(C, A);" +
			"}" +
			"diagramtype S(Int in => Int out) {}";
		Program p = parseProgram(s);
		DiagramType dt = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		
		Component a = dt.getComponent(0);
		Component b = dt.getComponent(1);
		Component c = dt.getComponent(2);
		
		assertEquals(-1, a.dfo());
		assertEquals(-1, b.dfo());
		assertEquals(-1, c.dfo());
	}
}
