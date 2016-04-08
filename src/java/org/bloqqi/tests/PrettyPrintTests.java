package org.bloqqi.tests;


import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.tests.testsuite.DynamicTestSuite;

@RunWith(Parameterized.class)
public class PrettyPrintTests extends DynamicTestSuite {
	private final static String DIRECTORY_PATH = "prettyprint/";
	
	public PrettyPrintTests(String filename) { super(filename); }
	@Test public void test() { 
		Program p = parseValidProgramFile(DIRECTORY_PATH + filename + ".dia");
		String expected = readTestFile(DIRECTORY_PATH + filename + ".dia");
		String pp = p.getCompilationUnit(0).prettyPrint().trim();
		assertEquals(expected.trim(), pp);
		
		parseValidProgram(pp);
	}
	@Parameters(name = "{0}") public static Collection<Object[]> getFiles() { return getFiles(DIRECTORY_PATH); }
}
