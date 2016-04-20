package org.bloqqi.tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.bloqqi.compiler.ast.ASTNode;
import org.bloqqi.compiler.ast.CompilationUnit;
import org.bloqqi.compiler.ast.Component;
import org.bloqqi.compiler.ast.ComponentParameter;
import org.bloqqi.compiler.ast.Connection;
import org.bloqqi.compiler.ast.DiagramType;
import org.bloqqi.compiler.ast.FlowDecl;
import org.bloqqi.compiler.ast.IntLiteral;
import org.bloqqi.compiler.ast.Pair;
import org.bloqqi.compiler.ast.Parameter;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.compiler.ast.SimpleVarUse;
import org.bloqqi.compiler.ast.VarUse;
import org.bloqqi.tests.testsuite.TestSuite;

public class EditorHelpTests extends TestSuite {
	@Test
	public void testAccessInlinedComponentInParameter() {
		String str = 
			"diagramtype Main() {" +
			"	inline A a;" +
			"	inline A { "+
			"		Add add2;" +
			"		connect(in, add2.in2);" +
			"   } a2;" +
			"}" +
			"diagramtype A(Int in) {" +
			"	Add add;" +
			"   connect(in, add.in1);" +
			"}";

		DiagramType dt = parseValidDiagramType(str);
		
		Component a_add = dt.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertTrue(a_add.getInParameter(0).canAccess());
		assertEquals("a.in", a_add.getInParameter(0).access().toString());
		assertFalse(a_add.getInParameter(1).canAccess());
		assertNull(a_add.getInParameter(1).access());

		Component a2_add = dt.components().getChild(1);
		assertEquals("a2$add", a2_add.name());
		assertTrue(a2_add.getInParameter(0).canAccess());
		assertEquals("a2.in", a2_add.getInParameter(0).access().toString());
		assertFalse(a2_add.getInParameter(1).canAccess());
		assertNull(a2_add.getInParameter(1).access());

		Component a2_add2 = dt.components().getChild(2);
		assertEquals("a2$add2", a2_add2.name());
		assertFalse(a2_add2.getInParameter(0).canAccess());
		assertNull(a2_add2.getInParameter(0).access());
		assertTrue(a2_add2.getInParameter(1).canAccess());
		assertEquals("a2.in", a2_add2.getInParameter(1).access().toString());
	}
	
	@Test
	public void testAccessInlinedComponentOutParameter() {
		String str = 
			"diagramtype Main() {" +
			"	inline A a;" +
			"	inline A (=> Int out2) { "+
			"		Add add2;" +
			"		connect(add2, out2);" +
			"   } a2;" +
			"}" +
			"diagramtype A(=> Int out) {" +
			"	Add add;" +
			"   connect(add, out);" +
			"}";

		DiagramType dt = parseValidDiagramType(str);
		
		Component a_add = dt.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertTrue(a_add.getOutParameter(0).canAccess());
		assertEquals("a.out", a_add.getOutParameter(0).access().toString());

		Component a2_add = dt.components().getChild(1);
		assertEquals("a2$add", a2_add.name());
		assertTrue(a2_add.getOutParameter(0).canAccess());
		assertEquals("a2.out", a2_add.getOutParameter(0).access().toString());
		
		Component a2_add2 = dt.components().getChild(2);
		assertEquals("a2$add2", a2_add2.name());
		assertTrue(a2_add2.getOutParameter(0).canAccess());
		assertEquals("a2.out2", a2_add2.getOutParameter(0).access().toString());
	}
	
	@Test
	public void testAccessInlinedTransitive() {
		String str = 
			"diagramtype Main() {" +
			"	inline A a; "+
			"}" +
			"diagramtype A(Int in) {" +
			"	inline B b;" +
			"   connect(in, b.in);" +
			"}" + 
			"diagramtype B(Int in) {" +
			"	Add add;" + 
			"	connect(in, add.in1);" + 
			"}";

		DiagramType dt = parseValidDiagramType(str);
		
		Component a_b_add = dt.components().getChild(0);
		assertEquals("a$b$add", a_b_add.name());
		assertTrue(a_b_add.getInParameter(0).canAccess());
		assertEquals("a.in", a_b_add.getInParameter(0).access().toString());
		assertFalse(a_b_add.getInParameter(1).canAccess());
		assertNull(a_b_add.getInParameter(1).access());
	}
	
