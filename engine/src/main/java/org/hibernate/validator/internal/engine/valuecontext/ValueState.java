/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.PathImpl;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ValueState<V> {

	private final PathImpl propertyPath;

	private final V currentValue;

	ValueState(PathImpl propertyPath, V currentValue) {
		this.propertyPath = propertyPath;
		this.currentValue = currentValue;
	}

	public PathImpl getPropertyPath() {
		return this.propertyPath;
	}

	public V getCurrentValue() {
		return this.currentValue;
	}
}
