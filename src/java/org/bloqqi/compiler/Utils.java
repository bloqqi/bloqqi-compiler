package org.bloqqi.compiler;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class Utils {
	public static <T> List<T> filterToList(Collection<T> collection, Predicate<T> predicate) {
		return collection.stream().filter(predicate).collect(toList());
	}
}
