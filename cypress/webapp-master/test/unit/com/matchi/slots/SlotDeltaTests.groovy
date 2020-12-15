package com.matchi.slots

import com.matchi.Court
import com.matchi.Slot
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

class SlotDeltaTests {

    SlotDelta delta

    @Before
    public void setUp() {


    }

    @Test
    void testSlotEquals() {
        def left = [create(1,1,2), create(1,1,3),create(1,1,4)]
        def right = [create(1,1,2), create(1,1,3),create(1,1,4)]

        assert left == right
    }

    @Test
    public void testLeft() {
        def left =  [create(1,1,2), create(1,1,3),create(1,1,4)]
        def right = [create(1,1,2), create(1,1,3),create(1,1,4),create(1,1,8)]
        delta = new SlotDelta(left, right)

        assert delta.leftOnly().size() == 0
    }


    @Test
    public void testRight() {
        def left = [create(1,1,2), create(1,1,3),create(1,1,4)]
        def right = [create(1,1,2), create(1,1,3),create(1,1,4),create(1,1,5)]
        delta = new SlotDelta(left, right)

        assert delta.rightOnly().size() == 1
    }

    @Test
    public void testRightOverlaps() {
        def left = [create(1,1,4, 0, 45), create(1,1,5, 0, 45), create(1,1,6, 0, 45)]
        def right = [create(1,1,4, 0, 60), create(1,1,5, 0, 60), create(1,1,6, 0, 60), create(1,1,7, 0, 60)]
        delta = new SlotDelta(left, right)

        assert delta.intersection().size() == 0
        assert delta.leftOnly().size() == 3
        assert delta.rightOverlaps(delta.leftOnly()).size() == 3
    }

    @Test
    public void testRightOverlapsMultipleOverlaps() {
        def left = [create(1,1,4, 0, 360)]
        def right = [create(1,1,3, 0, 90), create(1,1,4, 30, 30), create(1,1,5, 0, 30), create(1,1,5, 30, 30)]
        delta = new SlotDelta(left, right)

        assert delta.intersection().size() == 0
        assert delta.leftOnly().size() == 1
        assert delta.rightOverlaps(delta.leftOnly()).size() == 1
        assert delta.rightOverlaps(delta.leftOnly()).get(left.get(0)).size() == 4
    }

    @Test
    public void testRightOverlapsMultipleOverlapsSameEndStartTime() {
        def left = [create(1,1,4, 0, 60)]
        def right = [create(1,1,5, 0, 60)]
        delta = new SlotDelta(left, right)

        assert delta.intersection().size() == 0
        assert delta.leftOnly().size() == 1
        assert delta.rightOverlaps(delta.leftOnly()).size() == 0
    }

    @Test
    public void testIntersection() {
        def left = [create(1,1,4), create(1,1,5),create(1,1,6)]
        def right = [create(1,1,2), create(1,1,3),create(1,1,4),create(1,1,5)]
        delta = new SlotDelta(left, right)

        assert delta.intersection().size() == 2
    }

    @Test
    public void test() {
        def left = [create(1,1,4), create(1,1,5),create(1,1,6)]
        def right = [create(1,1,2), create(1,1,3),create(1,1,4),create(1,1,5)]
        delta = new SlotDelta(left, right)

        assert delta.union().size() == 5
    }

    def create(def courtId, def day = 1, def hour = 1, def minute = 1, def length = 60) {

        Slot slot = new Slot()
        slot.court = new Court()
        slot.court.id = courtId

        def start = new DateTime(2012, 1, day, hour, minute, 0, 0)

        slot.startTime = start.toDate()
        slot.endTime   = start.plusMinutes(length).toDate()
        return slot
    }
}
