package org.bloqqi.tests.testsuite;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.concurrent.TimeUnit;

import org.bloqqi.compiler.ast.*;

abstract public class TestSuite {
	protected static final String TEST_FILES_PATH = "testfiles/";
	protected static final int DEFAULT_EXECUTION_TIMEOUT = 3000; // In ms

	protected DiagramType parseValidDiagramType(String s) {
		Program p = parseValidProgram(s);
		DiagramType dt = (DiagramType) p.getCompilationUnit(0).typeDecls().get(0);
		return dt;
	}


	/**
	 * Parse a program and return the corresponding AST.
	 * The test will fail if the program contains semantic errors.
	 */
	protected static Program parseValidProgram(String program) {
		Program p = parseProgram(program);
		assertEquals("Program contains errors", "", getErrors(p));
		return p;
	}

	/**
	 * Parse a program given a filename and return the corresponding AST.
	 * The test will fail if the program contains semantic errors.
	 */
	protected static Program parseValidProgramFile(String filename) {
		return parseValidProgramFile(new File(TEST_FILES_PATH, filename));
	}

	/**
	 * Parse a program given a file object and return the corresponding AST.
	 * The test will fail if the program contains semantic errors.
	 */
	protected static Program parseValidProgramFile(File file) {
		Program p = parseProgramFile(file);
		assertEquals("Program in file " + file.getAbsolutePath() + " contains errors", "", getErrors(p));
		return p;
	}


	/**
	 * Parse a program given a filename and compare the errors
	 * with expected errors that are read from an error file.
	 */
	protected static void checkErrors(String filename) {
		Program p = parseProgramFile(filename + ".dia");
		String expectedErrors = readTestFile(filename + ".err");
		String actualErrors = getErrors(p);
		assertEquals(expectedErrors.trim(), actualErrors.trim());
	}

	protected static void checkSyntaxErrors(String filename) {
		String actualSyntaxError = "";
		try {
			parseProgramFile(filename + ".dia");
		} catch (BloqqiScanner.ScannerError e) {
			actualSyntaxError = e.getMessage();
		}
		String expectedSyntaxError = readTestFile(filename + ".err");
		assertEquals(expectedSyntaxError.trim(), actualSyntaxError.trim());
	}

	/**
	 * Parse a program given a filename and return the corresponding AST.
	 */
	protected static Program parseProgramFile(String filename) {
		return parseProgramFile(new File(TEST_FILES_PATH, filename));
	}

	/**
	 * Parse a program given a filename and return the corresponding AST.
	 */
	protected static Program parseProgramFile(File file) {
		try {
			Reader r = new FileReader(file);
			return parseProgram(r, "Error when parsing the file " + file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
			return null;
		}
	}


	/**
	 * Parse a program and return the corresponding AST.
	 */
	protected static Program parseProgram(String program) {
		Reader r = new StringReader(program);
		return parseProgram(r, "Error when parsing string " + program);
	}

	/**
	 * Parse a program and return the corresponding AST.
	 */
	protected static Program parseProgram(Reader reader, String errorMessage) {
		PrintStream err = System.err;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);

		try {
			System.setErr(ps);
			StringReader stdLibReader = new StringReader(Utils.readResource("/bloqqi/StandardFunctions.dia"));
			CompilationUnit stdlib = parse(stdLibReader);
			if (os.size() > 0) {
				fail("Parser recovery:\n" + os.toString());
			}

			CompilationUnit cu = parse(reader);
			if (os.size() > 0) {
				// The parser should not recover from anything, such errors
				// should not be put in a test case (otherwise it should be
				// stated explicitly).
				fail("Parser recovery:\n" + os.toString());
			}
			Program p = new Program();
			p.setStandardLibrary(stdlib);
			p.addCompilationUnit(cu);
			return p;
		} catch (Exception e) {
			fail(errorMessage + ":\n " + e.getMessage() + "\n" + os.toString());
			return null; // This line is required to remove compile errors...
		} finally {
			System.setErr(err);
		}
	}

	protected static String getErrors(Program p) {
		StringBuilder sb = new StringBuilder();
		for (CompilationUnit cu: p.getCompilationUnits()) {
			for (ErrorMessage e: cu.errors()) {
				sb.append(e + "\n");
			}
		}
		return sb.toString();
	}

	protected static String getCodeGenerationErrors(Program p) {
		StringBuilder sb = new StringBuilder();
		for (ErrorMessage e: p.mainDiagramErrors()) {
			sb.append(e + "\n");
		}
		for (CompilationUnit cu: p.getCompilationUnits()) {
			for (ErrorMessage e: cu.codeGenerationErrors()) {
				sb.append(e + "\n");
			}
		}
		return sb.toString();
	}

	//-----------------------------------------------------------
	//
	// Helper methods
	//
	//-----------------------------------------------------------

	protected static String readTestFile(String filename) {
		return readFile(TEST_FILES_PATH + filename);
	}
	protected static String readFile(String filename) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			File file = new File(filename);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
			  sb.append(line).append("\n");
			}
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			if (br != null) {
				{ try { br.close();} catch (IOException ignored) { } }
			}
		}
		return sb.toString();
	}

	protected static void writeToFile(String filename, String content) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename), "utf-8"))) {
			writer.write(content);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	protected static String execute(java.util.List<String> cmd) throws IOException, InterruptedException {
		return execute(cmd, DEFAULT_EXECUTION_TIMEOUT);
	}
	protected static String execute(java.util.List<String> cmd, int timeoutMillis) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		process.getOutputStream().close();

		if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
			process.destroy();
			fail("Command " + cmd + " could not complete within " + timeoutMillis + " ms");
		}

		String standardError = inputStreamToString(process.getErrorStream());
		assertEquals("Standard error was not empty", "", standardError);
		assertEquals("Exit code was not zero => error occured", 0, process.exitValue());

		return inputStreamToString(process.getInputStream());
	}

	protected static String inputStreamToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return sb.toString();
	}

	private static CompilationUnit parse(Reader reader) throws IOException,
			beaver.Parser.Exception {
		BloqqiScanner scanner = new BloqqiScanner(reader);
		BloqqiParser parser = new BloqqiParser();
		return (CompilationUnit) parser.parse(scanner);
	}

}
