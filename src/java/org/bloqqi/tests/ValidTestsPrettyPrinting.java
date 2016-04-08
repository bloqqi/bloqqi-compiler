package org.bloqqi.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.tests.testsuite.AbstractValidTests;

public class ValidTestsPrettyPrinting extends AbstractValidTests {
	@Test 
	public final void qualifiedSuperTypeVarUse() {
		testFile("QualifiedSuperTypeVarUse");
	}
	
	@Test 
	public final void localComponentRequireNoQualification() {
		testFile("LocalComponentRequireNoQualification");
	}
	
	@Test
	public final void twoDiagramTypes() {
		testFile("TwoDiagramTypes");
	}
	
	@Test
	public final void structVariableDiagramType1() {
		testFile("StructVariableDiagramType1");
	}
	
	@Test
	public final void recommendationGroupingAndBefore() {
		testFile("GroupingAndBefore");
	}
	
	//-----------------------------------------------------------
	// Helper methods
	//-----------------------------------------------------------
	@Override
	protected void testFile(String filename) {
		String file = "valid/" + filename + ".dia";
		String content = readTestFile(file);
		
		Program p = parseValidProgram(content);
		String pPrettyPrinted = p.getCompilationUnit(0).prettyPrint();
		assertEquals(content.trim(), pPrettyPrinted.trim());
		
		Program p2 = parseValidProgram(pPrettyPrinted);
		String p2PrettyPrinted = p2.getCompilationUnit(0).prettyPrint();
		
		assertEquals(pPrettyPrinted.trim(), p2PrettyPrinted.trim());
	}
}
