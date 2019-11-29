package org.bloqqi.tests.codegen;


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.bloqqi.compiler.ast.CodeTargetTesting;
import org.bloqqi.compiler.ast.Program;
import org.bloqqi.tests.testsuite.DynamicTestSuite;

@RunWith(Parameterized.class)
public class CodeGeneration extends DynamicTestSuite {
	private final static int ITERATIONS = 10;
	private final static String DIRECTORY_PATH = "code_generation/";
	private final static String PRINT_FUNCTION_PATH
		= "src/java/org/bloqqi/tests/codegen/PrintFunction.c";

	public CodeGeneration(String filename) {
		super(filename);
	}

	@Test
	public void test() throws IOException, InterruptedException {
		String file = DIRECTORY_PATH + filename;
		String cFile = TEST_FILES_PATH + file + ".c";
		String externalCFile = TEST_FILES_PATH + file + ".external";
		String executableFile = TEST_FILES_PATH + file + ".executable";
		String outFile = TEST_FILES_PATH + file + ".out";
		String expectedFile = TEST_FILES_PATH + file + ".expected";

		Program p = parseValidProgramFile(file + ".dia");
		assertEquals("Program in file " + file + " contains code generation errors",
				"",
				getCodeGenerationErrors(p));

		// Generate C code
		String cCode = "#define ITERATIONS " + ITERATIONS + "\n";
		cCode += p.generateC(new CodeTargetTesting(p));
		writeToFile(cFile, cCode);

		// Compile C code
		List<String> cmd = new ArrayList<>();
		cmd.add("gcc");
		cmd.add("-std=c99");
		cmd.add("-pedantic");
		cmd.add("-Werror");
		cmd.add(cFile);
		cmd.add(PRINT_FUNCTION_PATH);
		if (new File(externalCFile).exists()) {
			Files.copy(
					Paths.get(externalCFile),
					Paths.get(externalCFile + ".c"),
					StandardCopyOption.REPLACE_EXISTING);
			cmd.add(externalCFile + ".c");
		}
		cmd.add("-o");
		cmd.add(executableFile);
		execute(cmd);

		// Run program
		String out = execute(Arrays.asList(executableFile), 500);
		writeToFile(outFile, out);
		String expected = "";
		if (new File(expectedFile).exists()) {
			expected = readFile(expectedFile);
		}
		assertEquals(expected, out);
	}

	@Parameters(name = "{0}")
	public static Collection<Object[]> getFiles() {
		return getFiles(DIRECTORY_PATH);
	}
}
