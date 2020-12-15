package com.matchi

import com.matchi.slots.AdjacentSlotGroup
import com.matchi.slots.SlotFilter
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*
import static com.matchi.TestUtils.createCustomer

class SlotServiceIntegrationTests extends GroovyTestCase {

    def slotService
    def dateUtil

    void testGroupByAdjacencyRequiresSameCourt() {
        Facility facility = createFacility()

        Court court1 = createCourt(facility)
        Slot slot1 = createSlot(court1)

        Court court2 = createCourt(facility)
        Slot slot2 = createSlot(court2)

        assert !slotService.areSameCourt([slot1, slot2])

        shouldFail (IllegalArgumentException) {
            slotService.groupByAdjacency([slot1, slot2])
        }
    }

    void testGroupByCourtAndAdjacency() {
        Facility facility = createFacility()
        Court court1 = createCourt(facility)

        Date time1 = new Date()
        Date time2 = time1 + 1
        Date time3 = time2 + 1
        Date time4 = time3 + 1
        Date time5 = time4 + 1

        Slot slot1 = createSlot(court1, time1, time2)
        Slot slot2 = createSlot(court1, time2, time3)

        List<AdjacentSlotGroup> slotGroups = slotService.groupByCourtAndAdjacency([slot1, slot2])
        assert slotGroups.size() == 1
        assert slotGroups[0].selectedSlots == [slot1, slot2]
        assert !slotGroups[0].subsequentSlots

        Court court2 = createCourt(facility)
        Slot slot3 = createSlot(court2, time1, time2)
        Slot slot4 = createSlot(court2, time2, time3)

        slotGroups = slotService.groupByCourtAndAdjacency([slot4, slot1, slot3, slot2])
        assert slotGroups.size() == 2
        assert slotGroups[0].selectedSlots == [slot3, slot4]
        assert slotGroups[1].selectedSlots == [slot1, slot2]

        Slot slot5 = createSlot(court2, time4, time5)
        slotGroups = slotService.groupByCourtAndAdjacency([slot4, slot1, slot3, slot2, slot5])
        assert slotGroups.size() == 3
        assert slotGroups[0].selectedSlots == [slot3, slot4]
        assert slotGroups[1].selectedSlots == [slot5]
        assert slotGroups[2].selectedSlots == [slot1, slot2]
    }

    void testGetBookableSubsequentSlots() {
        Facility facility = createFacility()

        Date time1 = new DateTime().withHourOfDay(0).toDate()
        Date time2 = new DateTime(time1).plusHours(1).toDate()
        Date time3 = new DateTime(time2).plusHours(1).toDate()
        Date time4 = new DateTime(time3).plusHours(1).toDate()
        Date time5 = new DateTime(time4).plusHours(1).toDate()

        Court courtA = createCourt(facility)
        Slot slot1A = createSlot(courtA, time1, time2) // The first slot
        Slot slot2A = createSlot(courtA, time2, time3)
        Slot slot3A = createSlot(courtA, time3, time4) // Having a booking, this should not be selected
        Slot slot4A = createSlot(courtA, time4, time5)

        Customer customer = createCustomer(facility)
        Booking booking3A = createBooking(customer, slot3A)

        List<AdjacentSlotGroup> result = slotService.getSubsequentBookableSlotsFor([slot1A])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1A, slot2A]

        result = slotService.getSubsequentBookableSlotsFor([slot1A, slot2A])
        assert result.size() == 2
        assert result[0].selectedSlots == [slot1A]
        assert result[1].selectedSlots == [slot2A]

