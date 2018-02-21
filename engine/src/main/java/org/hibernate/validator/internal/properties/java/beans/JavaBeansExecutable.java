/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.java.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBeansExecutable implements Callable {

	private final Executable executable;
	private final Type typeForValidatorResolution;
	private final String name;
	private final boolean hasParameters;
	private final boolean hasReturnValue;
	private final Type type;

	public JavaBeansExecutable(Executable executable) {
		this.executable = executable;
		this.name = executable.getName();
		this.type = ReflectionHelper.typeOf( executable );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
		this.hasParameters = executable.getParameterTypes().length > 0;
		this.hasReturnValue = hasReturnValue( executable );
	}

	@Override
	public Object getReturnValueFrom(Object object) {
		return object;
	}

	@Override
	public boolean hasReturnValue() {
		return hasReturnValue;
	}

	@Override
	public boolean hasParameters() {
		return hasParameters;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
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
	public Class<?>[] getParameterTypes() {
		return executable.getParameterTypes();
	}

	@Override
	public Type[] getGenericParameterTypes() {
		return executable.getGenericParameterTypes();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeansExecutable that = (JavaBeansExecutable) o;

		if ( this.hasParameters != that.hasParameters ) {
			return false;
		}
		if ( this.hasReturnValue != that.hasReturnValue ) {
			return false;
		}
		if ( !this.executable.equals( that.executable ) ) {
			return false;
		}
		if ( !this.typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}
		if ( !this.name.equals( that.name ) ) {
			return false;
		}
		return this.type.equals( that.type );
	}

	@Override
	public int hashCode() {
		int result = this.executable.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		result = 31 * result + this.name.hashCode();
		result = 31 * result + ( this.hasParameters ? 1 : 0 );
		result = 31 * result + ( this.hasReturnValue ? 1 : 0 );
		result = 31 * result + this.type.hashCode();
		return result;
	}

	private boolean hasReturnValue(Executable executable) {
		if ( executable instanceof Constructor ) {
			return true;
		}
		else {
			return ( (Method) executable ).getGenericReturnType() != void.class;
		}
	}
}
