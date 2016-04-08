package org.bloqqi.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class CycleTests extends TestSuite {
	@Test
	public void oneNodes() {
		String s = 
			"diagramtype T() { " +
			"  S A;" +
			"  connect(A, A);" +
			"}" +
			"diagramtype S(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();
		
		Component a = dt.getComponent(0);
		
		assertEquals(1, a.dfo());
		
		assertTrue(conns.getChild(0).isBroken());
		
		assertSame(a.cycleLowerBound(), null);
		assertSame(a.cycleUpperBound(), null);
	}
	
	@Test
	public void twoNodes() {
		String s = 
			"diagramtype T() { " +
			"  S A;" +
			"  S B;" +
			"  connect(A, B);" +
			"  connect(B, A);" +
			"}" +
			"diagramtype S(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();

		Component a = dt.getComponent(0);
		Component b = dt.getComponent(1);
		
		assertEquals(1, a.dfo());
		assertEquals(2, b.dfo());
		
		assertFalse(conns.getChild(0).isBroken());
		assertTrue(conns.getChild(1).isBroken());
		
		assertSame(a.cycleLowerBound(), null);
		assertSame(a.cycleUpperBound(), b);
		assertSame(b.cycleLowerBound(), a);
		assertSame(b.cycleUpperBound(), null);
	}

	@Test
	public void threeNodes() {
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
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();

		Component a = dt.getComponent(0);
		Component b = dt.getComponent(1);
		Component c = dt.getComponent(2);
		
		assertEquals(1, a.dfo());
		assertEquals(2, b.dfo());
		assertEquals(3, c.dfo());
		
		assertFalse(conns.getChild(0).isBroken());
		assertFalse(conns.getChild(1).isBroken());
		assertTrue(conns.getChild(2).isBroken());
		
		assertSame(a.cycleLowerBound(), null);
		assertSame(a.cycleUpperBound(), b);
		assertSame(b.cycleLowerBound(), a);
		assertSame(b.cycleUpperBound(), null);
		assertSame(c.cycleLowerBound(), a);
		assertSame(c.cycleUpperBound(), null);
	}
	
	@Test
	public void threeNodes2() {
		String s = 
			"diagramtype T() { " +
			"  S B;" +
			"  S A;" +
			"  S C;" + 
			"  connect(A, B);" +
			"  connect(B, C);" +
			"  connect(C, A);" +
			"}" +
			"diagramtype S(Int in => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();

		Component b = dt.getComponent(0);
		Component a = dt.getComponent(1);
		Component c = dt.getComponent(2);
		
		assertEquals(1, b.dfo());
		assertEquals(3, a.dfo());
		assertEquals(2, c.dfo());
		
		assertTrue(conns.getChild(0).isBroken());
		assertFalse(conns.getChild(1).isBroken());
		assertFalse(conns.getChild(2).isBroken());
		
		assertSame(b.cycleLowerBound(), null);
		assertSame(b.cycleUpperBound(), a);
		assertSame(a.cycleLowerBound(), b);
		assertSame(a.cycleUpperBound(), null);
		assertSame(c.cycleLowerBound(), b);
		assertSame(c.cycleUpperBound(), null);
	}
	
	
	@Test
	public void fourNodes() {
		String s = 
			"diagramtype Main() { " +
			"  T2 B;" +
			"  T A;" +
			"  T D;" + 
			"  T C;" + 
			"  connect(B, C);" +
			"  connect(C, A);" +
			"  connect(C, D);" +
			"  connect(A, B.in1);" +
			"  connect(D, B.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();

		Component b = dt.getComponent(0);
		Component a = dt.getComponent(1);
		Component d = dt.getComponent(2);
		Component c = dt.getComponent(3);
		
		assertEquals(1, b.dfo());
		assertEquals(3, a.dfo());
		assertEquals(4, d.dfo());
		assertEquals(2, c.dfo());
		
		assertFalse(conns.getChild(0).isBroken());
		assertFalse(conns.getChild(1).isBroken());
		assertFalse(conns.getChild(2).isBroken());
		assertTrue(conns.getChild(3).isBroken());
		assertTrue(conns.getChild(4).isBroken());
		
		assertSame(b.cycleLowerBound(), null);
		assertSame(b.cycleUpperBound(), a);
		assertSame(a.cycleLowerBound(), b);
		assertSame(a.cycleUpperBound(), null);
		assertSame(d.cycleLowerBound(), b);
		assertSame(d.cycleUpperBound(), null);
		assertSame(c.cycleLowerBound(), b);
		assertSame(c.cycleUpperBound(), null);
	}
	
	@Test
	public void fourNodes2() {
		String s = 
			"diagramtype Main() { " +
			"  T2 B;" +
			"  T A;" +
			"  T D;" + 
			"  T C;" + 
			"  connect(B, C);" +
			"  connect(C, A);" +
			"  connect(A, B.in1);" +
			"  connect(D, B.in2);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);

		List<InheritedConnection> conns = dt.connections();
		
		Component b = dt.getComponent(0);
		Component a = dt.getComponent(1);
		Component d = dt.getComponent(2);
		Component c = dt.getComponent(3);
		
		assertEquals(2, b.dfo());
		assertEquals(4, a.dfo());
		assertEquals(1, d.dfo());
		assertEquals(3, c.dfo());
		
		assertFalse(conns.getChild(0).isBroken());
		assertFalse(conns.getChild(1).isBroken());
		assertTrue(conns.getChild(2).isBroken());
		assertFalse(conns.getChild(3).isBroken());
		
		assertSame(b.cycleLowerBound(), null);
		assertSame(b.cycleUpperBound(), a);
		assertSame(a.cycleLowerBound(), b);
		assertSame(a.cycleUpperBound(), null);
		assertSame(d.cycleLowerBound(), null);
		assertSame(d.cycleUpperBound(), null);
		assertSame(c.cycleLowerBound(), b);
		assertSame(c.cycleUpperBound(), null);
	}
	
	@Test
	public void twoSCCs() {
		String s = 
			"diagramtype Main() { " +
			"  T2 B;" +
			"  T A;" +
			"  T D;" + 
			"  T3 C;" + 
			
			"  T F;" + 
			"  T G;" + 
			"  T E;" + 

			"  connect(B, C.in1);" +
			"  connect(C, A);" +
			"  connect(C, D);" +
			"  connect(A, B.in1);" +
			"  connect(D, B.in2);" +
			
			"  connect(C, E);" +
			"  connect(E, F);" +
			"  connect(E, G);" +
			"  connect(F, C.in2);" +
			"  connect(G, C.in3);" +
			"}" +
			"diagramtype T(Int in => Int out) {}" +
			"diagramtype T2(Int in1, Int in2 => Int out) {}" +
			"diagramtype T3(Int in1, Int in2, Int in3 => Int out) {}";
		DiagramType dt = parseValidDiagramType(s);
		
		List<InheritedConnection> conns = dt.connections();
		
		Component b = dt.getComponent(0);
		Component a = dt.getComponent(1);
		Component d = dt.getComponent(2);
		Component c = dt.getComponent(3);
		
		Component f = dt.getComponent(4);
		Component g = dt.getComponent(5);
		Component e = dt.getComponent(6);

		assertEquals(1, b.dfo());
		assertEquals(3, a.dfo());
		assertEquals(4, d.dfo());
		assertEquals(2, c.dfo());
		assertEquals(6, f.dfo());
		assertEquals(7, g.dfo());
		assertEquals(5, e.dfo());
		
		assertFalse(conns.getChild(0).isBroken());
		assertFalse(conns.getChild(1).isBroken());
		assertFalse(conns.getChild(2).isBroken());
		assertTrue(conns.getChild(3).isBroken());
		assertTrue(conns.getChild(4).isBroken());
		assertFalse(conns.getChild(5).isBroken());
		assertFalse(conns.getChild(6).isBroken());
		assertFalse(conns.getChild(7).isBroken());
		assertTrue(conns.getChild(8).isBroken());
		assertTrue(conns.getChild(9).isBroken());
		
		// "SCC TREE":
		// b
		//   a
		//   d
		//   c
		//     f
		//     g
		//     e
		assertSame(b.cycleLowerBound(), null);
		assertSame(b.cycleUpperBound(), a);
		assertSame(a.cycleLowerBound(), b);
		assertSame(a.cycleUpperBound(), null);
		assertSame(d.cycleLowerBound(), b);
		assertSame(d.cycleUpperBound(), null);
		assertSame(c.cycleLowerBound(), b);
		assertSame(c.cycleUpperBound(), f);
		assertSame(f.cycleLowerBound(), c);
		assertSame(f.cycleUpperBound(), null);
		assertSame(g.cycleLowerBound(), c);
		assertSame(g.cycleUpperBound(), null);
		assertSame(e.cycleLowerBound(), c);
		assertSame(e.cycleUpperBound(), null);
	}
}
