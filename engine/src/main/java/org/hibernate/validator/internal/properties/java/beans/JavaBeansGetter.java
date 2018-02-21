/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.java.beans;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * @author Marko Bekhta
 */
public class JavaBeansGetter implements Property, Callable {

	private static final Class[] PARAMETER_TYPES = new Class[0];

	private final Method method;
	private final String name;
	private final Type type;
	private final Type typeForValidatorResolution;

	public JavaBeansGetter(Method method) {
		this.method = getAccessible( method );
		this.name = ReflectionHelper.getPropertyName( method );
		this.type = ReflectionHelper.typeOf( method );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return method.getDeclaringClass();
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Object getValueFrom(Object bean) {
		return ReflectionHelper.getValue( method, bean );
	}

	@Override
	public Object getReturnValueFrom(Object bean) {
		return getValueFrom( bean );
	}

	@Override
	public boolean hasReturnValue() {
		// getters should always have a return value
		return true;
	}

	@Override
	public boolean hasParameters() {
		// getters should never have parameters
		return false;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return PARAMETER_TYPES;
	}

	@Override
	public Type[] getGenericParameterTypes() {
		return PARAMETER_TYPES;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeansGetter that = (JavaBeansGetter) o;

		if ( !this.method.equals( that.method ) ) {
			return false;
		}
		if ( !this.name.equals( that.name ) ) {
			return false;
		}
		if ( !this.type.equals( that.type ) ) {
			return false;
		}
		return this.typeForValidatorResolution.equals( that.typeForValidatorResolution );
	}

	@Override
	public int hashCode() {
		int result = this.method.hashCode();
		result = 31 * result + this.name.hashCode();
		result = 31 * result + this.type.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		return result;
	}

	/**
	 * Returns an accessible version of the given method. Will be the given method itself in case it is accessible,
	 * otherwise a copy which is set accessible.
	 */
	private static Method getAccessible(Method original) {
		if ( ( (AccessibleObject) original ).isAccessible() ) {
			return original;
		}

		SecurityManager sm = System.getSecurityManager();
		if ( sm != null ) {
			sm.checkPermission( HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS );
		}

		Class<?> clazz = original.getDeclaringClass();
		Method accessibleMethod = run( GetDeclaredMethod.andMakeAccessible( clazz, original.getName() ) );

		return accessibleMethod;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