	@Test
	public void testAccessLocalInlinedComponents() {
		String str = 
			"diagramtype Main() {" +
			"	inline A a; "+
			"}" +
			"diagramtype A(Int in) {" +
			"	Add add;" +
			"}";

		DiagramType dt = parseValidDiagramType(str);
		
		Component a_add = dt.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertFalse(a_add.getInParameter(0).canAccess());
		assertFalse(a_add.getInParameter(1).canAccess());
		assertFalse(a_add.getOutParameter(0).canAccess());
	}
	
	@Test
	public void canDeleteInlinedConnections() {
		String str =
			"diagramtype Main(Int in => Int out) {" +
			"	Add add;" +
			"	Add add2;" +
			"	inline A a;" +
			"	connect(in, a);" +
			"	connect(a, out);" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	Add add;" +
			"	Add add2;" +
			"	connect(in, add.in1);" +
			"	connect(add, add2.in1);" +
			"	connect(add2, out);" +
			"}";
		DiagramType dt = parseValidDiagramType(str);
		
		assertFalse(dt.connections().getChild(0).canDelete());
		assertTrue(dt.connections().getChild(1).canDelete());
		assertTrue(dt.connections().getChild(2).canDelete());
	}
	
	@Test
	public void canDeleteInlinedConnections2() {
		String str =
			"diagramtype Main(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"diagramtype SubMain extends Main {" +
			"	inline A a;" +
			"	intercept out with a, a;" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	Add add;" +
			"	connect(in, add.in1);" +
			"	connect(add, out);" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0) ;
		
		DiagramType dtSubMain = (DiagramType) cu.getDeclaration(1);
		assertTrue(dtSubMain.connections().getChild(0).canDelete());
		assertTrue(dtSubMain.connections().getChild(1).canDelete());
	}
	
	@Test
	public void canDeleteInlinedConnections3() {
		String str =
			"diagramtype Main(Int in => Int out) {" +
			"	inline A a;" +
			"	connect(in, a);" +
			"	connect(a, out);" +
			"}" +
			"diagramtype SubMain extends Main {" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	Add add;" +
			"	connect(in, add.in1);" +
			"	connect(add, out);" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0) ;
		
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		assertTrue(dtMain.connections().getChild(0).canDelete());
		assertTrue(dtMain.connections().getChild(1).canDelete());
		
