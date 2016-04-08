package org.bloqqi.tests.testsuite;

import org.junit.Test;

/**
 * Abstract class containing valid test cases.
 * 
 * A subclass can define how these test cases are tested,
 * for example, using pretty printing, by implementing the
 * method testFile(String).
 */
abstract public class AbstractValidTests extends TestSuite {
	@Test 
	public final void simpleDiagramType1() {
		testFile("SimpleDiagramType1");
	}
	
	@Test 
	public final void simpleDiagramType2() {
		testFile("SimpleDiagramType2");
	}

	@Test 
	public final void simpleDiagramType3() {
		testFile("SimpleDiagramType3");
	}

	@Test 
	public final void simpleDiagramType4() {
		testFile("SimpleDiagramType4");
	}

	@Test 
	public final void simpleDiagramType5() {
		testFile("SimpleDiagramType5");
	}
	
	@Test 
	public final void simpleDiagramType6() {
		testFile("SimpleDiagramType6");
	}
	@Test 
	public final void variableDiagramType1() {
		testFile("VariableDiagramType1");
	}
	abstract protected void testFile(String filename);
}
