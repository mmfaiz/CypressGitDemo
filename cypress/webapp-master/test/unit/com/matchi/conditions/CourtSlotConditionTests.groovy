package com.matchi.conditions

import com.matchi.Court
import com.matchi.Slot
import grails.test.mixin.TestFor
import org.junit.Before
/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CourtSlotCondition)
class CourtSlotConditionTests {

    def condition

    @Before
    void setUp() {
        condition = new CourtSlotCondition()
    }

    void testAccept() {
        def court = createCourt(1)
        condition.courts = [court]

        assert condition.accept(createSlot(court))
    }

    void testAcceptWhenMultiple() {
        def court1 = createCourt(1)
        def court2 = createCourt(2)
        def court3 = createCourt(3)

        condition.courts = [court1, court2, court3]

        assert condition.accept(createSlot(court2))
    }

    void testNotAcceptIfNoCourts() {
        def court = createCourt(1)
        condition.courts = []

        assert !condition.accept(createSlot(court))
    }

    void testNotAcceptIfWrongCourts() {
        def court = createCourt(1)
        def other = createCourt(2)

        condition.courts = [court]
        assert !condition.accept(createSlot(other))
    }

    void testNotAcceptIfSlotIsNull() {
        def court = createCourt(1)
        condition.courts = [court]

        assert !condition.accept(null)
    }

    void testNotAcceptIfSlotsCourtIsNull() {
        def court = createCourt(1)
        condition.courts = [court]

        assert !condition.accept(new Slot())
    }

    static def createCourt(def id) {
        def court = new Court()
        court.id = id
        return court
    }

    static def createSlot(def court) {
        def slot = new Slot()
        slot.court = court
        return slot
    }
}
