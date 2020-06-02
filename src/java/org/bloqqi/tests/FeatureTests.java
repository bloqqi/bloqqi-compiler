package org.bloqqi.tests;


import java.util.SortedSet;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		SortedSet<FeatureSelectionOptional> opts = selection.getOptionalFeatures();
		assertEquals(2, opts.size());
		Iterator<FeatureSelectionOptional> itr = opts.iterator();
		FeatureSelectionOptional f = itr.next();
		FeatureSelectionOptional g = itr.next();

		assertTrue(f.setSelected(true).isEmpty());
		assertEquals(
			Stream.of(f).collect(Collectors.toSet()),
			g.getUnselectedFeaturesIfSelected()
		);
		assertEquals(
			Stream.of(f).collect(Collectors.toSet()),
			g.setSelected(true)
		);

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

	@Test
	public void featureReSelection() {
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
		Block block = selection.newAnonymousBlock("a");
		main.addLocalBlock(block);
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

		// Re-select features
		FeatureSelection selection2 = dt.featureSelection(block.anonymousDiagramType());
		selection2.getOptionalFeatures().iterator().next().setSelected(false);
		block.setType(selection2.newAnonymousBlock("").getType());
		p.flushAllAttributes();

		// No errors
		assertEquals("[]", dt.compUnit().errors().toString());

		expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    feature g;\n" +
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, main.prettyPrint());
	}
}
