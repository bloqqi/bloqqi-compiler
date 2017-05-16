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
		
		assertEquals(8, p.specialize().nbrOfCombinations());
		assertEquals(24, part.specialize().nbrOfCombinations());
		assertEquals(1800, loop.specialize().nbrOfCombinations());
	}
	
	@Test
	public void combinationsOnwardLoop() {
		Program program = parseValidProgramFile("complete/OnwardLoop.dia");
		DiagramType controller = (DiagramType) program.lookupType("Controller");
		DiagramType part = (DiagramType) program.lookupType("ControllerPart");
		DiagramType loop = (DiagramType) program.lookupType("Loop");
		
		assertEquals(4, controller.specialize().nbrOfCombinations());
		assertEquals(8, part.specialize().nbrOfCombinations());
		assertEquals(216, loop.specialize().nbrOfCombinations());
	}
	
	@Test
	public void combinationsOnwardTank() {
		Program program = parseValidProgramFile("complete/OnwardTank.dia");
		DiagramType valveExt = (DiagramType) program.lookupType("ValveExtension");
		DiagramType tank = (DiagramType) program.lookupType("Tank");
		
		assertEquals(2, valveExt.specialize().nbrOfCombinations());
		assertEquals(48, tank.specialize().nbrOfCombinations());
	}
	
	@Test
	public void combinationsRecursiveRecommendations() {
		Program program = parseValidProgramFile("complete/RecursiveRecommendations.dia");
		DiagramType loop = (DiagramType) program.lookupType("Loop");
		DiagramType oneMorePart = (DiagramType) program.lookupType("OneMorePart");
		
		assertEquals(-1, loop.specialize().nbrOfCombinations());
		assertEquals(-1, oneMorePart.specialize().nbrOfCombinations());
	}
}
