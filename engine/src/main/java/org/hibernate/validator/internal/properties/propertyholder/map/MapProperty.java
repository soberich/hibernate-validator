/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder.map;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Map;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Marko Bekhta
 */
public class MapProperty implements Property {
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String name;
	private final Class<?> type;

	public MapProperty(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public Object getValueFrom(Object bean) {
		if ( !( bean instanceof Map ) ) {
			throw LOG.getUnexpextedPropertyHolderTypeException( Map.class, bean.getClass() );
		}
		Object value = ( (Map) bean ).get( name );
		if ( value != null && !type.isAssignableFrom( value.getClass() ) ) {
			throw LOG.getUnexpextedPropertyTypeInPropertyHolderException( type, value.getClass(), name );
		}
		return value;
	}

	@Override
	public String getPropertyName() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return Map.class;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return getType();
	}

	@Override
	public Type getType() {
		return type;
	}
}
