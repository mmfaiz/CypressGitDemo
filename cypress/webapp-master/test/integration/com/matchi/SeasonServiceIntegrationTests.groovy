package com.matchi

import com.matchi.season.CreateSeasonCommand
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotDeleteException
import org.joda.time.DateTime
import org.joda.time.Period

import static com.matchi.TestUtils.*
import static com.matchi.TestUtils.createBooking
import static com.matchi.TestUtils.createCourt
import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createSlot

class SeasonServiceIntegrationTests extends GroovyTestCase {

    def seasonService

    void testIllegalArgumentException() {
        Season season = createSeason()

        shouldFail(IllegalArgumentException) { ->
            seasonService.updateSeason(null, null, true, null)
        }

        shouldFail(IllegalArgumentException) { ->
            seasonService.updateSeason(season, null, true, "task")
        }

        UpdateSeasonCommand cmd = new UpdateSeasonCommand()

        shouldFail(IllegalArgumentException) { ->
            seasonService.updateSeason(season, cmd, true, "task")
        }

        cmd.startDate = season.startTime

        shouldFail(IllegalArgumentException) { ->
            seasonService.updateSeason(season, cmd, true, "task")
        }

        cmd.startDate = null
        cmd.endDate = season.endTime

        shouldFail(IllegalArgumentException) { ->
            seasonService.updateSeason(season, cmd, true, "task")
        }
    }

    void testGetSeasonDeviations() {
        def season1 = createSeason()
        def season2 = createSeason()
        def dev1 = new SeasonDeviation(season: season1, name: "dev1", timeBetween: new Period(),
                bookingLength: new Period()).save(failOnError: true)
        def dev2 = new SeasonDeviation(season: season2, name: "dev1", timeBetween: new Period(),
                bookingLength: new Period()).save(failOnError: true, flush: true)

        def result = seasonService.getSeasonDeviations(season1)

        assert result.size() == 1
        assert result[0].id == dev1.id

        result = seasonService.getSeasonDeviations(season2)

        assert result.size() == 1
        assert result[0].id == dev2.id
    }
}
