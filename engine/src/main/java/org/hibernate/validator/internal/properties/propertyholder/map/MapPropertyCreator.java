/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder.map;

import java.util.Map;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.spi.propertyholder.PropertyCreator;

/**
 * @author Marko Bekhta
 */
public class MapPropertyCreator implements PropertyCreator<Map> {
	@Override
	public Class<Map> getPropertyHolderType() {
		return Map.class;
	}

	@Override
	public Property create(String propertyName, Class<?> propertyType) {
		return new MapProperty( propertyName, propertyType );
	}

}
