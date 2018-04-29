/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.propertyholder;

/**
 * Facet of a property holder constraint mapping creational context which allows to specify the
 * property to which the next operations should be apply.
 *
 * @author Marko Bekhta
 */
public interface PropertyHolderTarget {

	/**
	 * Defines a property, that is a property holder itself, to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on property holder level. After calling this method constraints
	 * apply on the specified property with the given property type.
	 * </p>
	 * <p>
	 * A given property may only be configured once.
	 * </p>
	 *
	 * @param property The property holder on which to apply the following constraints.
	 * @param mappingName The mapping name of the specified property holder.
	 *
	 * @return A creational context representing the selected property.
	 */
	PropertyConstraintMappingContext propertyHolder(String property, String mappingName);

}
