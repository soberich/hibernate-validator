package org.hibernate.validator.test.internal.metadata.aggregated;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.test.internal.metadata.aggregated.HierarchyWithBadConstraints.WebServiceImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestCase {

	private static WebServiceImpl ws;
	private static Method method;
	private static Object[] params;

	@BeforeClass
	public static void setUp()
			throws NoSuchMethodException {
		ws = new WebServiceImpl();
		method = WebServiceImpl.class.getMethod(
				"getEntityVersion", Long.class );
		params = new Object[] { null };
	}

	@Test
	@TestForIssue(jiraKey = "HV-1450")
	public void testHV1450() {
		for ( int i = 0; i < 1; i++ ) {
			Validator validator = Validation.byDefaultProvider().configure()
					.buildValidatorFactory().getValidator();

			Set<ConstraintViolation<WebServiceImpl>> violations =
					validator.forExecutables().validateParameters( ws, method, params );

			assertThat( violations ).containsOnlyViolations( violationOf( NotNull.class ) );
		}
	}

	@Test
	public void testName() throws Exception {
		ExecutableHelper helper = new ExecutableHelper( new TypeResolutionHelper() );

		Method foo = Inter2.class.getMethod( "foo", Integer.class );
		Method foo1 = Inter1.class.getMethod( "foo", int.class );

		assertFalse( helper.overrides(
				foo,
				foo1
		) );
		assertFalse( helper.overrides(
				foo1,
				foo
		) );
		assertFalse( helper.same(
				foo,
				foo1
		) );
		assertTrue( helper.same(
				foo1,
				foo
		) );


		Method bar = Inter2.class.getMethod( "bar", int.class );
		Method bar1 = Inter1.class.getMethod( "bar", Integer.class );
		assertFalse( helper.overrides(
				bar,
				bar1
		) );
		assertFalse( helper.overrides(
				bar1,
				bar
		) );
		assertFalse( helper.same(
				bar,
				bar1
		) );
		assertTrue( helper.same(
				bar1,
				bar
		) );
	}

	interface Inter1 {

		void foo(int i);

		void bar(Integer i);
	}

	interface Inter2 extends Inter1 {

		void foo(Integer i);

		void bar(int i);
	}
}
