package org.bloqqi.compiler;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.bloqqi.compiler.ast.Program;
import org.bloqqi.compiler.ast.DiagramType;
import org.bloqqi.compiler.ast.TypeDecl;
import org.bloqqi.compiler.ast.Variable;
import org.bloqqi.compiler.ast.CodeTargetDist;

public class DistributedCGenerator {
	private final Program program;
	private final String jsonFile;

	private DistConfig conf;
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
			conf = gson.fromJson(json, DistConfig.class);

			Set<String> structuralErrors = conf.structuralAnalysis();
			if (!structuralErrors.isEmpty()) {
				printErrors(structuralErrors);
				return false;
			}

			TypeDecl td = program.lookupType(conf.getType());
			if (td == null || !td.isDiagramType()) {
				System.out.println(conf.getType() + " is not a diagram type");
				return false;
			}
			diagramType = (DiagramType) td;

			Set<String> semanticalErrors = conf.semanticAnalysis(diagramType);
			if (!semanticalErrors.isEmpty()) {
				printErrors(semanticalErrors);
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

	private void printErrors(Set<String> errors) {
		System.out.println("Errors in '" + jsonFile + "':");
		for (String e: errors) {
			System.out.println("- " + e);
		}
	}

	public void generate(String outputFile) {
		CodeTargetDist target = new CodeTargetDist(program, conf);
		Compiler.generateCSeparateFiles(program, outputFile, target);
	}

	public static class DistConfig {
		private String type;
		private double frequency;
		private List<ConfInput> inputs;
		private List<ConfOutput> outputs;

		// Computed during semantic analysis
		private transient DiagramType diagramType;

		public Set<String> structuralAnalysis() {
			Set<String> errors = new TreeSet<>();
			if (type == null) {
				errors.add("Missing field 'type'");
			}
			if (frequency <= 0) {
				errors.add("Field 'frequency' needs to be > 0");
			}
			if (inputs != null) {
				for (ConfInput in: inputs) {
					if (in == null) {
						errors.add("Empty input (an extra comma?)");
					} else {
						in.structuralAnalysis(errors);
					}
				}
			}
			if (outputs != null) {
				for (ConfOutput out: outputs) {
					if (out == null) {
						errors.add("Empty output (an extra comma?)");
					} else {
						out.structuralAnalysis(errors);
					}
				}
			}
			return errors;
		}

		/** The structural analysis is run before semantic analysis */
		public Set<String> semanticAnalysis(DiagramType dt) {
			this.diagramType = dt;
			Set<String> errors = new TreeSet<>();

			if (inputs != null) {
				for (ConfInput in: inputs) {
					in.semanticAnalysis(dt, errors);
				}
			}
			if (outputs != null) {
				for (ConfOutput out: outputs) {
					out.semanticAnalysis(dt, errors);
				}
			}

			Set<String> inputVars = new TreeSet<>();
			if (inputs != null) {
				for (ConfInput in: inputs) {
					inputVars.add(in.getInput());
				}
			}
			for (String inVar: dt.allInputVariables().keySet()) {
				if (!inputVars.contains(inVar)) {
					errors.add("Input variable '" + inVar + "' is not referenced");
				}
			}

			return errors;
		}

		public String getType() {
			return type;
		}
		public double getFrequency() {
			return frequency;
		}
		public List<ConfInput> getInputs() {
			return inputs;
		}
		public List<ConfOutput> getOutputs() {
			return outputs;
		}

		public DiagramType getDiagramType() {
			return diagramType;
		}

		public String toString() {
			return
				"type: " + type + "\n" +
				"frequency: " + frequency + "\n" +
				"inputs: " + inputs + "\n" +
				"outputs: " + outputs + "\n";
		}
	}

	public static class ConfVariable {
		protected String signal;

		// Computed during semantic checking
		protected transient Variable variable;

		public String getSignal() {
			return signal;
		}

		public Variable getVariable() {
			return variable;
		}
	}

	public static class ConfInput extends ConfVariable {
		private String input;
		@SerializedName("default")
		private String defaultValue;
		private double ttl;
		private boolean tunableParameter;

		public String getInput() {
			return input;
		}
		public String getDefault() {
			return defaultValue;
		}
		public double getTtl() {
			return ttl;
		}
		public boolean isTunableParameter() {
			return tunableParameter;
		}

		private void structuralAnalysis(Set<String> errors) {
			if (input == null || signal == null) {
				errors.add("Inputs need to have both an 'input' and 'signal' field");
			}
			if (ttl > 0 && defaultValue == null) {
				errors.add("Inputs with 'ttl' > 0 require field 'default'");
			}
			if (tunableParameter && defaultValue == null) {
				errors.add("Inputs with 'tunableParameter' = true require field 'default'");
			}
			if (tunableParameter && ttl > 0) {
				errors.add("Inputs with 'tunableParameter' = true cannot have 'ttl'");
			}
		}

		private void semanticAnalysis(DiagramType dt, Set<String> errors) {
			Variable v = dt.allInputVariables().get(input);
			if (v == null) {
				errors.add("Couldn't find input variable '" + input + "'");
				return;
			} else {
				variable = v;
			}

			if (!v.type().isReal() && !v.type().isInteger() && !v.type().isBool()) {
				errors.add("Type error for input: " + toString() + ". Unsupported type '" + v.type()
					+ "'. Supported types are: Real, Int, Bool.");
			}
		}

		public String toString() {
			return "(input: " + input + ", signal: " + signal + ")";
		}
	}

	public static class ConfOutput extends ConfVariable {
		private String output;

		public String getOutput() {
			return output;
		}

		private void structuralAnalysis(Set<String> errors) {
			if (output == null || signal == null) {
				errors.add("Outputs need to have both an 'output' and 'signal' field");
			}
		}

		private void semanticAnalysis(DiagramType dt, Set<String> errors) {
			Variable v = dt.allOutputVariables().get(output);
			if (v == null) {
				errors.add("Couldn't find output variable '" + output + "'");
				return;
			} else {
				variable = v;
			}

			if (!v.type().isReal() && !v.type().isInteger() && !v.type().isBool()) {
				errors.add("Type error for output: " + toString() + ". Unsupported type '" + v.type()
					+ "'. Supported types are: Real, Int, Bool.");
			}
		}

		public String toString() {
			return "(output: " + output + ", signal: " + signal + ")";
		}
	}
}
