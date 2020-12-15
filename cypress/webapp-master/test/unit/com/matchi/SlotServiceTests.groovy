package com.matchi

import com.matchi.slots.AdjacentSlotGroup
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.*
import org.junit.Before
import org.junit.Test

@TestFor(SlotService)
@TestMixin(GrailsUnitTestMixin)
@Mock([Slot])
class SlotServiceTests {

    def mockDateUtil

    @Before
    public void setUp() {
        MockUtils.mockLogging(SlotService)

        mockDateUtil = mockFor(DateUtil)
        service.dateUtil = mockDateUtil.createMock()
    }

    @Test
    void testDoesNotReturnsNullIfValidCommand() {
        def slots = service.generateSlots(createCommand())
        assertNotNull(slots)
    }

    @Test
    void testThrowsExceptionIfNullCommand() {
        shouldFail(NullPointerException) {
            service.generateSlots(null)
        }
    }

    @Test
    void testRightNumberOfSlots() {
        def slots = service.generateSlots(createCommand(1,1))
        assert 24 == slots.size()
    }

    @Test
    void testRightNumberOfSlotsLargeNumberOfDays() {
        def slots = service.generateSlots(createCommand(100,1))
        def expected = (100 + 1) * 12; // 100 days + plus today and 12 bookables per day
        assert expected == slots.size()
    }

    @Test
    void testRightNumberOfSlotsLargeNumberOfDaysMultipleCourts() {
        def slots = service.generateSlots(createCommand(10,10))
        // ~12,000 slots...
        def expected = ((10 + 1) * 12) * 10; // 10 days + plus today and 12 bookables per day and 20 courts 
        assert expected == slots.size()
    }

    @Test
    void testSlotsHaveCourt() {
        def slots = service.generateSlots(createCommand())
        def firstSlot = slots.get(0)
        assert firstSlot.court != null
    }

    @Test
    void testFirstSlotStartingTime() {
        def slots = service.generateSlots(createCommand(1,1))
        def firstSlot = slots.get(0)
        assertTimeEquals(firstSlot.startTime, 10, 0)
    }

    @Test
    void testFirstSlotStartDate() {
        def command = createCommand()
        def slots = service.generateSlots(command)
        def firstSlot = slots.get(0)
        assert new LocalDate(command.startTime) == new LocalDate(firstSlot.startTime)
    }

    @Test
    void testLastSlotStartDate() {
        def command = createCommand()
        def slots = service.generateSlots(command)
        def lastSlot = slots.get(slots.size() - 1)
        assert new LocalDate(command.startTime).plusDays(10) == new LocalDate(lastSlot.startTime)
    }

    @Test
    void testLastSlotStartTime() {
        def slots = service.generateSlots(createCommand())
        def lastSlot = slots.get(slots.size() - 1)
        assertTimeEquals(lastSlot.startTime, 21, 0)
    }

    @Test
    void testLastSlotEndTime() {
        def slots = service.generateSlots(createCommand())
        def lastSlot = slots.get(slots.size() - 1)
        assertTimeEquals(lastSlot.endTime, 22, 0)
    }

    @Test
    void testFirstSlotStartAndEndTimeWith45minutesIntervals() {
        def cmd = createCommand(1, 0)
        cmd.courts << createCourtCommand("Bana 1", 10, 12, 15, 45)
        def slots = service.generateSlots(cmd)
        def firstSlot = slots.get(0)
        assertTimeEquals(firstSlot.startTime, 10, 0)
        assertTimeEquals(firstSlot.endTime, 10, 45)
    }

    @Test
    void testLastSlotStartAndEndTimeWith45minutesIntervals() {
        def cmd = createCommand(1, 0)
        cmd.courts << createCourtCommand("Bana 1", 10, 12, 15, 45)
        def slots = service.generateSlots(cmd)
        def lastSlot = slots.get(slots.size() - 1)
        assertTimeEquals(lastSlot.startTime, 11, 0)
        assertTimeEquals(lastSlot.endTime, 11, 45)
    }

    @Test
    void testSlotDuringSummerTimeChange() {
        CreateSeason cmd = new CreateSeason()
        cmd.startTime = new DateTime(2012,3,22,9,0,0,0)
        cmd.endTime   = new DateTime(2012,3,22,9,0,0,0).plusDays(10)

        for(int i = 0 ; i < 1 ;i++) {
            cmd.courts << createCourtCommand("Bana ${i}", 0, 4)
        }

        service.generateSlots(cmd)
    }

