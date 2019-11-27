package org.bloqqi.compiler;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import org.bloqqi.compiler.ast.Program;
import org.bloqqi.compiler.ast.DiagramType;

public class DistributedCGenerator {
	private final Program program;
	private final String jsonFile;

	private Config conf;
	private DiagramType diagramType;

	public DistributedCGenerator(Program program, String jsonFile) {
		this.program = program;
		this.jsonFile = jsonFile;
	}

	/**
	 * Parses JSON config and returns false if there are errors
	 */
	public boolean readAndCheckConfig() {
		try {
			String json = new String(Files.readAllBytes(Paths.get(jsonFile)));
			Gson gson = new Gson();
			Config conf = gson.fromJson(json, Config.class);

			Set<String> structuralErrors = conf.structuralErrors();
			if (!structuralErrors.isEmpty()) {
				System.out.println("Errors in '" + jsonFile + "':");
				for (String e: structuralErrors) {
					System.out.println("- " + e);
				}
				return false;
			}



			return true;
		} catch (IOException e) {
			System.out.println("Couldn't read JSON file " + jsonFile);
			return false;
		} catch (com.google.gson.JsonSyntaxException e) {
			System.out.println("Syntax error in '" + jsonFile + "'");
			return false;
		}

	}

	public void generate() {
		System.out.println("Let's generate code!");
	}

	public class Config {
		private String type;
		private double frequency;
		private List<Input> inputs;
		private List<Output> outputs;

		public Set<String> structuralErrors() {
			Set<String> errors = new TreeSet<>();
			if (type == null) {
				errors.add("Missing field 'type'");
			}
			if (frequency <= 0) {
				errors.add("Field 'frequency' needs to be > 0");
			}
			return errors;
		}

		public String getType() {
			return type;
		}
		public double getFrequency() {
			return frequency;
		}
		public List<Input> getInputs() {
			return inputs;
		}
		public List<Output> getOutputs() {
			return outputs;
		}

		public String toString() {
			return
				"type: " + type + "\n" +
				"frequency: " + frequency + "\n" +
				"inputs: " + inputs + "\n" +
				"outputs: " + outputs + "\n";
		}
	}

	public static class Input {
		private String input;
		private String signal;

		public String getInput() {
			return input;
		}
		public String getSignal() {
			return signal;
		}

		private void findStructuralErrors(Set<String> errors) {
			if (input == null || signal == null) {
				errors.add("Inputs need to have both an 'input' and 'signal' field");
			}
		}

		public String toString() {
			return "(input: " + input + ", signal: " + signal + ")";
		}
	}

	public static class Output {
		private String output;
		private String signal;

		public String getOutput() {
			return output;
		}
		public String getSignal() {
			return signal;
		}

		private void findStructuralErrors(Set<String> errors) {
			if (output == null || signal == null) {
				errors.add("Outputs need to have both an 'output' and 'signal' field");
			}
		}

		public String toString() {
			return "(output: " + output + ", signal: " + signal + ")";
		}
	}
}
