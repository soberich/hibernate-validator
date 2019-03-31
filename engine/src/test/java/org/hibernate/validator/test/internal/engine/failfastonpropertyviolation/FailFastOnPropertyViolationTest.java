/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.failfastonpropertyviolation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests for fail fast on property violation mode.
 *
 * @author Marko Bekhta
 */
@TestForIssue(jiraKey = "HV-1328")
public class FailFastOnPropertyViolationTest {

	@Test
	public void testDefaultBehavior() {
		final Validator validator = getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( 1 ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class ),
				violationOf( Min.class ),
				violationOf( ClassAlwaysFail.class )
		);
	}

	@Test
	public void testFailFastOnPropertyViolation() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		configuration.failFastOnPropertyViolation( true );

		final Validator validator = configuration.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( 1 ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class ),
				violationOf( Min.class )
		);
	}

	@Test
	public void testFailFastOnPropertyViolationUsingProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST_ON_PROPERTY_VIOLATION, Boolean.TRUE.toString() );

		final Validator validator = configuration.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( 1 ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class ),
				violationOf( Min.class )
		);
	}


	@ClassAlwaysFail
	private static class Foo {
		@Min(10)
		private final int num;

		private Foo(int num) {
			this.num = num;
		}

		@NotBlank
		public String getText() {
			return "";
		}
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { FooConstraintValidator.class })
	public @interface ClassAlwaysFail {
		String message() default "alway fail";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class FooConstraintValidator implements ConstraintValidator<ClassAlwaysFail, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
