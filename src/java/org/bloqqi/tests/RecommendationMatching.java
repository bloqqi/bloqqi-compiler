package org.bloqqi.tests;

import static org.junit.Assert.assertEquals;

import org.bloqqi.compiler.ast.DiagramType;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.compiler.ast.SpecializeDiagramType;
import org.bloqqi.tests.testsuite.TestSuite;
import org.junit.Test;

public class RecommendationMatching extends TestSuite {
	@Test
	public void simpleMatch1() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		String expected = dtMain.prettyPrint();
		SpecializeDiagramType spec = dtA.specialize(dtMain.getLocalComponent(0).anonymousDiagramType());
		dtMain.setLocalComponent(spec.newAnonymousComponent("a"), 0);
		dtMain.flushAllAttributes();
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void simpleMatch2() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: SubB;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype SubB extends B {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		String expected = dtMain.prettyPrint();
		SpecializeDiagramType spec = dtA.specialize(dtMain.getLocalComponent(0).anonymousDiagramType());
		dtMain.setLocalComponent(spec.newAnonymousComponent("a"), 0);
		dtMain.flushAllAttributes();
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void simpleMatch3() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    redeclare b: SubB;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype SubB extends B {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		String expected = dtMain.prettyPrint();
		SpecializeDiagramType spec = dtA.specialize(dtMain.getLocalComponent(0).anonymousDiagramType());
		dtMain.setLocalComponent(spec.newAnonymousComponent("a"), 0);
		dtMain.flushAllAttributes();
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void simpleMatch4() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b2: SubB;" +
			"    redeclare b: SubB;" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype SubB extends B {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"  b2: B;" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		String expected = dtMain.prettyPrint();
		SpecializeDiagramType spec = dtA.specialize(dtMain.getLocalComponent(0).anonymousDiagramType());
		dtMain.setLocalComponent(spec.newAnonymousComponent("a"), 0);
		dtMain.flushAllAttributes();
		assertEquals(expected, dtMain.prettyPrint());
	}
}
