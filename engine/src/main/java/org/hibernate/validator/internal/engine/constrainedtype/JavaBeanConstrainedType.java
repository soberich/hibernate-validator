/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constrainedtype;

import org.hibernate.validator.engine.HibernateConstrainedType;

/**
 * An implementation of {@link HibernateConstrainedType} for regular JavaBeans.
 * Wrapps a {@link Class} object to adapt it to the needs of HV usages.
 *
 * @author Marko Bekhta
 */
public class JavaBeanConstrainedType<T> implements HibernateConstrainedType<T> {

	private final Class<T> clazz;

	public JavaBeanConstrainedType(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Class<T> getActuallClass() {
		return clazz;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		JavaBeanConstrainedType<?> that = (JavaBeanConstrainedType<?>) o;

		if ( !clazz.equals( that.clazz ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return clazz.hashCode();
	}
}
