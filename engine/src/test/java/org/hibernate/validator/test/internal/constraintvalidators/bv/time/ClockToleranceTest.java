/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.Validator;
import javax.validation.constraints.FutureOrPresent;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ClockToleranceTest {

	private Validator validator;
	private Instant now;

	@BeforeMethod
	public void setUp() throws Exception {
		now = Instant.now();
		validator = getConfiguration()
				.clockSkewTolerance( Duration.ofSeconds( 10 ) )
				.clockProvider( () -> Clock.fixed( now, ZoneId.systemDefault() ) )
				.buildValidatorFactory().getValidator();
	}

	@Test
	public void testFutureTolerance() throws Exception {
		assertNoViolations( validator.validate( new FutureDummyEntity( now.atZone( ZoneId.systemDefault() ).plusSeconds( 5 ) ) ) );
		assertNoViolations( validator.validate( new FutureDummyEntity( now.atZone( ZoneId.systemDefault() ).minusSeconds( 5 ) ) ) );
	}

	private static class FutureDummyEntity {

		@FutureOrPresent
		private Calendar calendar;

		@FutureOrPresent
		private Date date;

		@FutureOrPresent
		private HijrahDate hijrahDate;

		@FutureOrPresent
		private Instant instant;

		@FutureOrPresent
		private JapaneseDate japaneseDate;

		@FutureOrPresent
		private LocalDate localDate;

		@FutureOrPresent
		private LocalDateTime localDateTime;

		@FutureOrPresent
		private MinguoDate minguoDate;

		@FutureOrPresent
		private OffsetDateTime offsetDateTime;

		@FutureOrPresent
		private ThaiBuddhistDate thaiBuddhistDate;

		@FutureOrPresent
		private Year year;

		@FutureOrPresent
		private YearMonth yearMonth;

		@FutureOrPresent
		private ZonedDateTime zonedDateTime;

		public FutureDummyEntity() {
		}

		public FutureDummyEntity(ZonedDateTime dateTime) {
			calendar = GregorianCalendar.from( dateTime );
			date = calendar.getTime();

			instant = dateTime.toInstant();
			localDateTime = dateTime.toLocalDateTime();

			hijrahDate = HijrahDate.from( dateTime );
			japaneseDate = JapaneseDate.from( dateTime );
			localDate = LocalDate.from( dateTime );
			minguoDate = MinguoDate.from( dateTime );
			offsetDateTime = dateTime.toOffsetDateTime();
			thaiBuddhistDate = ThaiBuddhistDate.from( dateTime );
			year = Year.from( dateTime );
			yearMonth = YearMonth.from( dateTime );
			zonedDateTime = dateTime;
		}
	}
}
