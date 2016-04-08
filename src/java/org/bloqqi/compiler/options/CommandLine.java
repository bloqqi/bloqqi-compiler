package org.bloqqi.compiler.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class CommandLine {
	private final Collection<Option<?>> options;
	private final Map<String, Option<?>> mapping;
	private final List<String> arguments;
	
	public CommandLine(Collection<Option<?>> options) {
		this.options = options;
		this.mapping = new HashMap<>();
		for (Option<?> option: options) {
			mapping.put(option.getName(), option);
		}
		this.arguments = new ArrayList<>();
	}
	
	public void parse(String args[]) throws CommandLineException {
		int i = 0;
		while (i < args.length) {
			if (args[i].startsWith(Option.PREFIX)) {
				int argumentIndex = args[i].indexOf("=");
				String name;
				String argument = null;
				if (argumentIndex > 0) {
					name = args[i].substring(2, argumentIndex);
					argument = args[i].substring(argumentIndex+1);
				} else {
					name = args[i].substring(2);
				}
				Option<?> option = mapping.get(name);
				if (option == null) {
					throw new CommandLineException("Option " + Option.PREFIX + name + " not found");
				}
				match(option, argument);
			} else {
				arguments.add(args[i]);
			}
			i++;
		}
	}

	public void match(Option<?> option, String argument) throws CommandLineException {
		try {
			switch (option.hasArgument()) {
			case NO:
				if (argument == null) {
					option.match(null);
				} else {
					throw new CommandLineException("Option " + option + " is not allowed to have an argument");
				}
				break;
			case OPTIONAL:
				option.match(argument);
				break;
			case YES:
				if (argument != null) {
					option.match(argument);
				} else {
					throw new CommandLineException("Option " + option + " requires an argument");
				}
				break;
			}
		} catch (Option.IllegalMatchException e) {
			throw new CommandLineException("Invalid value for option " + option + ": " + e.getMessage());
		}
	}
	
	public List<String> getArguments() {
		return arguments;
	}
	
	public String printOptionHelp() {
		StringBuilder sb = new StringBuilder();
		int longestOption = 0;
		for (Option<?> option: options) {
			if (longestOption < option.getName().length()) {
				longestOption = option.getName().length();
			}
		}
		for (Option<?> option: new TreeSet<>(options)) {
			String s = String.format("  %s%-" + (longestOption+6) + "s %s%n", 
					Option.PREFIX, option.getName(), option.getDescription());
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static class CommandLineException extends Exception {
		private static final long serialVersionUID = 1L;
		public CommandLineException(String message) {
			super(message);
		}
	}
}
