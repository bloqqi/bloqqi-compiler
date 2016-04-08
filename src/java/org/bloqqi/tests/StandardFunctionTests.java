package org.bloqqi.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.tests.testsuite.TestSuite;

public class StandardFunctionTests extends TestSuite {
	@Test
	public void testStandardFunctions() {
		Program p = parseValidProgram("");
		assertEquals("[]", p.getStandardLibrary().errors().toString());
	}
	
	@Test
	public void useStandardFunction() {
		String s = 
				"diagramtype T() {\n" +
				"	Add_1;\n" +
				"   Sub_1;\n" +
				"	connect(1, Add_1.in1);\n" +
				"	connect(2, Add_1.in2);\n" +
				"	connect(Add_1, Sub_1.in1);\n" +
				"	connect(3, Sub_1.in2);\n" +
				"}\n";
		parseValidProgram(s);
	}
}