		DiagramType dtSubMain = (DiagramType) cu.getDeclaration(1);
		assertFalse(dtSubMain.connections().getChild(0).canDelete());
		assertFalse(dtSubMain.connections().getChild(1).canDelete());
	}
	
	
	@Test
	public void testDeclaredFlowDeclForInline() {
		String str =
			"diagramtype Main(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"diagramtype SubMain extends Main {" +
			"	inline A a;" +
			"	intercept out with a, a;" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	Add add;" +
			"	connect(in, add.in1);" +
			"	connect(add, out);" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0) ;
		
		DiagramType dtSubMain = (DiagramType) cu.getDeclaration(1);
		FlowDecl fd = dtSubMain.getFlowDecl(0);
		assertSame(fd, dtSubMain.connections().getChild(0).getDeclaredFlowDecl());
		assertSame(fd, dtSubMain.connections().getChild(1).getDeclaredFlowDecl());
	}
	
	
	@Test
	public void canModifyToAccess() {
		String str =
			"diagramtype Main() {" +
			"	inline A a1;" +
			"	inline A { Add add2; } a2;" +
			"}" +
			"diagramtype A() {" +
			"	Add add;" +
			"}" +
			"diagramtype B { " +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a1_add = dtMain.components().getChild(0);
		assertEquals("a1$add", a1_add.name());
		assertFalse(a1_add.getInParameter(0).canAccess());
		assertTrue(a1_add.getInParameter(0).canModifyToAccess());
		assertTrue(!a1_add.getInParameter(0).hasAnonymousTypesTransitively());

		Component a2_add = dtMain.components().getChild(1);
		assertEquals("a2$add", a2_add.name());
		assertFalse(a2_add.getInParameter(0).canAccess());
		assertTrue(a2_add.getInParameter(0).canModifyToAccess());

		Component a2_add2 = dtMain.components().getChild(2);
		assertEquals("a2$add2", a2_add2.name());
		assertFalse(a2_add2.getInParameter(0).canAccess());
		assertTrue(a2_add2.getInParameter(0).canModifyToAccess());
	}
	
	@Test
	public void canModifyToAccessOnlyLocalComponents() {
		String str =
			"diagramtype Main {" +
			"	inline A { } a;" +
			"}" +
			"diagramtype SubMain extends Main {" +
			"}" +
			"diagramtype A {" +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);

		// Local component - ok
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		Component main_a_add = dtMain.components().getChild(0);
		assertEquals("a$add", main_a_add.name());
		assertFalse(main_a_add.getInParameter(0).canAccess());
		assertTrue(main_a_add.getInParameter(0).canModifyToAccess());

		// Inherited component - not ok
		DiagramType dtSubMain = (DiagramType) cu.getDeclaration(1);
		Component subMain_a_add = dtSubMain.components().getChild(0);
		assertEquals("a$add", subMain_a_add.name());
		assertFalse(subMain_a_add.getInParameter(0).canAccess());
		assertFalse(subMain_a_add.getInParameter(0).canModifyToAccess());
	}
	
	@Test
	public void canModifyToAccessTransitive() {
		String str =
			"diagramtype Main {" +
			"	inline A { inline B { Add add2; } b; } a;" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B { " +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_add = dtMain.components().getChild(0);
		assertEquals("a$b$add", a_b_add.name());
		assertFalse(a_b_add.getInParameter(0).canAccess());
		assertTrue(a_b_add.getInParameter(0).canModifyToAccess());

		Component a_b_add2 = dtMain.components().getChild(1);
		assertEquals("a$b$add2", a_b_add2.name());
		assertFalse(a_b_add2.getInParameter(0).canAccess());
		assertTrue(a_b_add2.getInParameter(0).canModifyToAccess());
	}
	
	@Test
	public void canModifyToAccessTransitive2() {
		String str =
			"diagramtype Main {" +
			"	inline A { inline B { } b; } a;" +
			"}" +
			"diagramtype A { }" +
			"diagramtype B { " +
			"	inline C { Add add; } c;" +
			"}" +
			"diagramtype C { }";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_c_add = dtMain.components().getChild(0);
		assertEquals("a$b$c$add", a_b_c_add.name());
		assertFalse(a_b_c_add.getInParameter(0).canAccess());
		assertTrue(a_b_c_add.getInParameter(0).canModifyToAccess());
		assertFalse(a_b_c_add.getInParameter(0).hasAnonymousTypesTransitively());
	}
	
	@Test
	public void canModifyToAccessTransitive3() {
		String str =
			"diagramtype Main {" +
			"	inline A { inline B { connect(in, add.in2); } b; } a;" +
			"}" +
			"diagramtype A { }" +
			"diagramtype B(Int in) { " +
			"	Add add;" +
			"	connect(in, add.in1);" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_add = dtMain.components().getChild(0);
		assertEquals("a$b$add", a_b_add.name());
		assertFalse(a_b_add.getInParameter(0).canAccess());
		assertFalse(a_b_add.getInParameter(0).canModifyToAccess());
		assertFalse(a_b_add.getInParameter(1).canAccess());
		assertFalse(a_b_add.getInParameter(1).canModifyToAccess());

	}
	
	@Test
	public void modifyToAccess() {
		String str =
			"diagramtype Main(Int in) {" +
			"  inline a: A { " +
			"     Add add2; " +
			"  };" +
			"}" +
			"diagramtype A {" +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_add = dtMain.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertFalse(a_add.getInParameter(0).canAccess());
		assertTrue(a_add.getInParameter(0).canModifyToAccess());
		
		Component a_add2 = dtMain.components().getChild(1);
		assertEquals("a$add2", a_add2.name());
		assertFalse(a_add2.getInParameter(0).canAccess());
		assertTrue(a_add2.getInParameter(0).canModifyToAccess());

		// Access a$add.in1
		a_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_add.getInParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p.second));
		cu.program().flushAllAttributes();
