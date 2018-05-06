/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.propertyholder;

import org.hibernate.validator.internal.properties.Property;

/**
 * This interface expose the functionality of converting a string representation
 * of a property to a {@link Property}
 *
 * @author Marko Bekhta
 */
public interface PropertyCreator<T> {

	/**
	 * @return a type of a property holder this creator can be applied to.
	 */
	Class<T> getPropertyHolderType();

	/**
	 * Creates property for a given name and type.
	 *
	 * @param propertyName property name
	 * @param propertyType property type
	 *
	 * @return created property
	 */
	Property create(String propertyName, Class<?> propertyType);
}
