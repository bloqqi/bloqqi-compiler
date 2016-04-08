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
public class CodeGenerationErrors extends DynamicTestSuite {
	private final static String DIRECTORY_PATH = "code_generation_errors/";
	
	public CodeGenerationErrors(String filename) { super(filename); }
	@Test public void test() {
		String file = DIRECTORY_PATH + filename;
		Program p = parseValidProgramFile(file + ".dia");
		String expectedErrors = readTestFile(file + ".err");
		String actualErrors = getCodeGenerationErrors(p);
		assertEquals(expectedErrors.trim(), actualErrors.trim());
	}
	@Parameters(name = "{0}") public static Collection<Object[]> getFiles() { return getFiles(DIRECTORY_PATH); }
}