    @Test
    void testGroupSlotsByStartingTimeAndDuration() {
        Slot slot1 = new Slot(id: 1, startTime: new DateTime().withHourOfDay(10).withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0).toDate(), endTime: new DateTime().withHourOfDay(10).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0).toDate())
        Slot slot2 = new Slot(id: 2, startTime: new DateTime().withHourOfDay(10).withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0).toDate(), endTime: new DateTime().withHourOfDay(10).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0).toDate())

        Slot slot3 = new Slot(id: 3, startTime: new DateTime().withHourOfDay(11).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate(), endTime: new DateTime().withHourOfDay(11).withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0).toDate())
        Slot slot4 = new Slot(id: 4, startTime: new DateTime().withHourOfDay(11).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate(), endTime: new DateTime().withHourOfDay(11).withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0).toDate())

        def groupedSlots = service.groupSlotsByStartingTimeAndDuration([ slot1, slot4, slot3, slot2 ])

        assert groupedSlots.size() == 2
        assert groupedSlots[0].slots.size() == 2
        assert groupedSlots[1].slots.size() == 2

        assert groupedSlots[0].hour     == 10
        assert groupedSlots[0].minute   == 15
        assert groupedSlots[1].hour     == 11
        assert groupedSlots[1].minute   == 0

        assert new DateTime(groupedSlots[0].slots[0].startTime).getHourOfDay() == 10
        assert new DateTime(groupedSlots[0].slots[0].startTime).getMinuteOfHour() == 15
        assert new DateTime(groupedSlots[0].slots[1].startTime).getHourOfDay() == 10
        assert new DateTime(groupedSlots[0].slots[1].startTime).getMinuteOfHour() == 15
        assert new DateTime(groupedSlots[1].slots[0].startTime).getHourOfDay() == 11
        assert new DateTime(groupedSlots[1].slots[0].startTime).getMinuteOfHour() == 0
        assert new DateTime(groupedSlots[1].slots[1].startTime).getHourOfDay() == 11
        assert new DateTime(groupedSlots[1].slots[1].startTime).getMinuteOfHour() == 0
    }

    @Test
    void testGroupSlotsByDate() {
        Slot slot1 = new Slot(id: 1, startTime: new DateTime(2012, 1, 2, 10, 0).toDate())
        Slot slot2 = new Slot(id: 2, startTime: new DateTime(2012, 1, 2, 10, 0).toDate())
        Slot slot3 = new Slot(id: 3, startTime: new DateTime(2012, 1, 3, 10, 0).toDate())
        Slot slot4 = new Slot(id: 4, startTime: new DateTime(2012, 1, 4, 10, 0).toDate())

        def result = service.groupSlotsByDate([slot1, slot2, slot3, slot4])

        assert result.containsKey(new LocalDate(2012,1,2))
        assert result.containsKey(new LocalDate(2012,1,3))
        assert result.containsKey(new LocalDate(2012,1,4))
    }

    @Test
    void testGroupSlotsByDateSortsSlotsByTime() {
        Slot slot1 = new Slot(id: 1, startTime: new DateTime(2012, 1, 2, 11, 0).toDate())
        Slot slot2 = new Slot(id: 2, startTime: new DateTime(2012, 1, 2, 10, 0).toDate())
        Slot slot3 = new Slot(id: 3, startTime: new DateTime(2012, 1, 3, 4, 0).toDate())
        Slot slot4 = new Slot(id: 3, startTime: new DateTime(2012, 1, 3, 10, 0).toDate())
        Slot slot5 = new Slot(id: 3, startTime: new DateTime(2012, 1, 3, 8, 0).toDate())

        def result = service.groupSlotsByDate([slot1, slot2, slot3, slot4, slot5])

        def slotsByDate = result[new LocalDate(2012, 1,3)]

        assert slotsByDate.get(0).startTime < slotsByDate.get(1).startTime
        assert slotsByDate.get(1).startTime < slotsByDate.get(2).startTime

    }

    @Test
    void testAreConsecutive() {
        Date time1 = new Date()
        Date time2 = time1 + 1
        Date time3 = time2 + 1

        Slot slot1 = new Slot(startTime: time1, endTime: time2)
        Slot slot2 = new Slot(startTime: time2, endTime: time3)

        assert service.areConsecutive([])
        assert service.areConsecutive([slot1])
        assert service.areConsecutive([slot1, slot2])
        assert service.areConsecutive([slot2, slot1])

        Slot slot3 = new Slot(startTime: time1, endTime: time2)

        assert !service.areConsecutive([slot1, slot2, slot3]) // Two slots in parallel

        Slot slot4 = new Slot(startTime: time1, endTime: time3 + 1)

        assert !service.areConsecutive([slot2, slot4])
        assert !service.areConsecutive([slot1, slot4])

        Slot slot5 = new Slot(startTime: time3, endTime: time3 + 1)

        assert service.areConsecutive([slot1, slot2, slot5])
    }

    @Test
    void testGroupByAdjacency() {
        shouldFail (IllegalArgumentException) {
            service.groupByAdjacency(null)
        }

        shouldFail (IllegalArgumentException) {
            service.groupByAdjacency([])
        }

        Date time1 = new Date()
        Date time2 = time1 + 1
        Date time3 = time2 + 1
        Date time4 = time3 + 1
        Date time5 = time4 + 1

        Court court = new Court()

        Slot slot1 = new Slot(startTime: time1, endTime: time2, court: court)
        Slot slot2 = new Slot(startTime: time2, endTime: time3, court: court)
        Slot slot3 = new Slot(startTime: time3, endTime: time4, court: court)
        Slot slot4 = new Slot(startTime: time4, endTime: time5, court: court)

        List<AdjacentSlotGroup> result = service.groupByAdjacency([slot1])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1]
        assert !result[0].subsequentSlots // Not populated in this method

        assert service.areConsecutive([slot1, slot2])
        result = service.groupByAdjacency([slot2, slot1])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1, slot2] // Ordered
        assert !result[0].subsequentSlots

        assert !service.areConsecutive([slot1, slot2, slot4])
        result = service.groupByAdjacency([slot2, slot1, slot4])
        assert result.size() == 2
        assert result[0].selectedSlots == [slot1, slot2] // Ordered
        assert result[1].selectedSlots == [slot4]

        assert service.areConsecutive([slot4, slot1, slot3, slot2])
        result = service.groupByAdjacency([slot4, slot1, slot3, slot2])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1, slot2, slot3, slot4] // Ordered

        // Empty split list
        result = service.groupByAdjacency([slot4, slot1, slot3, slot2], [])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1, slot2, slot3, slot4] // Ordered

        // Testing to split
        result = service.groupByAdjacency([slot4, slot1, slot3, slot2], [slot3])
        assert result.size() == 2
        assert result[0].selectedSlots == [slot1, slot2]
        assert result[1].selectedSlots == [slot3, slot4]
    }

    CreateSeason createCommand() {
        return createCommand(10, 1)
    }

    CreateSeason createCommand(def days, def numCourts) {
        CreateSeason cmd = new CreateSeason()
        cmd.startTime = new DateTime(2012,3,22,9,0,0,0)
        cmd.endTime   = new DateTime(2012,3,22,9,0,0,0).plusDays(days)

        for(int i = 0 ; i < numCourts ;i++) {
            cmd.courts << createCourtCommand("Bana ${i}", 10, 22)
        }

        return cmd
    }

    private CreateCourtSeason createCourtCommand(def courtName, def openHour, def closingHour) {
        return createCourtCommand(courtName, openHour, closingHour, 0, 60)
    }

    private CreateCourtSeason createCourtCommand(def courtName, def openHour, def closingHour, def timeBetween, def bookingLength) {
        CreateCourtSeason courtCmd = new CreateCourtSeason()
        courtCmd.timeBetween = new Period().plusMinutes(timeBetween)
        courtCmd.bookingLength = new Period().plusMinutes(bookingLength)

        courtCmd.court = new Court(name: courtName)

        for (int i = DateTimeConstants.MONDAY; i <= DateTimeConstants.DAYS_PER_WEEK; i++) {
            OpenHours openHours = new OpenHours()
            openHours.opening = new LocalTime(openHour, 0)
            openHours.closing = new LocalTime(closingHour, 0)

            courtCmd.addOpenHours(i, openHours)
        }
        return courtCmd
    }

    private assertTimeEquals(DateTime time, def hour, def minute) {
        assertEquals(hour, time.hourOfDay)
        assertEquals("Minutes should equal", minute, time.minuteOfHour)
        assertEquals(0, time.secondOfMinute)
        assertEquals(0, time.millisOfSecond)
    }

    private assertTimeEquals(Date time, def hour, def minute) {
        assertTimeEquals(new DateTime(time), hour, minute)
    }
}
