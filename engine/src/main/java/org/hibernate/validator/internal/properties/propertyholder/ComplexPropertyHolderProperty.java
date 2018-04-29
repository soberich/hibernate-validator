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
 * Describes a property of property holder that is a property holder itself.
 *
 * @author Marko Bekhta
 */
public class ComplexPropertyHolderProperty implements Property {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String name;
	private final String mappingName;

	public ComplexPropertyHolderProperty(String name, String mappingName) {
		this.name = name;
		this.mappingName = mappingName;
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
		return PropertyHolder.class;
	}

	@Override public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ComplexPropertyHolderProperty that = (ComplexPropertyHolderProperty) o;

		if ( !name.equals( that.name ) ) {
			return false;
		}
		return mappingName.equals( that.mappingName );
	}

	@Override public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + mappingName.hashCode();
		return result;
	}
}
