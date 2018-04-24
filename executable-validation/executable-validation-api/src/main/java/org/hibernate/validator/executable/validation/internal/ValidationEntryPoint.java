/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.executable.validation.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;

/**
 * @author Marko Bekhta
 */
public final class ValidationEntryPoint {

	private ValidationEntryPoint() {
	}

	private static ExecutableValidator validator;

	private static ExecutableValidator getValidator() {
		if ( validator == null ) {
			synchronized (ValidationEntryPoint.class) {
				if ( validator == null ) {
					validator = new ServiceLoaderBasedValidatorFactoryProducer()
							.getValidatorFactory()
							.getValidator()
							.forExecutables();
				}
			}
		}
		return validator;
	}

	/**
	 * @see ExecutableValidator#validateParameters(Object, Method, Object[], Class[])
	 */
	public static void validateParameters(
			Object object,
			Method method,
			Object[] parameterValues,
			Class<?>[] groups) {
		Set<ConstraintViolation<Object>> violations = getValidator().validateParameters(
				object,
				method,
				parameterValues,
				groups
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * @see ExecutableValidator#validateReturnValue(Object, Method, Object, Class[])
	 */
	public static void validateReturnValue(
			Object object,
			Method method,
			Object returnValue,
			Class<?>[] groups) {
		Set<ConstraintViolation<Object>> violations = getValidator().validateReturnValue(
				object,
				method,
				returnValue,
				groups
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * @see ExecutableValidator#validateConstructorParameters(Constructor, Object[], Class[])
	 */
	public static void validateConstructorParameters(
			Constructor<?> proxiedConstructor,
			Object[] parameterValues,
			Class<?>[] groups) {
		Constructor<?> constructor = findOriginalConstructor( proxiedConstructor );
		Set<ConstraintViolation<Object>> violations = getValidator().validateConstructorParameters(
				constructor,
				parameterValues,
				groups
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * @see ExecutableValidator#validateConstructorReturnValue(Constructor, Object, Class[])
	 */
	public static void validateConstructorReturnValue(
			Constructor<?> proxiedConstructor,
			Object createdObject,
			Class<?>[] groups) {
		Constructor<?> constructor = findOriginalConstructor( proxiedConstructor );
		Set<ConstraintViolation<Object>> violations = getValidator().validateConstructorReturnValue(
				constructor,
				createdObject,
				groups
		);

		// if there are any violations - throw a ConstraintViolationException
		if ( !violations.isEmpty() ) {
			throw new ConstraintViolationException( violations );
		}
	}

	/**
	 * @param constructor a constructor created by ByteBuddy.
	 *
	 * @return a {@link Constructor} from a superclass matching the created one.
	 */
	private static Constructor<?> findOriginalConstructor(Constructor<?> constructor) {
//		try {
//			return constructor.getDeclaringClass()
//					.getSuperclass()
//					.getDeclaredConstructor( constructor.getParameterTypes() );
//		}
//		catch (NoSuchMethodException e) {
//			throw new IllegalStateException( "Wasn't able to find constructor matching the proxied one." );
//		}
		return constructor;
	}
}
