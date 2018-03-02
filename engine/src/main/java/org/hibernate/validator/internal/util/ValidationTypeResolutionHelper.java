/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Marko Bekhta
 */
public final class ValidationTypeResolutionHelper {

	private ValidationTypeResolutionHelper() {
	}

	public static Type getTypeForValidatorResolution(Class<?> declaringClass) {
		// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator
		// resolution (HF)
		return declaringClass.getTypeParameters().length == 0 ?
				declaringClass :
				TypeHelper.parameterizedType( declaringClass, declaringClass.getTypeParameters() );
	}

	public static Type getTypeForValidatorResolution(Field field) {
		return ReflectionHelper.boxedType( ReflectionHelper.typeOf( field ) );
	}

	public static Type getTypeForValidatorResolution(Method method) {
		return ReflectionHelper.boxedType( ReflectionHelper.typeOf( method ) );
	}

	public static Type getTypeForValidatorResolution(Executable executable, int index) {
		return ReflectionHelper.boxedType( ReflectionHelper.typeOf( executable, index ) );
	}

	public static Type getTypeForValidatorResolution(Executable executable) {
		return ReflectionHelper.boxedType( ReflectionHelper.typeOf( executable ) );
	}
}
