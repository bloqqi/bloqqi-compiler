package org.bloqqi.tests;

import static org.junit.Assert.assertEquals;

import org.bloqqi.compiler.ast.Component;
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
		testMatching(dtMain);
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
		testMatching(dtMain);
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
		testMatching(dtMain);
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
		testMatching(dtMain);
	}

	@Test
	public void nestedMatch1() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: B {" +
			"      c: C;" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype C {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}" +
			"recommendation B {" +
			"  c: C;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		testMatching(dtMain);
	}

	@Test
	public void nestedMatch2() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b: SubB {" +
			"      c: SubC;" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype SubB extends B {" +
			"}" +
			"diagramtype C {" +
			"}" +
			"diagramtype SubC extends C {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}" +
			"recommendation B {" +
			"  c: C;" +
			"}" +
			"recommendation SubB extends super;";

		Program program = parseValidProgram(str);
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		testMatching(dtMain);
	}

	@Test
	public void nestedMatch3() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    redeclare b: B {" +
			"      redeclare c: SubC;" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"  c: C;" +
			"}" +
			"diagramtype C {" +
			"}" +
			"diagramtype SubC extends C {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"}" +
			"recommendation B {" +
			"  replaceable c;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		testMatching(dtMain);
	}

	@Test
	public void nestedMatch4() {
		String str =
			"diagramtype Main {" +
			"  a: A {" +
			"    b2: SubB {" +
			"      c2: SubC;" +
			"      redeclare c: SubC;" +
			"    };" +
			"    redeclare b: SubB {" +
			"      c2: C;" +
			"      redeclare c: SubC;" +
			"    };" +
			"  };" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"  c: C;" +
			"}" +
			"diagramtype SubB extends B {" +
			"}" +
			"diagramtype C {" +
			"}" +
			"diagramtype SubC extends C {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"  b2: B;" +
			"}" +
			"recommendation B {" +
			"  replaceable c;" +
			"  c2: C;" +
			"}" +
			"recommendation SubB extends super;";

		Program program = parseValidProgram(str);
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		testMatching(dtMain);
	}

	private void testMatching(DiagramType dtMain) {
		Component c = dtMain.getLocalComponent(0);
		DiagramType specType = c.type().directSuperTypes().iterator().next();

		String expected = dtMain.prettyPrint();
		SpecializeDiagramType spec = specType.specialize(c.anonymousDiagramType());
		dtMain.setLocalComponent(spec.newAnonymousComponent(c.name()), 0);
		dtMain.flushAllAttributes();
		assertEquals(expected, dtMain.prettyPrint());
	}
}
