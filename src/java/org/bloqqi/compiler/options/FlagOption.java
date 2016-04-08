package org.bloqqi.compiler.options;

public class FlagOption extends Option<Boolean> {
	private boolean value;
	
	public FlagOption(String name, String description) {
		super(name, description);
		value = false;
	}

	@Override
	public Boolean getValue() {
		return value;
	}

	@Override
	public Option.HasArgument hasArgument() {
		return Option.HasArgument.NO;
	}
	
	@Override
	public void match(String string) throws IllegalMatchException {
		value = true;
	}

	@Override
	public boolean isSet() {
		return getValue();
	}
}
