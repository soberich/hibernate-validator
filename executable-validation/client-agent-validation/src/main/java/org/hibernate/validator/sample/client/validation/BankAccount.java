/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.sample.client.validation;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.executable.validation.Validate;

/**
 * @author Marko Bekhta
 */
public class BankAccount {
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
