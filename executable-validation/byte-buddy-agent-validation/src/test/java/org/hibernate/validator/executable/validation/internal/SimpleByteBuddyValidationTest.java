/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.executable.validation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import org.hibernate.validator.bytebuddy.agent.Groups;
import org.hibernate.validator.bytebuddy.agent.ValidationAgent;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.executable.validation.Validate;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class SimpleByteBuddyValidationTest {

	@Test(enabled = false)
	public void test() throws Exception {
		Class<? extends BankAccount> loaded = new ByteBuddy()
				.redefine( BankAccount.class )
				.visit( Advice
						.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
						.to( ValidationAgent.ConstructorParametersValidationAdvice.class )
						.on( ElementMatchers.isConstructor()
								.and( ElementMatchers.hasParameters( ElementMatchers.any() ) )
								.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
				)
				.visit( Advice
						.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
						.to( ValidationAgent.ConstructorReturnValueValidationAdvice.class )
						.on( ElementMatchers.isConstructor()
								.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
				)
				.visit( Advice
						.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
						.to( ValidationAgent.MethodParametersValidationAdvice.class )
						.on( ElementMatchers.isMethod()
								.and( ElementMatchers.hasParameters( ElementMatchers.any() ) )
								.and( ElementMatchers.isAnnotatedWith( Validate.class ) )
						)
				)
				.visit( Advice
						.withCustomMapping().bind( Groups.ForGroups.Factory.INSTANCE )
						.to( ValidationAgent.MethodReturnValueValidationAdvice.class )
						.on( ElementMatchers.isMethod()
								// visit only non void methods
								.and( ElementMatchers.returns( ElementMatchers.not( ElementMatchers.is( void.class ) ) ) )
								.and( ElementMatchers.isAnnotatedWith( Validate.class ) ) )
				)
				.make()
				.load( SimpleByteBuddyValidationTest.class.getClassLoader() )
				.getLoaded();

		Constructor<? extends BankAccount> constructor = loaded.getConstructor( int.class );

		// should fail as a negative amount is passed as parameter
		assertThatThrownBy( () -> constructor.newInstance( -1 ) )
				.hasCauseInstanceOf( ConstraintViolationException.class );

		// account with positive amount should pass validation
		BankAccount account = constructor.newInstance( 10 );

		// increasing the num should work fine if amount is |num| < 100
		assertThat( account.add( 10 ) ).isEqualTo( 20 );
		assertThat( account.add( -1 ) ).isEqualTo( 19 );

		//but it should fail if the result is < 0
		assertThatThrownBy( () -> account.add( -90 ) )
				.hasCauseInstanceOf( ConstraintViolationException.class );

		// and also in case when amount is |num| > 100
		assertThatThrownBy( () -> account.add( 200 ) )
				.hasCauseInstanceOf( ConstraintViolationException.class );
	}

	public static class BankAccount {

		private int num;

		@Validate
		public BankAccount(@Positive int num) {
			this.num = num;
		}

		@Validate
		@Min(0)
		public int add(@Range(min = -100, max = 100) int num) {
			if ( this.num + num > -1 ) {
				this.num += num;
				return this.num;
			}
			return this.num + num;
		}
	}
}
