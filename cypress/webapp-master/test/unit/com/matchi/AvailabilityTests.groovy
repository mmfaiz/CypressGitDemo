package com.matchi

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Availability)
class AvailabilityTests {

    void testValidityIsNullable() {
        Availability availability = new Availability(
               weekday: 1,
               begin: new LocalTime(),
               end: new LocalTime().plusHours(1),
               validStart: null,
               validEnd: null
        ).save(flush: true, failOnError: true)

        assert !availability.validStart
        assert !availability.validEnd
    }

    void testValidityIsFormatted() {
        DateTime badValidStart = new DateTime().withHourOfDay(12).withMinuteOfHour(32).withSecondOfMinute(22).withMillisOfSecond(333)
        DateTime badValidEnd = badValidStart.plusWeeks(1)

        Availability availability = new Availability(
                weekday: 1,
                begin: new LocalTime(),
                end: new LocalTime().plusHours(1),
                validStart: new LocalDate(badValidStart),
                validEnd: new LocalDate(badValidEnd)
        ).save(flush: true, failOnError: true)

        assert availability.validStart.toDate() == badValidStart.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate()
        assert availability.validEnd.toDate() == badValidEnd.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate()

        // Also when updated
        availability.validStart = new LocalDate(badValidStart)
        availability.validEnd = new LocalDate(badValidEnd)
        availability.save(flush: true, failOnError: true)

        assert availability.validStart.toDate() == badValidStart.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate()
        assert availability.validEnd.toDate() == badValidEnd.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate()
    }
}
