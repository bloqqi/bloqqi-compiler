package org.bloqqi.tests;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import org.bloqqi.compiler.ast.*;
import org.bloqqi.tests.testsuite.TestSuite;

public class RecommendationTests extends TestSuite {
	@Test
	public void testApplicationOrder() {
		String str = 
			"diagramtype A(Int in => Int out) {" +
			"	T at1;" +
			"	T at2;" +
			"	T at3;" +
			"	connect(in, at1);" +
			"	connect(in, at2);" +
			"	connect(in, at3);" +
			"	connect(in, out);" + 
			"}" +
			"recommendation A {" +
			"	T[out] t1;" +
			"	T[out] t2;" +
			"	T[out] t3;" +
			"	T[at1] b;" +
			"	T[at2] c;" +
			"	T[at3] a;" +
			"}" +
			"recommendation A {" +
			"	t3 before t2;" +
			"	t2 before t1;" +
			"}" +
			"diagramtype T(Int in => Int out) {" +
			"}" +
			"wiring T[=> Int in] {" + 
			"	intercept in with T, T;" +
			"}";

		DiagramType dt = parseValidDiagramType(str);
		SpecializeDiagramType s = dt.specialize();

		for (ConfComponentGroup g: s.getGroups()) {
			g.setSelected(true);
		}
		
		DiagramType newDt = s.newDiagramType("SubA");
		assertEquals("a", newDt.getLocalComponent(0).name());
		assertEquals("b", newDt.getLocalComponent(1).name());
		assertEquals("c", newDt.getLocalComponent(2).name());
		assertEquals("t3", newDt.getLocalComponent(3).name());
		assertEquals("t2", newDt.getLocalComponent(4).name());
		assertEquals("t1", newDt.getLocalComponent(5).name());
	}
	
	
	@Test
	public void testGrouping() {
		String str = 
			"diagramtype A(Int in, Int in2 => Int out, Int out2) {" +
			"	connect(in, out);" + 
			"}" +
			"recommendation A {" +
			"	T[out] block;" +
			"	S[out, out2] block;" +
			"}" +
			"diagramtype T(Int in => Int out) {" +
			"}" +
			"wiring T[=> Int in] {" + 
			"	intercept in with T, T;" +
			"}" +
			"diagramtype S(Int in2 => Int out2) extends T {" +
			"}" +
			"wiring S[=> Int in, =>Int in2] {" +
			"	intercept in with S.in, S.out;" +
			"	intercept in2 with S.in2, S.out2;" +
			"}";

		DiagramType dt = parseValidDiagramType(str);
		SpecializeDiagramType s = dt.specialize();

		assertEquals(1, s.getGroups().size());
		ConfComponentGroup g = s.getGroups().first();
		ConfComponent[] components = g.getRecommendations().toArray(new ConfComponent[0]);
		assertEquals("S", components[0].getType().name());
		assertEquals("T", components[1].getType().name());
		
		g.setSelected(true);
		g.setSelectedComponent(components[0]);
		
		DiagramType newDt = s.newDiagramType("SubA");
		dt.program().getCompilationUnit(0).addDeclaration(newDt);
		dt.program().flushAllAttributes();
		assertEquals("S", newDt.getLocalComponent(0).type().name());
	}

	@Test
	public void testRecommendationExtendsSuper() {
		String str =
			"diagramtype A(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"diagramtype B extends A { }" +
			"diagramtype C1 extends B { }" +
			"diagramtype C2 extends B { }" +
			"recommendation A {" +
			"	T[out] t;" +
			"}" +
			"recommendation B extends super;" +
			"recommendation C1 extends super;" +
			"diagramtype T(Int in => Int out) { }" +
			"wiring T[=> Int in] {" +
			"	intercept in with T, T;" +
			"}";
		
		Program p = parseValidProgram(str);
		CompilationUnit cu = p.getCompilationUnit(0);
		DiagramType dtA = (DiagramType) cu.typeDecls().get(0);
		DiagramType dtB = (DiagramType) cu.typeDecls().get(1);
		DiagramType dtC1 = (DiagramType) cu.typeDecls().get(2);
		DiagramType dtC2 = (DiagramType) cu.typeDecls().get(3);
		
		assertEquals(1, dtA.specialize().getGroups().size());
		assertEquals(1, dtB.specialize().getGroups().size());
		assertEquals(1, dtC1.specialize().getGroups().size());
		assertEquals(0, dtC2.specialize().getGroups().size());
	}
	
