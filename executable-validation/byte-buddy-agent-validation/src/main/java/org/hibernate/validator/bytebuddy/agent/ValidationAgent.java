/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.bytebuddy.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.hibernate.validator.executable.validation.Validate;
import org.hibernate.validator.executable.validation.internal.ValidationEntryPoint;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author Marko Bekhta
 */
//CHECKSTYLE:OFF HideUtilityClassConstructor
public class ValidationAgent {

	public static void premain(final String agentArgs, final Instrumentation instrumentation) {
		new AgentBuilder.Default()
				.type( ElementMatchers.any() )
				.transform( (builder, typeDescription, classLoader, module) -> builder
						.visit( Advice
								.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
								.to( ConstructorParametersValidationAdvice.class )
								.on( ElementMatchers.isConstructor()
										.and( ElementMatchers.hasParameters( ElementMatchers.any() ) )
										.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
						)
						.visit( Advice
								.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
								.to( ConstructorReturnValueValidationAdvice.class )
								.on( ElementMatchers.isConstructor()
										.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
						)
						.visit( Advice
								.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
								.to( MethodParametersValidationAdvice.class )
								.on( ElementMatchers.isMethod()
										.and( ElementMatchers.hasParameters( ElementMatchers.any() ) )
										.and( ElementMatchers.isAnnotatedWith( Validate.class ) )
								)
						)
						.visit( Advice
								.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
								.to( MethodReturnValueValidationAdvice.class )
								.on( ElementMatchers.isMethod()
										// visit only non void methods
										.and( ElementMatchers.returns( ElementMatchers.not( ElementMatchers.is( void.class ) ) ) )
										.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
						)
				).installOn( instrumentation );
	}
//CHECKSTYLE:ON HideUtilityClassConstructor

	public static class ConstructorParametersValidationAdvice {

		@Advice.OnMethodEnter
		private static void exit(
				@Advice.AllArguments Object[] parameters,
				@Advice.Origin Constructor<?> constructor,
				@Groups Class<?>[] groups) {
			ValidationEntryPoint.validateConstructorParameters( constructor, parameters, groups );
		}
	}

	public static class ConstructorReturnValueValidationAdvice {

		@Advice.OnMethodExit
		private static void exit(
				@Advice.This Object currentBean,
				@Advice.Origin Constructor<?> constructor,
				@Groups Class<?>[] groups) {
			ValidationEntryPoint.validateConstructorReturnValue( constructor, currentBean, groups );
		}
	}

	public static class MethodParametersValidationAdvice {

		@Advice.OnMethodEnter
		private static void exit(
				@Advice.This Object currentBean,
				@Advice.Origin Method method,
				@Advice.AllArguments Object[] parameters,
				@Groups Class<?>[] groups) {
			ValidationEntryPoint.validateParameters( currentBean, method, parameters, groups );
		}
	}

	public static class MethodReturnValueValidationAdvice {

		@Advice.OnMethodExit
		private static void exit(
				@Advice.This Object currentBean,
				@Advice.Origin Method method,
				@Advice.Return Object returnValue,
				@Groups Class<?>[] groups) {
			ValidationEntryPoint.validateReturnValue( currentBean, method, returnValue, groups );
		}
	}
}