//		assertEquals("[]", cu.errors().toString());

		// Access a$add2.in1
		a_add2 = dtMain.components().getChild(1);
		Pair<Component, VarUse> p2 = a_add2.getInParameter(0).modifyToAccess();
		assertNotNull(p2);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p2.second));
		cu.program().flushAllAttributes();
//		assertEquals("[]", cu.errors().toString());
		
		String expected =
				"diagramtype Main(in: Int) {\n" +
				"  inline a: A (addin1: Int, add2in1: Int) {\n" +
				"    add2: Add;\n" +
				"    connect(addin1, A::add.in1);\n" +
				"    connect(add2in1, add2.in1);\n" +
				"  };\n" +
				"  connect(in, a.addin1);\n" +
				"  connect(in, a.add2in1);\n" +
				"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyToAccessTransitive() {
		String str =
			"diagramtype Main(Int in) {" +
			"	inline A { inline B { Add add2; } b; } a;" +
			"}" +
			"diagramtype A { }" +
			"diagramtype B {" +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_add = dtMain.components().getChild(0);
		assertEquals("a$b$add", a_b_add.name());
		assertFalse(a_b_add.getInParameter(0).canAccess());
		assertTrue(a_b_add.getInParameter(0).canModifyToAccess());
		
		Component a_b_add2 = dtMain.components().getChild(1);
		assertEquals("a$b$add2", a_b_add2.name());
		assertFalse(a_b_add2.getInParameter(0).canAccess());
		assertTrue(a_b_add2.getInParameter(0).canModifyToAccess());
		
		// Access a$b$add.in1
		a_b_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_b_add.getInParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p.second));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		// Access a$b$add2.in1
		a_b_add2 = dtMain.components().getChild(1);
		Pair<Component, VarUse> p2 = a_b_add2.getInParameter(0).modifyToAccess();
		assertNotNull(p2);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p2.second));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		String expected = 
			"diagramtype Main(in: Int) {\n" +
			"  inline a: A (bin1: Int, bin12: Int) {\n" +
			"    inline b: B (addin1: Int, add2in1: Int) {\n" +
			"      add2: Add;\n" +
			"      connect(addin1, B::add.in1);\n" +
			"      connect(add2in1, add2.in1);\n" +
			"    };\n" +
			"    connect(bin1, b.addin1);\n" +
			"    connect(bin12, b.add2in1);\n" +
			"  };\n" +
			"  connect(in, a.bin1);\n" +
			"  connect(in, a.bin12);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
		
		// Remove connection and try to access it again
		dtMain.getFlowDeclList().removeChild(0);
		a_b_add = dtMain.components().getChild(0);
		assertTrue(a_b_add.getInParameter(0).canAccess());
		assertFalse(a_b_add.getInParameter(0).canModifyToAccess());
		
		// Access a$b$add.in1 again
		VarUse access = a_b_add.getInParameter(0).access();
		Connection newConn = new Connection(new SimpleVarUse("in"), access);
		dtMain.getFlowDecls().insertChild(newConn, 0);
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyToAccessOutParameter() {
		String str =
			"diagramtype Main(=> Int out1, Int out2) {" +
			"	inline A { Add add2; } a;" +
			"}" +
			"diagramtype A {" +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_add = dtMain.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertFalse(a_add.getOutParameter(0).canAccess());
		assertTrue(a_add.getOutParameter(0).canModifyToAccess());
		
		Component a_add2 = dtMain.components().getChild(1);
		assertEquals("a$add2", a_add2.name());
		assertFalse(a_add2.getOutParameter(0).canAccess());
		assertTrue(a_add2.getOutParameter(0).canModifyToAccess());

		// Access a$add.out
		a_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_add.getOutParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(p.second, new SimpleVarUse("out1")));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		// Access a$add2.out
		a_add2 = dtMain.components().getChild(1);
		Pair<Component, VarUse> p2 = a_add2.getOutParameter(0).modifyToAccess();
		assertNotNull(p2);
		dtMain.addFlowDecl(new Connection(p2.second, new SimpleVarUse("out2")));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		String expected =
			"diagramtype Main( => out1: Int, out2: Int) {\n" +
			"  inline a: A ( => addout: Int, add2out: Int) {\n" +
			"    add2: Add;\n" +
			"    connect(A::add.out, addout);\n" +
			"    connect(add2.out, add2out);\n" +
			"  };\n" +
			"  connect(a.addout, out1);\n" +
			"  connect(a.add2out, out2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void modifyToAccessTransitiveOutParameter() {
		String str =
			"diagramtype Main(=> Int out) {" +
			"	inline A { inline B { Add add; } b; } a;" +
			"}" +
			"diagramtype A(=> Int notUsed) { }" +
			"diagramtype B(=> Int notUsed) { }";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_add = dtMain.components().getChild(0);
		assertEquals("a$b$add", a_b_add.name());
		assertFalse(a_b_add.getOutParameter(0).canAccess());
		assertTrue(a_b_add.getOutParameter(0).canModifyToAccess());
		
		// Access a$add.out
		a_b_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_b_add.getOutParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(p.second, new SimpleVarUse("out")));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		String expected =
			"diagramtype Main( => out: Int) {\n" +
			"  inline a: A ( => bout: Int) {\n" +
			"    inline b: B ( => addout: Int) {\n" +
			"      add: Add;\n" +
			"      connect(add.out, addout);\n" +
			"    };\n" +
			"    connect(b.addout, bout);\n" +
			"  };\n" +
			"  connect(a.bout, out);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyToAccessAlreadyExistingParameterName() {
		String str =
			"diagramtype Main(Int in) {" +
			"	inline A (Int addin1) { } a;" +
			"}" +
			"diagramtype A {" +
			"	Add add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_add = dtMain.components().getChild(0);
		assertEquals("a$add", a_add.name());
		assertFalse(a_add.getInParameter(0).canAccess());
		assertTrue(a_add.getInParameter(0).canModifyToAccess());
		
		// Access a$add.in1
		a_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_add.getInParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p.second));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());

		String expected =
				"diagramtype Main(in: Int) {\n" +
				"  inline a: A (addin1: Int, addin12: Int) {\n" +
				"    connect(addin12, A::add.in1);\n" +
				"  };\n" +
				"  connect(in, a.addin12);\n" +
				"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyToAccessNameError() {
		String str =
			"diagramtype Main(Int in) {" +
			"	inline A { inline B { Add_1; } b; } a;" +
			"}" +
			"diagramtype A { }" +
			"diagramtype B { }";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a_b_add = dtMain.components().getChild(0);
		
		assertEquals("a$b$Add_1", a_b_add.name());
		assertFalse(a_b_add.getInParameter(0).canAccess());
		assertTrue(a_b_add.getInParameter(0).canModifyToAccess());
		
		// Access a$b$Add_1.in1
		a_b_add = dtMain.components().getChild(0);
		Pair<Component, VarUse> p = a_b_add.getInParameter(0).modifyToAccess();
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new SimpleVarUse("in"), p.second));
		cu.program().flushAllAttributes();
		assertEquals("[]", cu.errors().toString());
		
		String expected = 
			"diagramtype Main(in: Int) {\n" +
			"  inline a: A (bin1: Int) {\n" +
			"    inline b: B (Add_1in1: Int) {\n" +
			"      Add_1;\n" +
			"      connect(Add_1in1, Add_1.in1);\n" +
			"    };\n" +
			"    connect(bin1, b.Add_1in1);\n" +
			"  };\n" +
			"  connect(in, a.bin1);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
		parseValidProgram(cu.prettyPrint());
	}

	@Test
	public void createAnonymousTypesInline() {
		String str =
			"diagramtype Main {" +
			"  inline a: A;" +
			"}" +
			"diagramtype A {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypesInline2() {
		String str =
			"diagramtype Main {" +
			"  inline a: A;" +
			"}" +
			"diagramtype A {" +
			"  inline b: B;" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);

		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypesInline3() {
		String str =
			"diagramtype Main {" +
			"  inline a: A { };" +
			"}" +
			"diagramtype A {" +
			"  inline b: B;" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();

		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void createAnonymousTypesInline4() {
		String str =
			"diagramtype Main {" +
			"  inline a: A;" +
			"}" +
			"diagramtype A {" +
			"  inline b: B { };" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void createAnonymousTypesInline5() {
		String str =
			"diagramtype Main(Int in) {" +
			"  inline a: A;" +
			"}" +
			"diagramtype SubMain() extends Main {" +
			"  redeclare a: super { };" +
			"}" +
			"diagramtype A {" +
			"  inline b: B { };" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtSubMain = (DiagramType) cu.getDeclaration(1);

		Component subMainAdd = dtSubMain.components().getChild(0);
		Component newComponent = subMainAdd.addAnonymousTypesToInlinedComponent();
		dtSubMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();

		String expected =
			"diagramtype SubMain extends Main {\n" +
			"  redeclare a: super {\n" +
			"    redeclare b: super {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtSubMain.prettyPrint());
	}
	
	
	@Test
	public void createAnonymousTypesInline6() {
		String str =
			"diagramtype Main {" +
			"  inline a: A { redeclare b: super { }; };" +
			"}" +
			"diagramtype A {" +
			"  inline b: B { };" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void createAnonymousTypesInline7() {
		String str =
			"diagramtype Main {" +
			"  inline a: A;" +
			"}" +
			"diagramtype A {" +
			"  inline b: B { };" +
			"}" +
			"diagramtype B {" +
			"  inline c: C { };" +
			"}" +
			"diagramtype C {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"      redeclare c: super {\n" +
			"      };\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void createAnonymousTypesInline8() {
		String str =
			"diagramtype Main {" +
			"  inline a: A { redeclare b: super { }; };" +
			"}" +
			"diagramtype A {" +
			"  inline b: B { };" +
			"}" +
			"diagramtype B {" +
			"  inline c: C;" +
			"}" +
			"diagramtype C {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    redeclare b: super {\n" +
			"      redeclare c: super {\n" +
			"      };\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypesInline9() {
		String str =
			"diagramtype Main {" +
			"  inline a: A{" +
			"    inline b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A {\n" +
			"    inline b: B {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypes() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component a = (Component) dtMain.lookup("a");
		Component b = (Component) a.type().lookup("b");
		java.util.List<Component> components = Arrays.asList(a, b);
		Component newComponent = Component.addAnonymousTypesForNestedComponents(components);
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    b: B {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypes2() {
		String str =
			"diagramtype Main {" +
			"  a: A;" +
			"}" +
			"diagramtype A {" +
			"  b: B { };" +
			"}" +
			"diagramtype B {" +
			"  c: C { };" +
			"}" +
			"diagramtype C {" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);

		Component a = (Component) dtMain.lookup("a");
		Component b = (Component) a.type().lookup("b");
		Component c = (Component) b.type().lookup("c");
		java.util.List<Component> components = Arrays.asList(a, b, c);
		Component newComponent = Component.addAnonymousTypesForNestedComponents(components);
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    redeclare b: super {\n" +
			"      redeclare c: super {\n" +
			"      };\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void createAnonymousTypes10() {
		String str =
			"diagramtype Main {" +
			"  inline a: A (in: Int) {" +
			"    inline b: B[in];" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int) {" +
			"  add: Add;" +
			"}" +
			"wiring B[Int v] {" +
			"  connect(v, B.in);" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component mainAdd = dtMain.components().getChild(0);
		Component newComponent = mainAdd.addAnonymousTypesToInlinedComponent();
		dtMain.getLocalComponents().setChild(newComponent, 0);
		cu.program().flushAllAttributes();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A (in: Int) {\n" +
			"    inline b: B[in] {\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyAndAccessComplete() {
		String str =
			"diagramtype Main {" +
			"  inline a: A;" +
			"}" +
			"diagramtype A {" +
			"  inline b: B;" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component mainAdd = dtMain.components().getChild(0);
		mainAdd.getInParameter(0).modifyToAccess();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A (bin1: Int) {\n" +
			"    redeclare b: super (addin1: Int) {\n" +
			"      connect(addin1, B::add.in1);\n" +
			"    };\n" +
			"    connect(bin1, b.addin1);\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void modifyAndAccessComplete2() {
		String str =
			"diagramtype Main {" +
			"  inline a: A{" +
			"    inline b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"  add: Add;" +
			"}";
		CompilationUnit cu = parseValidProgram(str).getCompilationUnit(0);
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		
		Component mainAdd = dtMain.components().getChild(0);
		mainAdd.getInParameter(0).modifyToAccess();
		
		String expected =
			"diagramtype Main {\n" +
			"  inline a: A (bin1: Int) {\n" +
			"    inline b: B (addin1: Int) {\n" +
			"      connect(addin1, B::add.in1);\n" +
			"    };\n" +
			"    connect(bin1, b.addin1);\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void testAddConnectionsParameters() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int, in2: Int) {" +
			"}";

		Program program = parseValidProgram(str);

		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Component a = (Component) dtMain.lookup("a");
		Component b = (Component) a.type().lookup("b");
		ComponentParameter in = (ComponentParameter) b.findMember("in2");
		
		Pair<Component, VarUse> p = ASTNode.addConnectionsParameters(a, a, in, null);
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		dtMain.getLocalComponentList().setChild(p.first, 0);
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
		
		String expected =
			"diagramtype Main {\n" +
			"  a: A (bin2: Int) {\n" +
			"    b: B;\n" +
			"    connect(bin2, b.in2);\n" +
			"  };\n" +
			"  connect(5, a.bin2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}

	
	@Test
	public void testAddConnectionsParameters2() {
		String str =
			"diagramtype Main {" +
			"  inline a: A {" +
			"    b: B {" +
			"      c: C (in: Int) {" +
			"      };" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype C {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Component a = (Component) dtMain.lookup("a");
		Component b = (Component) a.type().lookup("b");
		Component c = (Component) b.type().lookup("c");
		ComponentParameter in = (ComponentParameter) c.findMember("in");
		
		try {
			// Reversing the arguments should yield an exception
			ASTNode.addConnectionsParameters(b, a, in, null);
			fail("An exception should be thrown here");
		} catch (RuntimeException e) {
			// OK
		}
		
		Pair<Component, VarUse> p = ASTNode.addConnectionsParameters(a, b, in, null);
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		dtMain.getLocalComponentList().setChild(p.first, 0);
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  inline a: A (bin: Int) {\n" +
		"    b: B (cin: Int) {\n" +
		"      c: C (in: Int) {\n" +
		"      };\n" +
		"      connect(cin, c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void testAddConnectionsParameters3() {
		String str =
			"diagramtype Main {" +
			"  inline a: A {" +
			"    b: B {" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"  c: C;" +
			"}" +
			"diagramtype C(in: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Component a = (Component) dtMain.lookup("a");
		Component b = (Component) a.type().lookup("b");
		Component c = (Component) b.type().lookup("c");
		ComponentParameter in = (ComponentParameter) c.findMember("in");
		
		Pair<Component, VarUse> p = ASTNode.addConnectionsParameters(a, b, in, null);
		assertNotNull(p);
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		dtMain.getLocalComponentList().setChild(p.first, 0);
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  inline a: A (bin: Int) {\n" +
		"    b: B (cin: Int) {\n" +
		"      connect(cin, B::c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void testAddConnectionsParametersPath() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int, in2: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a.b.in2", "bin2");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
		
		String expected =
			"diagramtype Main {\n" +
			"  a: A (bin2: Int) {\n" +
			"    b: B;\n" +
			"    connect(bin2, b.in2);\n" +
			"  };\n" +
			"  connect(5, a.bin2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void testAddConnectionsParametersPath2() {
		String str =
			"diagramtype Main {" +
			"  inline a: A {" +
			"    b: B {" +
			"      c: C (in: Int) {" +
			"      };" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype C {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a.b.c.in", "bin");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  inline a: A (bin: Int) {\n" +
		"    b: B (cin: Int) {\n" +
		"      c: C (in: Int) {\n" +
		"      };\n" +
		"      connect(cin, c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void testAddConnectionsParametersPath3() {
		String str =
			"diagramtype Main {" +
			"  inline a: A {" +
			"    b: B {" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"  c: C;" +
			"}" +
			"diagramtype C(in: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a.b.c.in", "bin");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  inline a: A (bin: Int) {\n" +
		"    b: B (cin: Int) {\n" +
		"      connect(cin, B::c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void testAddConnectionsParametersPath4() {
		String str =
			"diagramtype Main {" +
			"  a1: A;" +
			"  a2: A;" +
			"  a3: A {" +
			"    b: B;" +
			"  };" +
			"  a4: A;" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int, in2: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a3.b.in2", "bin2");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
		
		String expected =
			"diagramtype Main {\n" +
			"  a1: A;\n" +
			"  a2: A;\n" +
			"  a3: A (bin2: Int) {\n" +
			"    b: B;\n" +
			"    connect(bin2, b.in2);\n" +
			"  };\n" +
			"  a4: A;\n" +
			"  connect(5, a3.bin2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	
	@Test
	public void testAddConnectionsParametersPath5() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int, in2: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addConnectionsParametersToDiagramType("a.b.in2", "ain2");
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
		
		String expected =
			"diagramtype Main(ain2: Int) {\n" +
			"  a: A (bin2: Int) {\n" +
			"    b: B;\n" +
			"    connect(bin2, b.in2);\n" +
			"  };\n" +
			"  connect(ain2, a.bin2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void isParameterExposedTo() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B {" +
			"      c: C (in: Int, in2: Int) {" +
			"      };" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype C {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a.b.c.in", "bin");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  a: A (bin: Int) {\n" +
		"    b: B (cin: Int) {\n" +
		"      c: C (in: Int, in2: Int) {\n" +
		"      };\n" +
		"      connect(cin, c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
		
		DiagramType anonDt = p.first.anonymousDiagramType();
		Parameter binPar = (Parameter) anonDt.lookupInnerVarDecl("bin");
		assertSame(binPar, anonDt.isInnerParameterExposed("b.cin"));
		assertSame(binPar, anonDt.isInnerParameterExposed("b.c.in"));
		assertSame(binPar, anonDt.isInnerParameterExposed("bin"));

		assertNull(anonDt.isInnerParameterExposed("b.c.in2"));
		assertNull(anonDt.isInnerParameterExposed("b.c2.in"));
	}
	
	@Test
	public void isParameterExposedTo2() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B(in: Int, in2: Int) {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addConnectionsParametersToDiagramType("a.b.in2", "ain2");
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
		
		String expected =
			"diagramtype Main(ain2: Int) {\n" +
			"  a: A (bin2: Int) {\n" +
			"    b: B;\n" +
			"    connect(bin2, b.in2);\n" +
			"  };\n" +
			"  connect(ain2, a.bin2);\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
		
		DiagramType anonDt = dtMain.getLocalComponent(0).anonymousDiagramType();
		Parameter binPar = (Parameter) anonDt.lookupInnerVarDecl("bin2");
		assertSame(binPar, anonDt.isInnerParameterExposed("b.in2"));
		assertNull(anonDt.isInnerParameterExposed("b.in"));
		assertNull(anonDt.isInnerParameterExposed("b.z.in"));
		assertNull(anonDt.isInnerParameterExposed("in.b"));
	}
	
	/**
	 * Test inlined blocks together with isParameterExposed
	 */
	@Test
	public void isParameterExposedTo3() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    inline b: B {" +
			"      c: C (in: Int, in2: Int) {" +
			"      };" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype C {" +
			"}";

		Program program = parseValidProgram(str);
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		Pair<Component, VarUse> p = dtMain.addConnectionsParameters("a.b.c.in", "bin");
		dtMain.addFlowDecl(new Connection(new IntLiteral(5), p.second));
		program.flushAllAttributes();
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());

		String expected =
		"diagramtype Main {\n" +
		"  a: A (bin: Int) {\n" +
		"    inline b: B (cin: Int) {\n" +
		"      c: C (in: Int, in2: Int) {\n" +
		"      };\n" +
		"      connect(cin, c.in);\n" +
		"    };\n" +
		"    connect(bin, b.cin);\n" +
		"  };\n" +
		"  connect(5, a.bin);\n" +
		"}\n";
		assertEquals(expected, dtMain.prettyPrint());
		
		DiagramType anonDt = p.first.anonymousDiagramType();
		Parameter binPar = (Parameter) anonDt.lookupInnerVarDecl("bin");
		assertSame(binPar, anonDt.isInnerParameterExposed("b.cin"));
		assertSame(binPar, anonDt.isInnerParameterExposed("b.c.in"));
		assertSame(binPar, anonDt.isInnerParameterExposed("bin"));

		assertNull(anonDt.isInnerParameterExposed("b.c.in2"));
		assertNull(anonDt.isInnerParameterExposed("b.c2.in"));
	}
}
