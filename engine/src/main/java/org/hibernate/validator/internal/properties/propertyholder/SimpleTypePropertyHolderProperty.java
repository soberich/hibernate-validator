/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Describes a simple type property.
 *
 * @author Marko Bekhta
 */
public class SimpleTypePropertyHolderProperty implements Property {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String name;
	private final Class<?> type;

	public SimpleTypePropertyHolderProperty(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	@Override public Object getValueFrom(Object bean) {
		if ( !( bean instanceof PropertyHolder ) ) {
			throw LOG.getCannotConvertToPropertyHolderException( bean.getClass() );
		}
		return bean;
	}

	@Override public String getPropertyName() {
		return getName();
	}

	@Override public String getName() {
		return name;
	}

	@Override public Class<?> getDeclaringClass() {
		// Not sure about this one. What would we really want to use here...
		return PropertyHolder.class;
	}

	@Override public Type getTypeForValidatorResolution() {
		return getType();
	}

	@Override public Type getType() {
		return type;
	}

	@Override public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		SimpleTypePropertyHolderProperty that = (SimpleTypePropertyHolderProperty) o;

		if ( !name.equals( that.name ) ) {
			return false;
		}
		return type.equals( that.type );
	}

	@Override public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}
}
