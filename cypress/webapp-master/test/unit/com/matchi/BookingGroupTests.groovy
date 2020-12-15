package com.matchi

import com.matchi.enums.BookingGroupType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before

@TestFor(BookingGroup)
@Mock([BookingGroup, Booking, Slot])
class BookingGroupTests  {

    @Before
    public void setUp() {
        mockForConstraintsTests(BookingGroup)
        mockForConstraintsTests(Booking)
        mockForConstraintsTests(Slot)
    }

    void testVerifySave() {
        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.TRAINING

        group = group.save()

        assertNotNull(group.id)
    }

    void testTypeNotNullable() {
        BookingGroup group = new BookingGroup()
        assertFalse group.validate()
        assertEquals 'Type can not be null', 'nullable', group.errors['type']
    }

    void testBookingsAreSavedWithGroup() {
        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.TRAINING

        Booking booking = new Booking(customer: new Customer())

        def court = new Court()
        court.id = 2
        Slot slot = new Slot(startTime: createTime(1),
                                endTime: createTime(2), court: court)
        booking.slot = slot
        booking.save(flush: true)
        slot.booking = booking
        slot.save(flush: true)

        group.addToBookings(booking)
        group.save(flush: true)

        assertTrue(slot.validate())
        assertTrue(booking.validate())
        assertTrue(group.validate())
        assertNotNull(group.id)
        assertNotNull(group.bookings.first())
        assertNotNull(group.bookings.first().id)
    }

    void testIsTypeReturnsTrue() {
        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.DEFAULT

        assert group.isType(BookingGroupType.DEFAULT)
    }

    void testIsTypeReturnsFalse() {
        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.DEFAULT

        assert !group.isType(BookingGroupType.ACTIVITY)
    }



    def createTime(int hour) {
        return new DateTime(2011, 11, 20, hour, 0, 0, 0).toDate()
    }
}
