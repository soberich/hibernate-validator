/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.sample.client.validation;

import javax.validation.ConstraintViolationException;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;

/**
 * @author Marko Bekhta
 */
//CHECKSTYLE:OFF
public class BankOperationsRunner {

	@IgnoreForbiddenApisErrors(reason = "test")
	public static void main(String[] args) {
		BankAccount account = new BankAccount( 10 );


		try {
			account.add( -90 );
		}
		catch (ConstraintViolationException e) {
			e.printStackTrace();
		}

		try {
			account.add( 1000 );
		}
		catch (ConstraintViolationException e) {
			e.printStackTrace();
		}

		try {
			new BankAccount( -1 );
		}
		catch (ConstraintViolationException e) {
			e.printStackTrace();
		}
	}
}
//CHECKSTYLE:ON
