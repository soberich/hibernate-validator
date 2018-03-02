/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutils;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocationBuilder;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public final class ConstraintLocationHelper {

	private static final ExecutableParameterNameProvider EXECUTABLE_PARAMETER_NAME_PROVIDER = new ExecutableParameterNameProvider( new DefaultParameterNameProvider() );

	private ConstraintLocationHelper() {
	}

	public static ConstraintLocation forClass(Class<?> declaringClass) {
		return ConstraintLocationBuilder.forClass( declaringClass ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forGetter(Method method) {
		return ConstraintLocationBuilder.forGetter( method ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forField(Field field) {
		return ConstraintLocationBuilder.forField( field ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forTypeArgument(ConstraintLocationBuilder delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		return ConstraintLocationBuilder.forTypeArgument( delegate, typeParameter, typeOfAnnotatedElement ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forReturnValue(Executable executable) {
		return ConstraintLocationBuilder.forReturnValue( executable ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forCrossParameter(Executable executable) {
		return ConstraintLocationBuilder.forCrossParameter( executable ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}

	public static ConstraintLocation forParameter(Executable executable, int index) {
		return ConstraintLocationBuilder.forParameter( executable, index ).getLocation( EXECUTABLE_PARAMETER_NAME_PROVIDER );
	}
}
