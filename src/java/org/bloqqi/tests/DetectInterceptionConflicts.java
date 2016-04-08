package org.bloqqi.tests;


import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.DynamicTestSuite;

@RunWith(Parameterized.class)
public class DetectInterceptionConflicts extends DynamicTestSuite {
	private final static String DIRECTORY_PATH = "detect_interception_conflicts/";
	
	public DetectInterceptionConflicts(String filename) { super(filename); }
	@Test public void test() { 
		Program p = parseValidProgramFile(DIRECTORY_PATH + filename + ".dia");
		String expected = readTestFile(DIRECTORY_PATH + filename + ".expected");
		
		StringBuilder actual = new StringBuilder();
		for (TypeDecl td: p.getCompilationUnit(0).typeDecls()) {
			if (td.isDiagramType()) {
				DiagramType dt = (DiagramType) td;
				actual.append(dt.name() + " = " + dt.interceptionConflicts() + "\n");
			}
		}
		assertEquals(expected.trim(), actual.toString().trim());
	}
	@Parameters(name = "{0}") public static Collection<Object[]> getFiles() { return getFiles(DIRECTORY_PATH); }
}
