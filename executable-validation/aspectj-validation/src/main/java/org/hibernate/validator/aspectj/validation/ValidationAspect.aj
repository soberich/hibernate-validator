/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation;

import javax.validation.Validator;

import org.hibernate.validator.executable.validation.Validate;
import org.hibernate.validator.executable.validation.internal.ServiceLoaderBasedValidatorFactoryProducer;
import org.hibernate.validator.executable.validation.internal.ValidationEntryPoint;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * An aspect that contains all the pointcuts and validation advices for method/constructor
 * validation. Uses {@link ServiceLoaderBasedValidatorFactoryProducer} to get an instance
 * of {@link Validator}.
 *
 * @author Marko Bekhta
 */
@Aspect
public aspect ValidationAspect {

	/**
	 * Defines a pointcut that looks for {@code @Validate} annotated elements. Used to build up advices.
	 *
	 * @param validate a reference to the annotation
	 */
	pointcut annotationPointCutDefinition(Validate validate): @annotation(validate);

	/**
	 * Defines a pointcut that looks for any method executions. Used to build up advices.
	 */
	pointcut atMethodExecution(): execution(* *(..));

	/**
	 * Defines a pointcut that looks for any constructor executions. Used to build up advices.
	 * @param jp a joint point of constructor execution
	 */
	pointcut atConstructorExecution(): execution(*.new(..));

	/**
	 * Defines an advice for validation of method parameters
	 */
	before(Validate validate): annotationPointCutDefinition(validate) && atMethodExecution() {
		ValidationEntryPoint.validateParameters(
				thisJoinPoint.getTarget(),
				( (MethodSignature) thisJoinPoint.getSignature() ).getMethod(),
				thisJoinPoint.getArgs(),
				validate.groups()
		);
	}

	/**
	 * Defines an advice for validation of method return value
	 */
	after(Validate validate) returning(Object returnValue): annotationPointCutDefinition(validate) && atMethodExecution() {
		ValidationEntryPoint.validateReturnValue(
				thisJoinPoint.getTarget(),
				( (MethodSignature) thisJoinPoint.getSignature() ).getMethod(),
				returnValue,
				validate.groups()
		);
	}

	/**
	 * Defines an advice for validation of constructor parameters
	 */
	before(Validate validate): annotationPointCutDefinition(validate) && atConstructorExecution() {
		ValidationEntryPoint.validateConstructorParameters(
				( (ConstructorSignature) thisJoinPoint.getSignature() ).getConstructor(),
				thisJoinPoint.getArgs(),
				validate.groups()
		);
	}


	/**
	 * Defines an advice for validation of method return value
	 */
	after(Validate validate): annotationPointCutDefinition(validate) && atConstructorExecution() {
		ValidationEntryPoint.validateConstructorReturnValue(
				( (ConstructorSignature) thisJoinPoint.getSignature() ).getConstructor(),
				thisJoinPoint.getTarget(),
				validate.groups()
		);
	}

}