        result = slotService.getSubsequentBookableSlotsFor([slot2A])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot2A]

        Court courtB = createCourt(facility)
        Slot slot1B = createSlot(courtB, time1, time2) // The first slot
        Slot slot2B = createSlot(courtB, time2, time3) // Having a booking, this should not be selected
        Slot slot3B = createSlot(courtB, time3, time4)
        Slot slot4B = createSlot(courtB, time4 + 1, time5 + 1) // The day after, should not be fetched

        Booking booking2B = createBooking(customer, slot2B)
        result = slotService.getSubsequentBookableSlotsFor([slot1A, slot1B])
        assert result.size() == 2
        assert result[0].selectedSlots == [slot1A, slot2A]
        assert result[1].selectedSlots == [slot1B]

        result = slotService.getSubsequentBookableSlotsFor([slot1A, slot1B, slot3B])
        assert result.size() == 3
        assert result[0].selectedSlots == [slot1A, slot2A]
        assert result[1].selectedSlots == [slot1B]
        assert result[2].selectedSlots == [slot3B]
    }

    void testcreateAdjacentSlotGroupsWithSubsequentSlotsWithSpace() {
        Facility facility = createFacility()

        Date time1 = new DateTime().withHourOfDay(0).toDate()
        Date time2 = new DateTime(time1).plusHours(1).toDate()
        Date time3 = new DateTime(time2).plusHours(1).toDate()

        // Here is the space :)
        Date time4 = new DateTime(time3).plusHours(2).toDate()
        Date time5 = new DateTime(time4).plusHours(1).toDate()

        Court courtA = createCourt(facility)
        Slot slot1A = createSlot(courtA, time1, time2) // The first slot
        Slot slot2A = createSlot(courtA, time2, time3)
        Slot slot3A = createSlot(courtA, time4, time5) // The one a bit after, not to be returned unless selected

        List<AdjacentSlotGroup> result = slotService.createAdjacentSlotGroupsWithSubsequentSlots([slot1A])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot1A]
        assert result[0].subsequentSlots == [slot1A, slot2A]

        result = slotService.createAdjacentSlotGroupsWithSubsequentSlots([slot2A])
        assert result.size() == 1
        assert result[0].selectedSlots == [slot2A]
        assert result[0].subsequentSlots == [slot2A]

        result = slotService.createAdjacentSlotGroupsWithSubsequentSlots([slot1A, slot3A])
        assert result.size() == 2
        assert result[0].selectedSlots == [slot1A]
        assert result[0].subsequentSlots == [slot1A, slot2A]
        assert result[1].selectedSlots == [slot3A]
        assert result[1].subsequentSlots == [slot3A]
    }

    void testGetSlots() {
        def facility = createFacility()
        def court1 = createCourt(facility)
        def slot1a = createSlot(court1, new DateTime(2019, 1, 15, 14, 0).toDate(),
                new DateTime(2019, 1, 15, 15, 0).toDate())
        def slot1b = createSlot(court1, new DateTime(2019, 1, 25, 15, 0).toDate(),
                new DateTime(2019, 1, 25, 16, 0).toDate())
        def slot1c = createSlot(court1, new DateTime(2019, 1, 26, 16, 0).toDate(),
                new DateTime(2019, 1, 26, 17, 0).toDate())
        def slot1d = createSlot(court1, new DateTime(2019, 1, 27, 17, 0).toDate(),
                new DateTime(2019, 1, 27, 18, 0).toDate())
        def court2 = createCourt(facility)
        def slot2a = createSlot(court2, new DateTime(2019, 1, 20, 10, 30).toDate(),
                new DateTime(2019, 1, 20, 11, 30).toDate())
        def customer = createCustomer(facility)
        createBooking(customer, slot1a)
        createBooking(customer, slot1c)

        // test with no courts
        assert !slotService.getSlots(new SlotFilter())

        // test with "courts-from-to" filter
        assert !slotService.getSlots(new SlotFilter(courts: [court1, court2],
                from: new DateTime(2019, 1, 1, 0, 0), to: new DateTime(2019, 1, 2, 0, 0)))

        def result = slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 1, 0, 0), to: new DateTime(2019, 1, 21, 0, 0)))
        assert result.size() == 1
        assert result[0].id == slot1a.id

        result = slotService.getSlots(new SlotFilter(courts: [court1, court2],
                from: new DateTime(2019, 1, 1, 0, 0), to: new DateTime(2019, 1, 21, 0, 0)))
        assert result.size() == 2
        assert result[0].id == slot1a.id
        assert result[1].id == slot2a.id

        // test with "courts-from-to-onWeekDays" filter
        result = slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 25, 0, 0), to: new DateTime(2019, 1, 31, 0, 0),
                onWeekDays: [6, 7]))
        assert result.size() == 2
        assert result[0].id == slot1c.id
        assert result[1].id == slot1d.id

        result = slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 25, 0, 0), to: new DateTime(2019, 1, 31, 0, 0),
                onWeekDays: [5]))
        assert result.size() == 1
        assert result[0].id == slot1b.id

        assert !slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 25, 0, 0), to: new DateTime(2019, 1, 31, 0, 0),
                onWeekDays: [2, 3]))

        // test with "courts-from-onWeekDays-onlyFreeSlots" filter
        result = slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 1, 0, 0), to: new DateTime(2019, 1, 31, 0, 0),
                onWeekDays: [1, 2, 3, 4, 5, 6, 7], onlyFreeSlots: true))
        assert result.size() == 2
        assert result[0].id == slot1b.id
        assert result[1].id == slot1d.id

        result = slotService.getSlots(new SlotFilter(courts: [court1],
                from: new DateTime(2019, 1, 1, 0, 0), to: new DateTime(2019, 1, 31, 0, 0),
                onWeekDays: [1, 2, 3, 4, 5, 6, 7], onlyFreeSlots: false))
        assert result.size() == 4
        assert result[0].id == slot1a.id
        assert result[1].id == slot1b.id
        assert result[2].id == slot1c.id
        assert result[3].id == slot1d.id
    }

    void testGetSlotsMoreTests() {
        Facility facility = createFacility()
        Court court1 = createCourt(facility)

        DateTime dateTimeStart = new DateTime().withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(8)
        DateTime dateTimeEnd = dateTimeStart.plusHours(1)

        Slot morningSlot = createSlot(court1, dateTimeStart.toDate(), dateTimeEnd.toDate())

        assert slotService.getSlots(new SlotFilter(courts: [court1],
                from: dateUtil.beginningOfDay(dateTimeStart),
                to: dateUtil.endOfDay(dateTimeEnd))
        ) == [morningSlot]

        // Commented away due to H2 database problems with time() function
        // Should be "activated" again if we move to MySQL for testing

        /*assert slotService.getSlots(new SlotFilter(courts: [court1],
                from: dateUtil.beginningOfDay(dateTimeStart),
                to: dateUtil.endOfDay(dateTimeEnd),
                fromTime: dateTimeStart.toLocalTime())
        ) == [morningSlot]

        assert slotService.getSlots(new SlotFilter(courts: [court1],
                from: dateUtil.beginningOfDay(dateTimeStart),
                to: dateUtil.endOfDay(dateTimeEnd),
                fromTime: dateTimeStart.toLocalTime(),
                toTime: dateTimeEnd.toLocalTime())
        ) == [morningSlot]

        assert slotService.getSlots(new SlotFilter(courts: [court1],
                from: dateUtil.beginningOfDay(dateTimeStart),
                to: dateUtil.endOfDay(dateTimeEnd),
                fromTime: dateTimeStart.toLocalTime(),
                toTime: dateUtil.getLocalTimeMidnight())
        ) == [morningSlot] */
    }
}
