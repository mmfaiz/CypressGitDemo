package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalDate
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(SeasonDeviationService)
@Mock([Customer, Facility, Municipality, Region, Slot, Court, Sport])
class SeasonDeviationServiceTests extends Specification {

    def slotService = Mock(SlotService)

    def setup() {
        service.slotService = slotService
    }

    void testApplyOpen() {
        def seasonDeviation = new SeasonDeviation(fromDate: LocalDate.now(),
                toDate: LocalDate.now(), weekDays: "1", courtIds: "1")
        def court = createCourt()
        def existingSlot1 = createSlot(court, new DateTime(2019, 1, 1, 10, 0).toDate(),
                new DateTime(2019, 1, 1, 11, 0).toDate())
        def existingSlot2 = createSlot(court, new DateTime(2019, 1, 1, 11, 0).toDate(),
                new DateTime(2019, 1, 1, 12, 0).toDate())
        def existingSlot3 = createSlot(court, new DateTime(2019, 1, 1, 12, 0).toDate(),
                new DateTime(2019, 1, 1, 13, 0).toDate())
        def newSlot = new Slot(court: court, startTime: new DateTime(2019, 1, 1, 10, 30).toDate(),
                endTime: new DateTime(2019, 1, 1, 11, 30).toDate())

        when:
        service.applyOpen(seasonDeviation)

        then:
        1 * slotService.generateSlots(_) >> [newSlot]
        1 * slotService.getSlots(_) >> [existingSlot1, existingSlot2, existingSlot3]
        1 * slotService.removeSlots({it.size() == 2 && it.contains(existingSlot1) && it.contains(existingSlot2)})
        1 * slotService.createSlots({it.size() == 1 && it[0] == newSlot})
    }
}
