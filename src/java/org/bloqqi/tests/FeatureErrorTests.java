package org.bloqqi.tests;


import java.util.Collection;

import org.bloqqi.tests.testsuite.DynamicTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FeatureErrorTests extends DynamicTestSuite {
	private final static String DIRECTORY_PATH = "feature_errors/";

	public FeatureErrorTests(String filename) { super(filename); }
	@Test public void test() { checkErrors(DIRECTORY_PATH + filename); }
	@Parameters(name = "{0}") public static Collection<Object[]> getFiles() { return getFiles(DIRECTORY_PATH); }
}
