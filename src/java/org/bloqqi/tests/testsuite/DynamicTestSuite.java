package org.bloqqi.tests.testsuite;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DynamicTestSuite extends TestSuite {
	public static Collection<Object[]> getFiles(String testDirectory) {
		Collection<Object[]> params = new ArrayList<Object[]>();
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".dia");
			}
		};
		for (File f : new File(TEST_FILES_PATH, testDirectory).listFiles(filter)) {
			String name = f.getName();
			name = name.substring(0, name.length() - 4);
			params.add(new Object[] { name });
		}
		return params;
	}

	protected String filename;

	public DynamicTestSuite(String filename) {
		this.filename = filename;
	}
}
