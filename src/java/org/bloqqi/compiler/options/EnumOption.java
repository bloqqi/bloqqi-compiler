package org.bloqqi.compiler.options;

import java.util.Collection;
import java.util.TreeSet;

public class EnumOption extends Option<String> {
	private final TreeSet<String> allowedValues;
	private final String defaultValue;
	private String value;
	private boolean isSet;

	public EnumOption(String name, String description, Collection<String> allowedValues, String defaultValue) {
		super(name, description);
		this.allowedValues = new TreeSet<>(allowedValues);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.isSet = false;
	}

	public boolean addAllowedValue(String allowedValue) {
		return allowedValues.add(allowedValue);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Option.HasArgument hasArgument() {
		return Option.HasArgument.OPTIONAL;
	}
	
	@Override
	public void match(String argument) throws IllegalMatchException {
		if (argument == null) {
			isSet = true;
			value = defaultValue;
		} else if (allowedValues.contains(argument)) {
			isSet = true;
			value = argument;
		} else {
			throw new IllegalMatchException(argument 
					+ " is not allowed, allowed values are " + allowedValues);
		}
	}
	
	@Override
	public boolean isSet() {
		return isSet;
	}

	@Override
	public String getDescription() {
		String allowedValuesStr = allowedValues.toString();
		allowedValuesStr = allowedValuesStr.substring(1);
		allowedValuesStr = allowedValuesStr.substring(0, allowedValuesStr.length()-1);
		return super.getDescription() + " (allowed values: " + allowedValuesStr + ")";
	}
}
