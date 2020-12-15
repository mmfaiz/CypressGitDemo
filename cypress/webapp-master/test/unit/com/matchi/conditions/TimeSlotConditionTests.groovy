package com.matchi.conditions

import com.matchi.Slot
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalTime

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(TimeSlotCondition)
class TimeSlotConditionTests {

    void testAccept() {
        def condition = new TimeSlotCondition()

        condition.startTime = new LocalTime("10:00")
        condition.endTime   = new LocalTime("12:00")

        def slot = new Slot(startTime: new DateTime(2010,1,1,10,0).toDate(), endTime: new DateTime(2010,1,1,11,0).toDate())
        assert condition.accept(slot)

        slot = new Slot(startTime: new DateTime(2010,1,1,9,0).toDate(), endTime: new DateTime(2010,1,1,10,0).toDate())
        assert !condition.accept(slot)

        slot = new Slot(startTime: new DateTime(2010,1,1,11,0).toDate(), endTime: new DateTime(2010,1,1,12,0).toDate())
        assert condition.accept(slot)

        slot = new Slot(startTime: new DateTime(2010,1,1,12,0).toDate(), endTime: new DateTime(2010,1,1,13,0).toDate())
        assert !condition.accept(slot)
    }

}
