/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.java.beans;

import java.util.stream.Stream;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.PropertySelector;

/**
 * @author Marko Bekhta
 */
public class JavaBeanPropertySelector implements PropertySelector<JavaBean> {

	@Override
	public Stream<Property> getProperties(JavaBean type) {
		return Stream.concat( type.getFieldProperties(), type.getGetterProperties() );
	}
}