	private Pair<Program, SpecializeDiagramType> buildHierarchicalSpecialization() {
		String str = 
			"diagramtype Main {" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	Block block;" +
			"	connect(in, block);" +
			"	connect(block, out);" +
			"}" +
			"diagramtype Block(Int in => Int out) {" +
			"	InnerBlock innerBlock;" +
			"	connect(in, innerBlock);" +
			"	connect(innerBlock, out);" +
			"}" +
			"wiring Block[=>Int in] {" +
			"	intercept in with Block, Block;" +
			"}" +
			"diagramtype SubBlock() extends Block {" +
			"}" +
			"diagramtype InnerBlock(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"wiring InnerBlock[=>Int in] {" +
			"	intercept in with InnerBlock, InnerBlock;" +
			"}" +
			"diagramtype SubInnerBlock() extends InnerBlock {" +
			"}" +
			"recommendation A {" +
			"	replaceable block;" +
			"}" +
			"recommendation Block {" +
			"	InnerBlock[out] innerBlock2;" +
			"	replaceable innerBlock;" +
			"}";

		Program p = parseValidProgram(str);
		CompilationUnit cu = p.getCompilationUnit(0);
		DiagramType dtA = (DiagramType) cu.typeDecls().get(1);

		SpecializeDiagramType specDt = dtA.specialize();
		ConfReplaceableAlternative alt = specDt.getReplaceables().first().getAlternatives().first();
		assertEquals("Block", alt.getType().name());
		
		SpecializeDiagramType blockSpecDt = alt.specializeType();
		blockSpecDt.getGroups().first().setSelected(true);

		return new Pair<>(p, specDt);
	}
	
	@Test
	public void testHierarchicalSpecialization() {
		Pair<Program, SpecializeDiagramType> pair = buildHierarchicalSpecialization();
		Program p = pair.first;
		SpecializeDiagramType specDt = pair.second;
		CompilationUnit cu = p.getCompilationUnit(0);
		
		// Create new specialization and add them to the program
		DiagramType newDt = specDt.newDiagramType("SubA");
		cu.addDeclaration(newDt);
		p.flushAllAttributes();

		assertEquals("[]", cu.errors().toString());

		String expected = 
			"diagramtype SubA extends A {\n" +
			"  redeclare block: Block {\n" +
			"    innerBlock2: InnerBlock[out];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, newDt.prettyPrint());
	}
	@Test
	public void testAnonymousHierarchicalSpecialization() {
		Pair<Program, SpecializeDiagramType> pair = buildHierarchicalSpecialization();
		Program p = pair.first;
		SpecializeDiagramType specDt = pair.second;
		CompilationUnit cu = p.getCompilationUnit(0);
		
		// Create new specialization and add them to the program
		Component comp = specDt.newAnonymousComponent("a");
		DiagramType dtMain = (DiagramType) cu.getDeclaration(0);
		dtMain.addLocalComponent(comp);
		p.flushAllAttributes();

		// No errors
		assertEquals("[]", cu.errors().toString());

		// Expected anonymous type
		String expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    redeclare block: Block {\n" +
			"      innerBlock2: InnerBlock[out];\n" +
			"    };\n"+
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, dtMain.prettyPrint());
	}
	
	@Test
	public void testAnonymousType() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"recommendation A {" +
			"	T[out] t;" +
			"	T[out] t2;" +
			"	t2 before t;" +
			"}" +
			"diagramtype T(Int in => Int out) {" +
			"}" +
			"wiring T[=> Int in] {" +
			"	intercept in with T, T;" +
			"}";
		
		Program program = parseValidProgram(str);
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		SpecializeDiagramType specA = dtA.specialize();
		for (ConfComponentGroup group: specA.getGroups()) {
			group.setSelected(true);
		}
		
		Component comp = specA.newAnonymousComponent("a");
		dtMain.addLocalComponent(comp);
		program.flushAllAttributes();
		
		String expectedMain =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    t2: T[out];\n" +
			"    t: T[out];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expectedMain, dtMain.prettyPrint());
	}
	
