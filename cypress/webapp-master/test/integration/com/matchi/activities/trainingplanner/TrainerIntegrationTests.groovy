package com.matchi.activities.trainingplanner

import com.matchi.Availability
import com.matchi.Facility
import com.matchi.Sport
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 *  Integration tests for Trainer
 */
class TrainerIntegrationTests extends GroovyTestCase {

    @Test
    void testGetCurrentAndFutureAvailabilities() {
        LocalDateTime thisHour = new LocalDateTime().withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        LocalDateTime inAnHour     = thisHour.plusHours(1)

        Facility facility = Facility.first()
        Trainer trainer1 = createTrainer(facility, Sport.first(), null, true)

        LocalDate now = new LocalDate()

        assert !trainer1.availabilities
        assert trainer1.getCurrentAndFutureAvailabilities(now).size() == 0

        Availability a1 = createAvailability(1, thisHour.toLocalTime(), inAnHour.toLocalTime())
        trainer1.addToAvailabilities(a1)

        assert trainer1.availabilities?.size() == 1
        assert trainer1.getCurrentAndFutureAvailabilities(now).size() == 1

        a1.validEnd = new LocalDate(now.plusDays(1))
        a1.validStart = a1.validEnd
        a1.save(flush: true, failOnError: true)

        assert trainer1.availabilities?.size() == 1
        assert trainer1.getCurrentAndFutureAvailabilities(now).size() == 1

        a1.validEnd = new LocalDate(now.minusDays(1))
        a1.validStart = a1.validEnd
        a1.save(flush: true, failOnError: true)

        assert trainer1.availabilities?.size() == 1
        assert trainer1.getCurrentAndFutureAvailabilities(now).size() == 0

        a1.validEnd = now
        a1.validStart = now
        a1.save(flush: true, failOnError: true)

        assert trainer1.availabilities?.size() == 1
        assert trainer1.getCurrentAndFutureAvailabilities(now).size() == 1
        assert trainer1.getCurrentAndFutureAvailabilities(new Date()).size() == 1
    }
}
