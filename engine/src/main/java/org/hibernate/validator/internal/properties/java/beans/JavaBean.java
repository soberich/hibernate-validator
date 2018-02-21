/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.java.beans;

import java.util.Arrays;
import java.util.stream.Stream;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.Type;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBean implements Type {

	private final Class<?> clazz;

	public JavaBean(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Stream<Property> getFieldProperties() {
		return Arrays.stream( clazz.getDeclaredFields() )
				.map( JavaBeansField::new );
	}

	public Stream<Property> getGetterProperties() {
		return Arrays.stream( clazz.getDeclaredMethods() )
				.filter( ReflectionHelper::isGetterMethod )
				.map( JavaBeansGetter::new );
	}
}
