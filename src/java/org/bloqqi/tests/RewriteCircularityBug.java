package org.bloqqi.tests;

import org.bloqqi.compiler.Compiler;
import org.bloqqi.compiler.ast.ASTNode;

public class RewriteCircularityBug {
	/**
	 * Compiler bug related to rewrites found by Anton's testing tool.
	 */

	private static ASTNode<?> reparse() throws Exception {
		final String[] args = new String[]{
				"testfiles/code_generation/AbstractDiagramType.dia",
				"testfiles/code_generation/Features.dia",
		};
		return (ASTNode<?>) Compiler.CodeProber_parse(args);
	}

	public static void main(String[] args) throws Exception {
		/**
		 * The following code will throw an exception.
		 * Output:
		 * Stepping child 0/2 in Program
		 * Stepping child 1/2 in List
		 * Stepping child 1/2 in CompilationUnit
		 * Stepping child 4/8 in List
		 * Stepping child 8/11 in DiagramType
		 * Stepping child 1/2 in List
		 * Stepping child 0/2 in Connection
		 * Exception in thread "main" java.lang.NullPointerException: Cannot invoke "org.bloqqi.compiler.ast.Port.name()" because "p" is null
		 *      at org.bloqqi.compiler.ast.ParseSimpleVarUse.rewriteRule0(ParseSimpleVarUse.java:244)
		 *      at org.bloqqi.compiler.ast.ParseSimpleVarUse.rewriteTo(ParseSimpleVarUse.java:228)
		 *      at org.bloqqi.compiler.ast.ParseSimpleVarUse.rewrittenNode(ParseSimpleVarUse.java:289)
		 *      at org.bloqqi.compiler.ast.ASTNode.getChild(ASTNode.java:642)
		 *      at org.bloqqi.tests.AntonTest.main(AntonTest.java:20)
		 *
		 * The bug is related to rewrites of simple names. If a block has one input/out parameter, then
		 * connections don't need to name the port (parameter), because it's implicit (since there is only one).
		 *
		 * In this particular test, two files declare the same type name (A),
		 * which gives rise to a null pointer exception. The issue is on line 27 in Features.dia,
		 * where "a" is accessed and rewritten. The access should be rewritten to "a.out".
		 * Running the same two files as input to the Bloqqi compiler, then there are no issues.
		 *
		 * After some digging, it seems like the bug is related to rewrites and circular attributes.
		 * If circularity checking is enabled (--visitcheck=true in build.xml), then an exception will be
		 * thrown for this test case. Thus, there is some circularity issue related to simplified names
		 * and Bloqqi "features" (Features.dia tests Bloqqi features).
		 * With circularity check enabled, then it's enough to only have Features.dia in this test,
		 * and adjusting the second child index below from 1 to 0, i.e., { 0, 0, 1, 4, 8, 1, 0 }.
		 * No other test cases throw an circularity exception with this flag enabled.
		 *
		 * This test can't be a normal JUnit test with the provided JastAdd version,
		 * because an exception is thrown during circular evaluation and the state isn't restored
		 * (static values I assume).
		 */
		ASTNode<?> node = reparse();
		for (int idx : new int[]{ 0, 1, 1, 4, 8, 1, 0 }) {
			System.out.println("Stepping child " + idx + "/" + node.getNumChild() + " in " + node.getClass().getSimpleName());
			node = node.getChild(idx);
		}
	}
}