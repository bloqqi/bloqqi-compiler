package org.bloqqi.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;
import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class InheritanceTests extends TestSuite {
	@Test
	public void singleInheritance() {
		String s =
			"diagramtype B {\n}\n" +
			"diagramtype D extends B {\n}";
		CompilationUnit cu = parseAndPrettyPrint(s);
		DiagramType B = (DiagramType) cu.typeDecls().get(0);
		DiagramType D = (DiagramType) cu.typeDecls().get(1);
		assertEquals(B, D.getSuperType(0).type());
	}

	@Test
	public void multipleInheritance() {
		String s = 
			"diagramtype B1 {\n}\n" + 
			"diagramtype D extends B1, B2 {\n}\n" +
			"diagramtype B2 {\n}";
		CompilationUnit cu = parseAndPrettyPrint(s);
		DiagramType B1 = (DiagramType) cu.typeDecls().get(0);
		DiagramType D = (DiagramType) cu.typeDecls().get(1);
		DiagramType B2 = (DiagramType) cu.typeDecls().get(2);
		assertEquals(B1, D.getSuperType(0).type());
		assertEquals(B2, D.getSuperType(1).type());
	}

	@Test
	public void diamondProblem() {
		String s = 
			"diagramtype B1 {\n}\n" +
			"diagramtype B2 extends B1 {\n}\n" +
			"diagramtype B3 extends B1 {\n}\n" +
			"diagramtype D extends B2, B3 {\n}";
		CompilationUnit cu = parseAndPrettyPrint(s);
		DiagramType B1 = (DiagramType) cu.typeDecls().get(0);
		DiagramType B2 = (DiagramType) cu.typeDecls().get(1);
		DiagramType B3 = (DiagramType) cu.typeDecls().get(2);
		DiagramType D = (DiagramType) cu.typeDecls().get(3);
		assertEquals(B1, B2.getSuperType(0).type());
		assertEquals(B1, B3.getSuperType(0).type());
		assertEquals(B2, D.getSuperType(0).type());
		assertEquals(B3, D.getSuperType(1).type());
	}


	@Test
	public void diamondProblemLinearization() {
		String s;
		s  = "diagramtype B1 {\n}\n";
		s += "diagramtype B2 extends B1 {\n}\n";
		s += "diagramtype B3 extends B1 {\n}\n";
		s += "diagramtype D extends B2, B3 {\n}";
		CompilationUnit cu = parseAndPrettyPrint(s);
		DiagramType B1 = (DiagramType) cu.typeDecls().get(0);
		DiagramType B2 = (DiagramType) cu.typeDecls().get(1);
		DiagramType B3 = (DiagramType) cu.typeDecls().get(2);
		DiagramType D = (DiagramType) cu.typeDecls().get(3);

		compareOrderedColl(B1.superTypesLinearized(), B1);
		compareOrderedColl(B2.superTypesLinearized(), B1, B2);
		compareOrderedColl(B3.superTypesLinearized(), B1, B3);
		compareOrderedColl(D.superTypesLinearized(), B1, B2, B3, D);
	}

	@Test
	public void complicatedDiamondProblemLinearization() {
		String s;
		s  = "diagramtype B1 {\n}\n";
		s += "diagramtype B2 extends B1 {\n}\n";
		s += "diagramtype B4 extends B2 {\n}\n";
		s += "diagramtype B3 extends B1 {\n}\n";
		s += "diagramtype B5 extends B3 {\n}\n";
		s += "diagramtype D extends B4, B5, B2 {\n}";
		CompilationUnit cu = parseAndPrettyPrint(s);
		DiagramType B1 = (DiagramType) cu.typeDecls().get(0);
		DiagramType B2 = (DiagramType) cu.typeDecls().get(1);
		DiagramType B4 = (DiagramType) cu.typeDecls().get(2);
		DiagramType B3 = (DiagramType) cu.typeDecls().get(3);
		DiagramType B5 = (DiagramType) cu.typeDecls().get(4);
		DiagramType D = (DiagramType) cu.typeDecls().get(5);

		compareOrderedColl(B1.superTypesLinearized(), B1);
		compareOrderedColl(B2.superTypesLinearized(), B1, B2);
		compareOrderedColl(B4.superTypesLinearized(), B1, B2, B4);
		compareOrderedColl(B3.superTypesLinearized(), B1, B3);
		compareOrderedColl(B5.superTypesLinearized(), B1, B3, B5);
		compareOrderedColl(D.superTypesLinearized(), B1, B2, B4, B3, B5, D);
	}

	@Test
	public void interception() {
		String s;
		s  = "diagramtype T(in: Int => out: Int) {\n}\n";
		s += "diagramtype B {\n";
		s += "  a: T;\n";
		s += "  b: T;\n";
		s += "  connect(a.out, b.in);\n";
		s += "}\n";
		s += "diagramtype D extends B {\n";
		s += "  c: T;\n";
		s += "  intercept b.in with c.in, c.out;\n";
		s += "}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		List<InheritedConnection> connections = ((DiagramType) cu.typeDecls().get(2)).connections();
		assertEquals(2, connections.getNumChild());
		assertEquals("connect(a.out, c.in)", connections.getChild(0).prettyPrint());
		assertEquals("connect(c.out, b.in)", connections.getChild(1).prettyPrint());
	}
	
	@Test
	public void sameName() {
		String s;
		s  = "diagramtype T(in: Int => out: Int) {\n}\n";
		s += "diagramtype B {\n";
		s += "  a: T;\n";
		s += "  b: T;\n";
		s += "  connect(a.out, b.in);\n";
		s += "}\n";
		s += "diagramtype D extends B {\n";
		s += "  a: T;\n";
		s += "  c: T;\n";
		s += "  connect(a.out, c.in);\n";
		s += "}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		List<InheritedConnection> connections = ((DiagramType) cu.typeDecls().get(2)).connections();
		
		assertEquals(2, connections.getNumChild());
		assertEquals("connect(a.out, b.in)", connections.getChild(0).prettyPrint());
		assertEquals("connect(a.out, c.in)", connections.getChild(1).prettyPrint());
	}
	
	@Test
	public void sameNameInterception() {
		String s;
		s  = "diagramtype T(in: Int => out: Int) {\n}\n";
		s += "diagramtype B {\n";
		s += "  a: T;\n";
		s += "  b: T;\n";
		s += "  connect(a.out, b.in);\n";
		s += "}\n";
		s += "diagramtype D extends B {\n";
		s += "  a: T;\n";
		s += "  c: T;\n";
		s += "  d: T;\n";
		s += "  connect(a.out, c.in);\n";
		s += "  intercept b.in with d.in, d.out;\n";
		s += "}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		List<InheritedConnection> connections = ((DiagramType) cu.typeDecls().get(2)).connections();
		
		assertEquals(3, connections.getNumChild());
		assertEquals("connect(a.out, c.in)", connections.getChild(0).prettyPrint());
		assertEquals("connect(a.out, d.in)", connections.getChild(1).prettyPrint());
		assertEquals("connect(d.out, b.in)", connections.getChild(2).prettyPrint());
	}
	
	
	@Test
	public void interceptionDiamondProblem() {
		String s = createInterceptionDiamond();

		CompilationUnit cu = parseAndPrettyPrint(s);

		List<InheritedConnection> connectionsB = ((DiagramType) cu.typeDecls().get(1)).connections();
		assertEquals(1, connectionsB.getNumChild());
		assertEquals("connect(a.out, b.in)", connectionsB.getChild(0).prettyPrint());
		
		List<InheritedConnection> connectionsB1 = ((DiagramType) cu.typeDecls().get(2)).connections();
		assertEquals(2, connectionsB1.getNumChild());
		assertEquals("connect(a.out, c.in)", connectionsB1.getChild(0).prettyPrint());
		assertEquals("connect(c.out, b.in)", connectionsB1.getChild(1).prettyPrint());

		List<InheritedConnection> connectionsB2 = ((DiagramType) cu.typeDecls().get(3)).connections();
		assertEquals(2, connectionsB2.getNumChild());
		assertEquals("connect(a.out, d.in)", connectionsB2.getChild(0).prettyPrint());
		assertEquals("connect(d.out, b.in)", connectionsB2.getChild(1).prettyPrint());
		
		List<InheritedConnection> connectionsD = ((DiagramType) cu.typeDecls().get(4)).connections();
		assertEquals(3, connectionsD.getNumChild());
		assertEquals("connect(a.out, c.in)", connectionsD.getChild(0).prettyPrint());
		assertEquals("connect(c.out, d.in)", connectionsD.getChild(1).prettyPrint());
		assertEquals("connect(d.out, b.in)", connectionsD.getChild(2).prettyPrint());
	}

	private String createInterceptionDiamond() {
		String s;
		s  = "diagramtype T(in: Int => out: Int) {\n}\n";
		s += "diagramtype B {\n";
		s += "  a: T;\n";
		s += "  b: T;\n";
		s += "  connect(a.out, b.in);\n";
		s += "}\n";
		s += "diagramtype B1 extends B {\n";
		s += "  c: T;\n";
		s += "  intercept b.in with c.in, c.out;\n";
		s += "}\n";
		s += "diagramtype B2 extends B {\n";
		s += "  d: T;\n";
		s += "  intercept b.in with d.in, d.out;\n";
		s += "}\n";
		s += "diagramtype D extends B1, B2 {\n";
		s += "}";
		return s;
	}
	
	@Test
	public void interceptOfIntercepted() {
		String s;
		s  = "diagramtype T(in: Int => out: Int) {\n}\n";
		s += "diagramtype B1(in1: Int => out1: Int) {\n";
		s += "  a: T;\n";
		s += "  b: T;\n";
		s += "  connect(a.out, b.in);\n";
		s += "}\n";
		s += "diagramtype B2(in2: Int => out2: Int) extends B1 {\n";
		s += "  c: T;\n";
		s += "  intercept b.in with c.in, c.out;\n";
		s += "}\n";
		s += "diagramtype B3(in3: Int => out3: Int) extends B2 {\n";
		s += "  d: T;\n";
		s += "  intercept b.in with d.in, d.out;\n";
		s += "}";

		CompilationUnit cu = parseAndPrettyPrint(s);

		List<InheritedConnection> connectionsB3 = ((DiagramType) cu.typeDecls().get(3)).connections();
		assertEquals(3, connectionsB3.getNumChild());
		assertEquals("connect(a.out, c.in)", connectionsB3.getChild(0).prettyPrint());
		assertEquals("connect(c.out, d.in)", connectionsB3.getChild(1).prettyPrint());
		assertEquals("connect(d.out, b.in)", connectionsB3.getChild(2).prettyPrint());
	}
	
	@Test
	public void dfo1() {
		String s =
			"diagramtype T(in: Int => out: Int) {\n}\n" +
			"diagramtype B {\n" +
			"  t1: T;\n" +
			"  t2: T;\n" +
			"}\n" +
			"diagramtype D extends B {\n" +
			"  t3: T;\n" +
			"}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		DiagramType D = (DiagramType) cu.typeDecls().get(2);
		assertEquals(1, D.getComponent(0).dfo());
		assertEquals(2, D.getComponent(1).dfo());
		assertEquals(3, D.getComponent(2).dfo());
	}
	
	@Test
	public void dfo2() {
		String s =
			"diagramtype T(in: Int => out: Int) {\n}\n" +
			"diagramtype B {\n" +
			"  t1: T;\n" +
			"  t2: T;\n" +
			"  connect(t2.out, t1.in);\n" +
			"}\n" +
			"diagramtype D extends B {\n" +
			"  t3: T;\n" +
			"  connect(t3.out, t2.in);\n" +
			"}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		DiagramType B = (DiagramType) cu.typeDecls().get(1);
		assertEquals(2, B.getComponent(0).dfo());
		assertEquals(1, B.getComponent(1).dfo());
		
		DiagramType D = (DiagramType) cu.typeDecls().get(2);
		assertEquals(3, D.getComponent(0).dfo());
		assertEquals(2, D.getComponent(1).dfo());
		assertEquals(1, D.getComponent(2).dfo());
	}
	
	@Test
	public void dfoIntercept() {
		String s =
			"diagramtype T(in: Int => out: Int) {\n}\n" +
			"diagramtype B {\n" +
			"  t1: T;\n" +
			"  t2: T;\n" +
			"  connect(t2.out, t1.in);\n" +
			"}\n" +
			"diagramtype D extends B {\n" +
			"  t3: T;\n" +
			"  intercept t1.in with t3.in, t3.out;\n" +
			"}";
		CompilationUnit cu = parseAndPrettyPrint(s);

		DiagramType D = (DiagramType) cu.typeDecls().get(2);
		assertEquals(3, D.getComponent(0).dfo());
		assertEquals(1, D.getComponent(1).dfo());
		assertEquals(2, D.getComponent(2).dfo());
	}
	
	@Test
	public void dfoDiamondProblem() {
		String s = createInterceptionDiamond();
		CompilationUnit cu = parseAndPrettyPrint(s);

		DiagramType D = (DiagramType) cu.typeDecls().get(4);
		assertEquals(1, D.getComponent(0).dfo());
		assertEquals(4, D.getComponent(1).dfo());
		assertEquals(2, D.getComponent(2).dfo());
		assertEquals(3, D.getComponent(3).dfo());
	}

	@Test
	public void isInherited() {
		String s = createInterceptionDiamond();
		CompilationUnit cu = parseAndPrettyPrint(s);
		
		DiagramType B = (DiagramType) cu.typeDecls().get(1);
		assertFalse(B.getConnection(0).isInherited());
		assertFalse(B.getConnection(0).isIntercept());
		
		DiagramType B1 = (DiagramType) cu.typeDecls().get(2);
		for (Connection c: B1.getConnections()) {
			assertFalse(c.isInherited());
			assertTrue(c.isIntercept());
		}
		
		DiagramType D = (DiagramType) cu.typeDecls().get(4);
		for (Component c: D.getComponents()) {
			assertTrue(c.isInherited());
		}
		for (Connection c: D.getConnections()) {
			assertTrue(c.isInherited());
			assertTrue(c.isIntercept());
		}
	}
	
	private void compareOrderedColl(Collection<DiagramType> actual, DiagramType ...expected) {
		Iterator<DiagramType> actualTypes = actual.iterator();
		for (DiagramType e: expected) {
			assertSame(e, actualTypes.next());
		}
	}

	private CompilationUnit parseAndPrettyPrint(String s) {
		Program p = parseValidProgram(s);
		CompilationUnit cu = p.getCompilationUnit(0);
		assertEquals(s.trim(), cu.prettyPrint().trim());
		return cu;
	}
}
