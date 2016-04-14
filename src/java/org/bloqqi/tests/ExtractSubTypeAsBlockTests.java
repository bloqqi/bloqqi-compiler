package org.bloqqi.tests;

import static org.junit.Assert.*;


import org.junit.Test;
import org.bloqqi.compiler.ast.Component;
import org.bloqqi.compiler.ast.DiagramType;
import org.bloqqi.compiler.ast.Pair;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.compiler.ast.TypeUse;
import org.bloqqi.compiler.ast.WiredComponent;
import org.bloqqi.tests.testsuite.TestSuite;

public class ExtractSubTypeAsBlockTests extends TestSuite {
	@Test
	public void connection() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"}" +
			"diagramtype B extends A {" +
			"  connect(in, out);" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");
		
		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		
		String expectedDiagramType =
			"diagramtype BWrapper(in: Int => out: Int) {\n" +
			"  connect(in, out);\n" +
			"}\n" +
			"wiring BWrapper[in: Int, =>out: Int] {\n" +
			"  connect(in, BWrapper.in);\n" +
			"  connect(BWrapper.out, out);\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[in, out]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void connectionAndBlock() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"}" +
			"diagramtype B extends A {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"  connect(block.out, out);" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType =
			"diagramtype BWrapper(in: Int => out: Int) {\n" +
			"  block: Block;\n" +
			"  connect(in, block.in);\n" +
			"  connect(block.out, out);\n" +
			"}\n" +
			"wiring BWrapper[in: Int, =>out: Int] {\n" +
			"  connect(in, BWrapper.in);\n" +
			"  connect(BWrapper.out, out);\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[in, out]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}

	
	@Test
	public void connectionInterception() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  connect(in, out);" +
			"}" +
			"diagramtype B extends A {" +
			"  block: Block;" +
			"  intercept out with block.in, block.out;" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType =
			"diagramtype BWrapper(in: Int => out: Int) {\n" +
			"  block: Block;\n" +
			"  connect(in, block.in);\n" +
			"  connect(block.out, out);\n" +
			"}\n" +
			"wiring BWrapper[=>out: Int] {\n" +
			"  intercept out with BWrapper.in, BWrapper.out;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[out]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}

	
	@Test
	public void connectionInterception2() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"  connect(block.out, out);" +
			"}" +
			"diagramtype B extends A {" +
			"  block2: Block;" +
			"  intercept block.in with block2.in, block2.out;" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType =
			"diagramtype BWrapper(in: Int => blockin: Int) {\n" +
			"  block2: Block;\n" +
			"  connect(in, block2.in);\n" +
			"  connect(block2.out, blockin);\n" +
			"}\n" +
			"wiring BWrapper[=>blockin: Int] {\n" +
			"  intercept blockin with BWrapper.in, BWrapper.blockin;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[block.in]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void connectionInterceptionAndParameter() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"  connect(block.out, out);" +
			"}" +
			"diagramtype B(in2: Int => out2: Int) extends A {" +
			"  block2: Block;" +
			"  intercept out with block2.in, block2.out;" +
			"  connect(in2, out2);" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);
		
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
			"diagramtype BWrapper(in2: Int, blockout: Int => out2: Int, out: Int) {\n" +
			"  block2: Block;\n" +
			"  connect(in2, out2);\n" +
			"  connect(blockout, block2.in);\n" +
			"  connect(block2.out, out);\n" +
			"}\n" +
			"wiring BWrapper[in2: Int, =>out2: Int, =>out: Int] {\n" +
			"  connect(in2, BWrapper.in2);\n" +
			"  connect(BWrapper.out2, out2);\n" +
			"  intercept out with BWrapper.blockout, BWrapper.out;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[in2: Int, =>out2: Int, out]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void sourceInterception() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"  connect(block.out, out);" +
			"}" +
			"diagramtype B extends A {" +
			"  block2: Block;" +
			"  intercept source block.out with block2.in, block2.out;" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);
		
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		
		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");
		
		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
			"diagramtype BWrapper(blockout: Int => out: Int) {\n" +
			"  block2: Block;\n" +
			"  connect(blockout, block2.in);\n" +
			"  connect(block2.out, out);\n" +
			"}\n" +
			"wiring BWrapper[blockout: Int] {\n" +
			"  intercept source blockout with BWrapper.blockout, BWrapper.out;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[block.out]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}

	@Test
	public void twoIngoingConnections() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"}" +
			"diagramtype B extends A {" +
			"  block2: Block2;" +
			"  connect(block.out, block2.in1);" +
			"  connect(block.out, block2.in2);" +
			"  connect(block2.out, out);" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}" +
			"diagramtype Block2(in1: Int, in2: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType =
			"diagramtype BWrapper(blockout: Int => out: Int) {\n" +
			"  block2: Block2;\n" +
			"  connect(blockout, block2.in1);\n" +
			"  connect(blockout, block2.in2);\n" +
			"  connect(block2.out, out);\n" +
			"}\n" +
			"wiring BWrapper[blockout: Int, =>out: Int] {\n" +
			"  connect(blockout, BWrapper.blockout);\n" +
			"  connect(BWrapper.out, out);\n" +
			"}\n";

		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[block.out, out]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	/*
	@Test
	public void twoOutgoingConnections() {
		String str =
			"diagramtype A(in: Int => out: Int, out2: Int) {" +
			"}" +
			"diagramtype B extends A {" +
			"  connect(in, out);" +
			"  connect(in, out2);" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");
		
		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType =
			"diagramtype BWrapper(in: Int => out: Int, out2: Int) {\n" +
			"  connect(in, out);\n" +
			"  connect(in, out2);\n" +
			"}\n" +
			"wiring BWrapper[in: Int, =>out: Int, =>out2: Int] {\n" +
			"  connect(in, BWrapper.in);\n" +
			"  connect(BWrapper.out, out);\n" +
			"  connect(BWrapper.out2, out2);\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[in, out, out2]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	*/

	@Test
	public void twoOutgoingConnections2() {
		String str =
			"diagramtype A(in: Int => out: Int) {" +
			"  block: Block;" +
			"  connect(in, block.in);" +
			"  connect(block.out, out);" +
			"}" +
			"diagramtype B extends A {" +
			"  block2: Block2;" +
			"  intercept out with block2.in1, block2.out;" +
			"  connect(block.out, block2.in2);" +
			"}" +
			"diagramtype Block(in: Int => out: Int) {" +
			"}" +
			"diagramtype Block2(in1: Int, in2: Int => out: Int) {" +
			"}";
		Program program = parseValidProgram(str);

		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtB = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		Pair<DiagramType, WiredComponent> p = dtB.extractsubTypeAsWiredBlock("BWrapper", "bWrapper");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtA, p.second);
		program.flushAllAttributes();
		
		
		String expectedDiagramType =
			"diagramtype BWrapper(blockout: Int => out: Int) {\n" +
			"  block2: Block2;\n" +
			"  connect(blockout, block2.in2);\n" +
			"  connect(blockout, block2.in1);\n" +
			"  connect(block2.out, out);\n" +
			"}\n" +
			"wiring BWrapper[blockout: Int, =>out: Int] {\n" +
			"  connect(blockout, BWrapper.blockout);\n" +
			"  intercept out with BWrapper.blockout, BWrapper.out;\n" +
			"}\n";

		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "bWrapper: BWrapper[block.out, out]";
		assertEquals(expectedComponent, p.second.prettyPrint());

		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	
	@Test
	public void tankValve2() {
		String tank = tankModel();
		tank +=
			"diagramtype TankSecondFilling(fill2: Bool) extends Tank {" +
			"  valve2: Valve;" +
			"  Or_1;" +
			"  connect(Tank::Le_1.out, valve2.interlock);" +
			"  connect(fill2, valve2.open);" +
			"  intercept filling with Or_1.in1, Or_1.out;" +
			"  connect(valve2.status, Or_1.in2);" +
			"}";
		Program program = parseValidProgram(tank);
		
		DiagramType dtTank = (DiagramType) program.getCompilationUnit(0).localLookupType("Tank");
		DiagramType dtTwo = (DiagramType) program.getCompilationUnit(0).localLookupType("TankSecondFilling");

		Pair<DiagramType, WiredComponent> p = dtTwo.extractsubTypeAsWiredBlock("ValveExtension", "Valve2");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtTank, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
			"diagramtype ValveExtension(Le_1out: Bool, fill2: Bool, valvestatus: Bool => filling: Bool) {\n" +
			"  valve2: Valve;\n" +
			"  Or_1;\n" +
			"  connect(Le_1out, valve2.interlock);\n" +
			"  connect(fill2, valve2.open);\n" +
			"  connect(valve2.status, Or_1.in2);\n" +
			"  connect(valvestatus, Or_1.in1);\n" +
			"  connect(Or_1.out, filling);\n" +
			"}\n" +
			"wiring ValveExtension[Le_1out: Bool, fill2: Bool, =>filling: Bool] {\n" +
			"  connect(Le_1out, ValveExtension.Le_1out);\n" +
			"  connect(fill2, ValveExtension.fill2);\n" +
			"  intercept filling with ValveExtension.valvestatus, ValveExtension.filling;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "Valve2: ValveExtension[Le_1.out, fill2: Bool, filling]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void tankPump2() {
		String tank = tankModel();
		tank +=
			"diagramtype TankSecondEmptying(empty2: Bool) extends Tank {" +
			"  pump2: Pump;" +
			"  Or_2;" +
			"  intercept emptying with Or_2.in1, Or_2.out;" +
			"  connect(Tank::Le_2.out, pump2.interlock);" +
			"  connect(empty2, pump2.open);" +
			"  connect(pump2.status, Or_2.in2);" +
			"}";
		Program program = parseValidProgram(tank);
		
		DiagramType dtTank = (DiagramType) program.getCompilationUnit(0).localLookupType("Tank");
		DiagramType dtTwo = (DiagramType) program.getCompilationUnit(0).localLookupType("TankSecondEmptying");

		Pair<DiagramType, WiredComponent> p = dtTwo.extractsubTypeAsWiredBlock("PumpExtension", "Pump2");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtTank, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
				"diagramtype PumpExtension(Le_2out: Bool, empty2: Bool, pumpstatus: Bool => emptying: Bool) {\n" +
				"  pump2: Pump;\n" +
				"  Or_2;\n" +
				"  connect(Le_2out, pump2.interlock);\n" +
				"  connect(empty2, pump2.open);\n" +
				"  connect(pump2.status, Or_2.in2);\n" +
				"  connect(pumpstatus, Or_2.in1);\n" +
				"  connect(Or_2.out, emptying);\n" +
				"}\n" +
				"wiring PumpExtension[Le_2out: Bool, empty2: Bool, =>emptying: Bool] {\n" +
				"  connect(Le_2out, PumpExtension.Le_2out);\n" +
				"  connect(empty2, PumpExtension.empty2);\n" +
				"  intercept emptying with PumpExtension.pumpstatus, PumpExtension.emptying;\n" +
				"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "Pump2: PumpExtension[Le_2.out, empty2: Bool, emptying]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void tankHeating() {
		String tank = tankModel();
		tank +=
			"diagramtype TankHeating(heat: Bool => heating: Bool) extends Tank {" +
			"  minLevelHeating: OperatorInt;" +
			"  Le_3;" +
			"  maxTemp: OperatorInt;" +
			"  temp: SensorInt;" +
			"  Le_4;" +
			"  Or_1;" +
			"  heater: Valve;" +
			"  connect(Tank::level.out, Le_3.in1);" +
			"  connect(minLevelHeating.output, Le_3.in2);" +
			"  connect(maxTemp.output, Le_4.in1);" +
			"  connect(temp.out, Le_4.in2);" +
			"  connect(Le_3.out, Or_1.in1);" +
			"  connect(Le_4.out, Or_1.in2);" +
			"  connect(Or_1.out, heater.interlock);" +
			"  connect(heat, heater.open);" +
			"  connect(heater.status, heating);" +
			"}";
		Program program = parseValidProgram(tank);
		
		DiagramType dtTank = (DiagramType) program.getCompilationUnit(0).localLookupType("Tank");
		DiagramType dtTwo = (DiagramType) program.getCompilationUnit(0).localLookupType("TankHeating");

		Pair<DiagramType, WiredComponent> p = dtTwo.extractsubTypeAsWiredBlock("HeatingExtension", "Heating");

		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtTank, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
			"diagramtype HeatingExtension(levelout: Int, heat: Bool => heating: Bool) {\n" +
			"  minLevelHeating: OperatorInt;\n" +
			"  Le_3;\n" +
			"  maxTemp: OperatorInt;\n" +
			"  temp: SensorInt;\n" +
			"  Le_4;\n" +
			"  Or_1;\n" +
			"  heater: Valve;\n" +
			"  connect(levelout, Le_3.in1);\n" +
			"  connect(minLevelHeating.output, Le_3.in2);\n" +
			"  connect(maxTemp.output, Le_4.in1);\n" +
			"  connect(temp.out, Le_4.in2);\n" +
			"  connect(Le_3.out, Or_1.in1);\n" +
			"  connect(Le_4.out, Or_1.in2);\n" +
			"  connect(Or_1.out, heater.interlock);\n" +
			"  connect(heat, heater.open);\n" +
			"  connect(heater.status, heating);\n" +
			"}\n" +
			"wiring HeatingExtension[levelout: Int, heat: Bool, =>heating: Bool] {\n" +
			"  connect(levelout, HeatingExtension.levelout);\n" +
			"  connect(heat, HeatingExtension.heat);\n" +
			"  connect(HeatingExtension.heating, heating);\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "Heating: HeatingExtension[level.out, heat: Bool, =>heating: Bool]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	@Test
	public void tankAgitation() {
		String tank = tankModel();
		tank +=
			"diagramtype TankAgitation(agitate: Bool => agitating: Bool) extends Tank {\n" +
			"  minLevelAgitation: SensorInt;\n" +
			"  Le_3;\n" +
			"  agitator: Motor;\n" +
			"  Or_1;\n" +
			"  Or_2;\n" +
			"  connect(Tank::level.out, Le_3.in1);\n" +
			"  connect(minLevelAgitation.out, Le_3.in2);\n" +
			"  connect(agitator.status, agitating);\n" +
			"  connect(agitate, agitator.open);\n" +
			"  intercept Tank::pump.interlock with Or_1.in1, Or_1.out;\n" +
			"  connect(Or_2.out, agitator.interlock);\n" +
			"  connect(Le_3.out, Or_2.in2);\n" +
			"  connect(agitate, Or_1.in2);\n" +
			"  connect(empty, Or_2.in1);\n" +
			"}\n";
		Program program = parseValidProgram(tank);
		
		DiagramType dtTank = (DiagramType) program.getCompilationUnit(0).localLookupType("Tank");
		DiagramType dtTwo = (DiagramType) program.getCompilationUnit(0).localLookupType("TankAgitation");

		Pair<DiagramType, WiredComponent> p = dtTwo.extractsubTypeAsWiredBlock("AgitationExtension", "Agitation");
		
		program.getCompilationUnit(0).addDeclaration(p.first);
		createAndAddSubtypeWith(dtTank, p.second);
		program.flushAllAttributes();
		
		String expectedDiagramType = 
			"diagramtype AgitationExtension(levelout: Int, agitate: Bool, empty: Bool, Le_2out: Bool => agitating: Bool, pumpinterlock: Bool) {\n" +
			"  minLevelAgitation: SensorInt;\n" +
			"  Le_3;\n" +
			"  agitator: Motor;\n" +
			"  Or_1;\n" +
			"  Or_2;\n" +
			"  connect(levelout, Le_3.in1);\n" +
			"  connect(minLevelAgitation.out, Le_3.in2);\n" +
			"  connect(agitator.status, agitating);\n" +
			"  connect(agitate, agitator.open);\n" +
			"  connect(Or_2.out, agitator.interlock);\n" +
			"  connect(Le_3.out, Or_2.in2);\n" +
			"  connect(agitate, Or_1.in2);\n" +
			"  connect(empty, Or_2.in1);\n" +
			"  connect(Le_2out, Or_1.in1);\n" +
			"  connect(Or_1.out, pumpinterlock);\n" +
			"}\n" +
			"wiring AgitationExtension[levelout: Int, =>agitating: Bool, agitate: Bool, empty: Bool, =>pumpinterlock: Bool] {\n" +
			"  connect(levelout, AgitationExtension.levelout);\n" +
			"  connect(AgitationExtension.agitating, agitating);\n" +
			"  connect(agitate, AgitationExtension.agitate);\n" +
			"  connect(empty, AgitationExtension.empty);\n" +
			"  intercept pumpinterlock with AgitationExtension.Le_2out, AgitationExtension.pumpinterlock;\n" +
			"}\n";
		assertEquals(expectedDiagramType, p.first.prettyPrint());
		
		String expectedComponent = "Agitation: AgitationExtension[level.out, =>agitating: Bool, agitate: Bool, empty, pump.interlock]";
		assertEquals(expectedComponent, p.second.prettyPrint());
		
		assertEquals("[]", program.getCompilationUnit(0).errors().toString());
	}
	
	
	private String tankModel() {
		return
			"diagramtype Tank(fill: Bool, empty: Bool => filling: Bool, emptying: Bool) {" +
			"  level: SensorInt;" +
			"  valve: Valve;" +
			"  pump: Pump;" +
			"  maxLevel: OperatorInt;" +
			"  minLevel: OperatorInt;" +
			"  Le_1;" +
			"  Le_2;" +
			"  connect(valve.status, filling);" +
			"  connect(pump.status, emptying);" +
			"  connect(maxLevel.output, Le_1.in1);" +
			"  connect(level.out, Le_1.in2);" +
			"  connect(Le_1.out, valve.interlock);" +
			"  connect(fill, valve.open);" +
			"  connect(level.out, Le_2.in1);" +
			"  connect(minLevel.output, Le_2.in2);" +
			"  connect(Le_2.out, pump.interlock);" +
			"  connect(empty, pump.open);" +
			"}" +
			"diagramtype ProcessObject(open: Bool, interlock: Bool => status: Bool) {" +
			"  actuator: ActuatorBool;" +
			"  sensor: SensorBool;" +
			"  And_1;" +
			"  Not_1;" +
			"  connect(sensor.status, status);" +
			"  connect(open, And_1.in1);" +
			"  connect(interlock, Not_1.in);" +
			"  connect(Not_1.out, And_1.in2);" +
			"  connect(And_1.out, actuator.open);" +
			"}" +
			"diagramtype Valve extends ProcessObject {" +
			"}" +
			"diagramtype AdvancedValve extends Valve {" +
			"  sensor2: SensorBool;" +
			"  And_1;" +
			"  Not_1;" +
			"  connect(sensor2.status, Not_1.in);" +
			"  connect(Not_1.out, And_1.in2);" +
			"  intercept status with And_1.in1, And_1.out;" +
			"}" +
			"diagramtype Pump extends ProcessObject {" +
			"}" +
			"diagramtype Motor extends ProcessObject {" +
			"}" +
			"diagramtype SensorInt( => out: Int) {" +
			"}" +
			"diagramtype SensorBool( => status: Bool) {" +
			"}" +
			"diagramtype ActuatorBool(open: Bool) {" +
			"}" +
			"diagramtype OperatorInt( => output: Int) {" +
			"}" +
			"diagramtype OperatorBool( => out: Bool) {" +
			"}";
	}
	
	private DiagramType createAndAddSubtypeWith(DiagramType superType, Component component) {
		DiagramType dt = new DiagramType();
		dt.setID("Sub" + superType.name());
		dt.addSuperType(new TypeUse(superType.name()));
		dt.addLocalComponent(component);
		superType.compUnit().addDeclaration(dt);
		return dt;
	}
}
