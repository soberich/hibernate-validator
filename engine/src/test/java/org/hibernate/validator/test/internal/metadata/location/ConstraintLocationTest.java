/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.location;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;

import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.java.beans.JavaBeansGetter;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintLocationTest {

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_constraint_locations_for_the_same_type_should_be_equal() {
		ConstraintLocation location1 = ConstraintLocation.forClass( Foo.class );
		ConstraintLocation location2 = ConstraintLocation.forClass( Foo.class );

		assertEquals( location1, location2, "Two constraint locations for the same type should be equal" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_constraint_locations_for_the_same_member_should_be_equal() throws Exception {
		Method getter = Foo.class.getMethod( "getBar" );
		Property property = new JavaBeansGetter( getter );
		ConstraintLocation location1 = ConstraintLocation.forProperty( property );
		ConstraintLocation location2 = ConstraintLocation.forProperty( property );

		assertEquals( location1, location2, "Two constraint locations for the same type should be equal" );
	}

	public static class Foo {
		public String getBar() {
			return null;
		}
	}
}
