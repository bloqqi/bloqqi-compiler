package org.bloqqi.tests;


import static org.junit.Assert.*;

import org.junit.Test;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class ValidTests extends TestSuite {
	@Test
	public void inline() {
		String str = 
			"diagramtype Main(Int in => Int out) {" +
			"	inline Block_1;" +
			"	connect(in, Block_1.in);" +
			"	connect(Block_1, out);" +
			"}" +
			"diagramtype Block(Int in => Int out) {" +
			"	Add_1;" +
			"	connect(in, Add_1.in1);" +
			"	connect(Add_1, out);" +
			"}";
		DiagramType dt = parseValidDiagramType(str);
		
		Block c = dt.blocks().getChild(0);
		Connection c1 = dt.connections().getChild(0);
		Connection c2 = dt.connections().getChild(1);
		
		assertEquals("Add", c.type().name());
		assertEquals("Block_1$Add_1", c.name());
		
		assertSame(c, c1.getTarget().node());
		assertSame(c, c2.getSource().node());
	}
	
	@Test
	public void rewrireBug() {
		parseValidProgramFile("valid/RewriteCacheBug.dia");
	}
}