	@Test
	public void testAnonymousTypeNoSpecializationMade() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"recommendation A {" +
			"	T[out] t;" +
			"	T[out] t2;" +
			"	t2 before t;" +
			"}" +
			"diagramtype T(Int in => Int out) {" +
			"}" +
			"wiring T[=> Int in] {" +
			"	intercept in with T, T;" +
			"}";
			
		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		SpecializeDiagramType specA = dtA.specialize();
		Component comp = specA.newAnonymousComponent("a");
		assertFalse(comp.hasAnonymousDiagramType());
	}

	@Test
	public void onlyIncludeNamedSubTypes() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A(Int in => Int out) {" +
			"	connect(in, out);" +
			"}" +
			"recommendation A {" +
			"	T[out] t;" +
			"}" +
			"diagramtype T(Int in => Int out) {" +
				"}" +
			"wiring T[=> Int in] {" +
			"	intercept in with T, T;" +
			"}" +
			"diagramtype EncloseAnonymousSubType {" +
			"	T { } t;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		// Do not include anonymous subtypes in alternatives
		SpecializeDiagramType specA = dtA.specialize();
		assertEquals(1, specA.getGroups().first().getRecommendations().size());
		assertEquals("T", specA.getGroups().first().getRecommendations().first().getType().name());
	}

	@Test
	public void testReplacables() {
		String str =
			"diagramtype A {" +
			"  block: Block;" +
			"}" +
			"diagramtype Block {" +
			"}" +
			"diagramtype SubBlock(in: Int) extends Block {" +
			"}" +
			"recommendation A {" +
			"  replaceable block;" +
			"}";
		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		SpecializeDiagramType specA = dtA.specialize();

		assertEquals(2, specA.getReplaceables().first().getAlternatives().size());
		Iterator<ConfReplaceableAlternative> itr = specA.getReplaceables().first().getAlternatives().iterator();
		assertEquals("Block", itr.next().getType().name());
		assertEquals("SubBlock", itr.next().getType().name());
	}
	
	@Test
	public void testNewInParameters1() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype Block(in: Int) {" +
			"}" +
			"wiring Block[Int v] {" +
			"  connect(v, Block.in);" +
			"}" +
			"diagramtype SubBlock(in2: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in3: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  block: Block[Int in];" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		SpecializeDiagramType specA = dtA.specialize();
		
		
		ConfComponentGroup group = specA.getGroups().first();
		group.setSelected(true);
		Iterator<ConfComponent> itr = group.getRecommendations().iterator();
		itr.next();
		itr.next();
		group.setSelectedComponent(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    block: SubSubBlock[in: Int];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in2", "block.in3"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
	
	@Test
	public void testNewInParameters2() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype Block(in: Int) {" +
			"}" +
			"diagramtype SubBlock(in2: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in3: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}" +
			"recommendation B {" +
			"  block: Block;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		SpecializeDiagramType specA = dtA.specialize();
		
		ConfComponentGroup groupA = specA.getGroups().first();
		groupA.setSelected(true);
		groupA.setSelectedComponent(groupA.getRecommendations().first());
		
		SpecializeDiagramType specCompB = groupA.getSelectedComponent().specializeType();
		ConfComponentGroup groupB = specCompB.getGroups().first();
		groupB.setSelected(true);
		Iterator<ConfComponent> itr = groupB.getRecommendations().iterator();
		itr.next();
		itr.next();
		groupB.setSelectedComponent(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    b: B {\n" +
			"      block: SubSubBlock;\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("b.block.in", "b.block.in2", "b.block.in3"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
	
	@Test
	public void testNewInParameters3() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype Block(in: Int) {" +
			"}" +
			"wiring Block[Int v] {" +
			"  connect(v, Block.in);" +
			"}" +
			"diagramtype SubBlock(in2: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in3: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  b: B;" +
			"}" +
			"recommendation B {" +
			"  block: Block[Int in];" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		SpecializeDiagramType specA = dtA.specialize();
		
		ConfComponentGroup groupA = specA.getGroups().first();
		groupA.setSelected(true);
		groupA.setSelectedComponent(groupA.getRecommendations().first());
		
		SpecializeDiagramType specCompB = groupA.getSelectedComponent().specializeType();
		ConfComponentGroup groupB = specCompB.getGroups().first();
		groupB.setSelected(true);
		Iterator<ConfComponent> itr = groupB.getRecommendations().iterator();
		itr.next();
		itr.next();
		groupB.setSelectedComponent(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    b: B {\n" +
			"      block: SubSubBlock[in: Int];\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("b.block.in2", "b.block.in3", "b.in"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
	
	@Test
	public void testNewInParameters4() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"  block: Block;" +
			"}" +
			"diagramtype Block {" +
			"}" +
			"diagramtype SubBlock(in: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in2: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  replaceable block;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		SpecializeDiagramType specA = dtA.specialize();
		ConfReplaceable c = specA.getReplaceables().first();
		Iterator<ConfReplaceableAlternative> a = c.getAlternatives().iterator();
		a.next();
		a.next();
		c.setSelectedAlternative(a.next());
		
		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in", "block.in2"));
		assertEquals(expectedSet, specA.getNewInParameters());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    redeclare block: SubSubBlock;\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());
	}
	
	@Test
	public void testNewInParameters5() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"  block: Block;" +
			"}" +
			"diagramtype Block {" +
			"}" +
			"diagramtype SubBlock(in: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in2: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"}" +
			"recommendation B {" +
			"  replaceable block;" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		SpecializeDiagramType specA = dtA.specialize();
		ConfReplaceable replA = specA.getReplaceables().first();
		replA.setSelectedAlternative(replA.getAlternatives().first());
		
		SpecializeDiagramType specCompB = replA.getSelectedAlternative().specializeType();
		ConfReplaceable replB = specCompB.getReplaceables().first();
		Iterator<ConfReplaceableAlternative> itr = replB.getAlternatives().iterator();
		itr.next();
		itr.next();
		replB.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    redeclare b: B {\n" +
			"      redeclare block: SubSubBlock;\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("b.block.in", "b.block.in2"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
	
	@Test
	public void testNewInParameters6() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"  b: B;" +
			"}" +
			"diagramtype B {" +
			"}" +
			"diagramtype Block(in: Int) {" +
			"}" +
			"wiring Block[Int v] {" +
			"  connect(v, Block.in);" +
			"}" +
			"diagramtype SubBlock(in2: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in3: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  replaceable b;" +
			"}" +
			"recommendation B {" +
			"  block: Block[in: Int];" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);

		SpecializeDiagramType specA = dtA.specialize();
		ConfReplaceable replA = specA.getReplaceables().first();
		replA.setSelectedAlternative(replA.getAlternatives().first());
		
		SpecializeDiagramType specCompB = replA.getSelectedAlternative().specializeType();
		ConfComponentGroup groupB = specCompB.getGroups().first();
		Iterator<ConfComponent> itr = groupB.getRecommendations().iterator();
		itr.next();
		itr.next();
		groupB.setSelected(true);
		groupB.setSelectedComponent(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    redeclare b: B {\n" +
			"      block: SubSubBlock[in: Int];\n" +
			"    };\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("b.in", "b.block.in2", "b.block.in3"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
	
	@Test
	public void testNewInParameter7() {
		String str =
			"diagramtype Main {" +
			"}" +
			"diagramtype A {" +
			"}" +
			"diagramtype Block(in: Int, in2: Int) {" +
			"}" +
			// Note: parameter in2 in Block is not connected by the wiring
			"wiring Block[Int v] {" +
			"  connect(v, Block.in);" +
			"}" +
			"diagramtype SubBlock(in3: Int) extends Block {" +
			"}" +
			"diagramtype SubSubBlock(in4: Int) extends SubBlock {" +
			"}" +
			"recommendation A {" +
			"  block: Block[Int in];" +
			"}";

		Program program = parseValidProgram(str);
		DiagramType dtA = (DiagramType) program.getCompilationUnit(0).typeDecls().get(1);
		SpecializeDiagramType specA = dtA.specialize();
		
		ConfComponentGroup group = specA.getGroups().first();
		group.setSelected(true);
		Iterator<ConfComponent> itr = group.getRecommendations().iterator();
		itr.next();
		itr.next();
		group.setSelectedComponent(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(specA.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    block: SubSubBlock[in: Int];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in2", "block.in3", "block.in4"));
		assertEquals(expectedSet, specA.getNewInParameters());
	}
}
