package org.bloqqi.tests;

import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.DiagramType;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.tests.testsuite.TestSuite;
import org.junit.Test;

public class CostEstimatesTests extends TestSuite {
	@Test
	public void combinationsPiiaLoop() {
		Program program = parseValidProgramFile("complete/PiiaLoop.dia");
		DiagramType p = (DiagramType) program.lookupType("P");
		DiagramType part = (DiagramType) program.lookupType("ControllerPart");
		DiagramType loop = (DiagramType) program.lookupType("Loop");
		
		assertEquals(p.specialize().nbrOfCombinations(), 8);
		assertEquals(part.specialize().nbrOfCombinations(), 24);
		assertEquals(loop.specialize().nbrOfCombinations(), 1800);
	}
	
	@Test
	public void combinationsOnwardLoop() {
		Program program = parseValidProgramFile("complete/OnwardLoop.dia");
		DiagramType controller = (DiagramType) program.lookupType("Controller");
		DiagramType part = (DiagramType) program.lookupType("ControllerPart");
		DiagramType loop = (DiagramType) program.lookupType("Loop");
		
		assertEquals(controller.specialize().nbrOfCombinations(), 4);
		assertEquals(part.specialize().nbrOfCombinations(), 8);
		assertEquals(loop.specialize().nbrOfCombinations(), 216);
	}
	
	@Test
	public void combinationsOnwardTank() {
		Program program = parseValidProgramFile("complete/OnwardTank.dia");
		DiagramType valveExt = (DiagramType) program.lookupType("ValveExtension");
		DiagramType tank = (DiagramType) program.lookupType("Tank");
		
		assertEquals(valveExt.specialize().nbrOfCombinations(), 2);
		assertEquals(tank.specialize().nbrOfCombinations(), 48);
	}
}
