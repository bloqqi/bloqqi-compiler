package org.bloqqi.tests;


import org.junit.Test;
import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class FeatureTests extends TestSuite {
	@Test
	public void featureSelection() {
		String str =
			"diagramtype A { } " +
			"diagramtype F extends A { } " +
			"diagramtype G extends A { } " +
			"features A {" +
			"  f: F;" +
			"  g: G;" +
			"}" +
			"diagramtype Main { }";

		DiagramType dt = parseValidDiagramType(str);
		FeatureSelection selection = dt.featureSelection();
		for (FeatureSelectionOptional opt: selection.getOptionalFeatures()) {
			opt.setSelected(true);
		}

		Program p = dt.program();
		DiagramType main = (DiagramType) dt.lookupType("Main");
		main.addLocalBlock(selection.newAnonymousBlock("a"));
		p.flushAllAttributes();

		// No errors
		assertEquals("[]", dt.compUnit().errors().toString());

		// Expected anonymous type
		String expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    feature f;\n" +
			"    feature g;\n" +
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, main.prettyPrint());
	}

	@Test
	public void excludes() {
		String str =
			"diagramtype A { } " +
			"diagramtype F extends A { } " +
			"diagramtype G extends A { } " +
			"features A {" +
			"  f: F;" +
			"  g: G;" +
			"  f excludes g;\n" +
			"}" +
			"diagramtype Main { }";

		DiagramType dt = parseValidDiagramType(str);
		FeatureSelection selection = dt.featureSelection();
		for (FeatureSelectionOptional opt: selection.getOptionalFeatures()) {
			opt.setSelected(true);
		}

		Program p = dt.program();
		DiagramType main = (DiagramType) dt.lookupType("Main");
		main.addLocalBlock(selection.newAnonymousBlock("a"));
		p.flushAllAttributes();

		// No errors
		assertEquals("[]", dt.compUnit().errors().toString());

		// Expected anonymous type
		String expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    feature g;\n" +
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, main.prettyPrint());
	}


	@Test
	public void before() {
		String str =
			"diagramtype A { } " +
			"diagramtype F extends A { } " +
			"diagramtype G extends A { } " +
			"diagramtype H extends A { } " +
			"features A {" +
			"  f: F;" +
			"  g: G;" +
			"  h: H;" +
			"  h before f;\n" +
			"  f before g;\n" +
			"}" +
			"diagramtype Main { }";

		DiagramType dt = parseValidDiagramType(str);
		FeatureSelection selection = dt.featureSelection();
		for (FeatureSelectionOptional opt: selection.getOptionalFeatures()) {
			opt.setSelected(true);
		}

		Program p = dt.program();
		DiagramType main = (DiagramType) dt.lookupType("Main");
		main.addLocalBlock(selection.newAnonymousBlock("a"));
		p.flushAllAttributes();

		// No errors
		assertEquals("[]", dt.compUnit().errors().toString());

		// Expected anonymous type
		String expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    feature h;\n" +
			"    feature f;\n" +
			"    feature g;\n" +
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, main.prettyPrint());
	}
}
