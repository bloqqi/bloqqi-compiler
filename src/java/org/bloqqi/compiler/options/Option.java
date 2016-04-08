package org.bloqqi.compiler.options;

abstract public class Option<ValueType> implements Comparable<Option<?>> {
	public final static String PREFIX = "--";
	public static enum HasArgument {
		NO,
		OPTIONAL,
		YES
	}
	
	private final String name;
	private final String description;
	
	public Option(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public int compareTo(Option<?> o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Option) {
			return compareTo((Option<?>) other) == 0;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return PREFIX + name;
	}

	abstract public boolean isSet();
	abstract public ValueType getValue();
	abstract public HasArgument hasArgument();
	abstract public void match(String input) throws IllegalMatchException;
	
	public static class IllegalMatchException extends Exception {
		private static final long serialVersionUID = 1L;
		public IllegalMatchException(String message) {
			super(message);
		}
	}
}
