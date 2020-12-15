package com.matchi

import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.Period
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(SeasonDeviation)
class SeasonDeviationTests {

    @Before
    public void setUp() {
        mockForConstraintsTests(SeasonDeviation)
    }

    @Test
    void testValidDeviation() {
        assert createValid().validate()
    }

    @Test
    void testWithinTrue() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(10, 1).toLocalDate()
        deviation.toDate = createTime(12,1).toLocalDate()
        deviation.fromTime = new LocalTime("07:00")
        deviation.toTime = new LocalTime("21:00")

        def slot = createSlot(createCourt(1), createTime(10, 7), 1)

        assert deviation.isWithin(slot)
    }

    @Test
    void testWithinTrueOneHourDeviation() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(10, 1).toLocalDate()
        deviation.toDate = createTime(12,1).toLocalDate()
        deviation.fromTime = new LocalTime("12:00")
        deviation.toTime = new LocalTime("13:00")

        def slot = createSlot(createCourt(1), createTime(10, 12), 1)

        assert deviation.isWithin(slot)
    }

    @Test
    void testWithinFalseDiffDate() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(10, 1).toLocalDate()
        deviation.toDate = createTime(12,1).toLocalDate()
        deviation.fromTime = new LocalTime("01:00")
        deviation.fromTime = new LocalTime("03:00")

        def slot = createSlot(createCourt(1), createTime(5, 6), 1)

        assert !deviation.isWithin(slot)
    }

    @Test
    void testWithinFalseDiffHour() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(1, 1).toLocalDate()
        deviation.toDate = createTime(3,1).toLocalDate()
        deviation.fromTime = new LocalTime("01:00")
        deviation.fromTime = new LocalTime("03:00")

        def slot = createSlot(createCourt(1), createTime(1, 6), 1)

        assert !deviation.isWithin(slot)
    }

    @Test
    void testWithinFalseDiffCourts() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(1, 1).toLocalDate()
        deviation.toDate = createTime(3,1).toLocalDate()
        deviation.fromTime = new LocalTime("01:00")
        deviation.fromTime = new LocalTime("03:00")
        deviation.courtIds = "2,3"

        def slot = createSlot(createCourt(1), createTime(1, 3), 1)

        // court id mismatch
        assert !deviation.isWithin(slot)
    }

    @Test
    void testWithinFalseDiffWeekDays() {
        SeasonDeviation deviation = new SeasonDeviation()
        deviation.fromDate = createTime(1, 1).toLocalDate()
        deviation.toDate = createTime(8,1).toLocalDate()
        deviation.fromTime = new LocalTime("07:00")
        deviation.toTime = new LocalTime("21:00")
        deviation.courtIds = "1"
        deviation.weekDays = "1,2,4,5,6,7" // not wednesday

        println DateTimeConstants.MONDAY

        def slot = createSlot(createCourt(1), createTime(4, 9), 1)

        // weekday mismatch
        assert !deviation.isWithin(slot)
    }

    SeasonDeviation createValid() {
        SeasonDeviation deviation = new SeasonDeviation()

        deviation.name = "Test"
        deviation.season = new Season()
        deviation.timeBetween = new Period()
        deviation.bookingLength = new Period()

        return deviation
    }

    def createTime(def day, def hour) {
        return new DateTime(2012, 1, day, hour, 0, 0);
    }

    def createSlot(def court, def time, def durationHours) {
        Slot slot = new Slot()
        slot.startTime = time.toDate()
        slot.endTime   = time.plus(durationHours).toDate()
        slot.court = court
        slot;
    }

    def createCourt(def id) {
        Court court = new Court()
        court.id = id
        return court
    }

}
