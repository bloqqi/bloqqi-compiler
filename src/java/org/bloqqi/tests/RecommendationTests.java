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
		FeatureConfiguration conf = dt.specialize();

		for (OptionalFeature opt: conf.getOptionalFeatures()) {
			opt.setSelected(true);
		}
		
		DiagramType newDt = conf.newDiagramType("SubA");
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
		FeatureConfiguration conf = dt.specialize();

		assertEquals(1, conf.getOptionalFeatures().size());
		OptionalFeature opt = conf.getOptionalFeatures().first();
		OptionalFeatureAlternative[] alternatives = opt.getAlternatives().toArray(new OptionalFeatureAlternative[0]);
		assertEquals("S", alternatives[0].getType().name());
		assertEquals("T", alternatives[1].getType().name());
		
		opt.setSelected(true);
		opt.setSelectedAlternative(alternatives[0]);
		
		DiagramType newDt = conf.newDiagramType("SubA");
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
		
		assertEquals(1, dtA.specialize().getOptionalFeatures().size());
		assertEquals(1, dtB.specialize().getOptionalFeatures().size());
		assertEquals(1, dtC1.specialize().getOptionalFeatures().size());
		assertEquals(0, dtC2.specialize().getOptionalFeatures().size());
	}
	
	private Pair<Program, FeatureConfiguration> buildHierarchicalSpecialization() {
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

		FeatureConfiguration confDt = dtA.specialize();
		MandatoryFeatureAlternative alt = confDt.getMandatoryFeatures().first().getAlternatives().first();
		assertEquals("Block", alt.getType().name());
		
		FeatureConfiguration confBlock = alt.specialize();
		confBlock.getOptionalFeatures().first().setSelected(true);

		return new Pair<>(p, confDt);
	}
	
	@Test
	public void testHierarchicalSpecialization() {
		Pair<Program, FeatureConfiguration> pair = buildHierarchicalSpecialization();
		Program p = pair.first;
		FeatureConfiguration conf = pair.second;
		CompilationUnit cu = p.getCompilationUnit(0);
		
		// Create new specialization and add them to the program
		DiagramType newDt = conf.newDiagramType("SubA");
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
		Pair<Program, FeatureConfiguration> pair = buildHierarchicalSpecialization();
		Program p = pair.first;
		FeatureConfiguration conf = pair.second;
		CompilationUnit cu = p.getCompilationUnit(0);
		
		// Create new specialization and add them to the program
		Component comp = conf.newAnonymousComponent("a");
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

		FeatureConfiguration confA = dtA.specialize();
		for (OptionalFeature opt: confA.getOptionalFeatures()) {
			opt.setSelected(true);
		}
		
		Component comp = confA.newAnonymousComponent("a");
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

		FeatureConfiguration confA = dtA.specialize();
		Component comp = confA.newAnonymousComponent("a");
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
		FeatureConfiguration conf = dtA.specialize();
		assertEquals(1, conf.getOptionalFeatures().first().getAlternatives().size());
		assertEquals("T", conf.getOptionalFeatures().first().getAlternatives().first().getType().name());
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
		FeatureConfiguration conf = dtA.specialize();

		assertEquals(2, conf.getMandatoryFeatures().first().getAlternatives().size());
		Iterator<MandatoryFeatureAlternative> itr = conf.getMandatoryFeatures().first().getAlternatives().iterator();
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
		FeatureConfiguration conf = dtA.specialize();
		
		
		OptionalFeature opt = conf.getOptionalFeatures().first();
		opt.setSelected(true);
		Iterator<OptionalFeatureAlternative> itr = opt.getAlternatives().iterator();
		itr.next();
		itr.next();
		opt.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(conf.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    block: SubSubBlock[in: Int];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in2", "block.in3"));
		assertEquals(expectedSet, conf.getNewInParameters());
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
		FeatureConfiguration confA = dtA.specialize();
		
		OptionalFeature optA = confA.getOptionalFeatures().first();
		optA.setSelected(true);
		optA.setSelectedAlternative(optA.getAlternatives().first());
		
		FeatureConfiguration confCompB = optA.getSelectedAlternative().specialize();
		OptionalFeature optB = confCompB.getOptionalFeatures().first();
		optB.setSelected(true);
		Iterator<OptionalFeatureAlternative> itr = optB.getAlternatives().iterator();
		itr.next();
		itr.next();
		optB.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(confA.newAnonymousComponent("a"));
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
		assertEquals(expectedSet, confA.getNewInParameters());
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
		FeatureConfiguration confA = dtA.specialize();
		
		OptionalFeature optA = confA.getOptionalFeatures().first();
		optA.setSelected(true);
		optA.setSelectedAlternative(optA.getAlternatives().first());
		
		FeatureConfiguration confCompB = optA.getSelectedAlternative().specialize();
		OptionalFeature optB = confCompB.getOptionalFeatures().first();
		optB.setSelected(true);
		Iterator<OptionalFeatureAlternative> itr = optB.getAlternatives().iterator();
		itr.next();
		itr.next();
		optB.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(confA.newAnonymousComponent("a"));
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
		assertEquals(expectedSet, confA.getNewInParameters());
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
		FeatureConfiguration conf = dtA.specialize();
		MandatoryFeature m = conf.getMandatoryFeatures().first();
		Iterator<MandatoryFeatureAlternative> a = m.getAlternatives().iterator();
		a.next();
		a.next();
		m.setSelectedAlternative(a.next());
		
		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in", "block.in2"));
		assertEquals(expectedSet, conf.getNewInParameters());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(conf.newAnonymousComponent("a"));
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

		FeatureConfiguration confA = dtA.specialize();
		MandatoryFeature mandatoryA = confA.getMandatoryFeatures().first();
		mandatoryA.setSelectedAlternative(mandatoryA.getAlternatives().first());
		
		FeatureConfiguration confCompB = mandatoryA.getSelectedAlternative().specialize();
		MandatoryFeature mandatoryB = confCompB.getMandatoryFeatures().first();
		Iterator<MandatoryFeatureAlternative> itr = mandatoryB.getAlternatives().iterator();
		itr.next();
		itr.next();
		mandatoryB.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(confA.newAnonymousComponent("a"));
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
		assertEquals(expectedSet, confA.getNewInParameters());
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

		FeatureConfiguration confA = dtA.specialize();
		MandatoryFeature mandatoryA = confA.getMandatoryFeatures().first();
		mandatoryA.setSelectedAlternative(mandatoryA.getAlternatives().first());
		
		FeatureConfiguration confCompB = mandatoryA.getSelectedAlternative().specialize();
		OptionalFeature optB = confCompB.getOptionalFeatures().first();
		Iterator<OptionalFeatureAlternative> itr = optB.getAlternatives().iterator();
		itr.next();
		itr.next();
		optB.setSelected(true);
		optB.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(confA.newAnonymousComponent("a"));
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
		assertEquals(expectedSet, confA.getNewInParameters());
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
		FeatureConfiguration conf = dtA.specialize();
		
		OptionalFeature opt = conf.getOptionalFeatures().first();
		opt.setSelected(true);
		Iterator<OptionalFeatureAlternative> itr = opt.getAlternatives().iterator();
		itr.next();
		itr.next();
		opt.setSelectedAlternative(itr.next());
		
		DiagramType dtMain = (DiagramType) program.getCompilationUnit(0).typeDecls().get(0);
		dtMain.addLocalComponent(conf.newAnonymousComponent("a"));
		dtMain.flushAllAttributes();
		String expected =
			"diagramtype Main {\n" +
			"  a: A {\n" +
			"    block: SubSubBlock[in: Int];\n" +
			"  };\n" +
			"}\n";
		assertEquals(expected, dtMain.prettyPrint());

		Set<String> expectedSet = new HashSet<>(Arrays.asList("block.in2", "block.in3", "block.in4"));
		assertEquals(expectedSet, conf.getNewInParameters());
	}
}
