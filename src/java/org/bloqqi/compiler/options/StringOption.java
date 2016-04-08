package org.bloqqi.compiler.options;

public class StringOption extends Option<String> {
	private String value;
	private boolean isSet;
	
	public StringOption(String name, String description) {
		this(name, description, "");
	}
	
	public StringOption(String name, String description, String defaultValue) {
		super(name, description);
		value = defaultValue;
		isSet = false;
	}

	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public Option.HasArgument hasArgument() {
		return Option.HasArgument.YES;
	}
	
	@Override
	public void match(String value) throws IllegalMatchException {
		this.value = value;
		isSet = true;
	}

	@Override
	public boolean isSet() {
		return isSet;
	}
}
