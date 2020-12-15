package com.matchi

import com.matchi.Facility
import grails.transaction.Transactional
import org.springframework.util.StopWatch
import com.matchi.Booking
import org.joda.time.DateTime
import com.matchi.protocol.ApiBookingsRequest
import com.matchi.protocol.ApiRequestedBookings
import com.matchi.protocol.ApiBookingsRequestException
import com.matchi.protocol.ApiBookingsRequestError
import com.matchi.protocol.ApiRequestedBookingEntry

class ApiBookingsService {

    static transactional = false

    @Transactional
    def processBookingsRequest(ApiBookingsRequest bookingsRequest) {

        Facility facility = findFacility(bookingsRequest)

        ApiRequestedBookings requestedBookings = new ApiRequestedBookings();
        requestedBookings.name = facility.name

        StopWatch watch = new StopWatch("Bookings for ${facility.name}")
        watch.start()

        def bookingCriteria = Booking.createCriteria()
        def bookings = bookingCriteria.list {
            if(bookingsRequest.startDate && bookingsRequest.endDate) {
                between('startTime', new DateTime(bookingsRequest.startDate).minusMillis(1).toDate(),
                        new DateTime(bookingsRequest.endDate).plusMillis(1).toDate())
            }

            court {
                eq("facility", facility)
            }

            order("id", "asc")
        }

        def entries = getBookingEntries(bookings)
        requestedBookings.entries = entries;

        if(entries.size() > 0) {
            requestedBookings.success = true
        }

        watch.stop();
        requestedBookings.executionTime = watch.lastTaskTimeMillis

        return requestedBookings;
    }

    public Facility findFacility(ApiBookingsRequest request) {
        Facility facility = null

        if(!request.facilityId) {
            throw new ApiBookingsRequestException(ApiBookingsRequestError.Code.FACILITY_NOT_FOUND)
        }
        try {
            facility = Facility.findById(request.facilityId)
        } catch(Exception exception) {
            throw new ApiBookingsRequestException(ApiBookingsRequestError.Code.FACILITY_NOT_FOUND)
        }

        if(facility == null) {
            throw new ApiBookingsRequestException(ApiBookingsRequestError.Code.FACILITY_NOT_FOUND)
        }

        return facility
    }

    private def getBookingEntries(def bookings) {
        def entries = []

        bookings.each { booking ->
            def addThis = true

            if(addThis) {
                ApiRequestedBookingEntry entry = createFromBooking(booking)
                entries << entry
            }
        }

        return entries
    }

    @Transactional
    private ApiRequestedBookingEntry createFromBooking(Booking booking) {
        ApiRequestedBookingEntry entry = new ApiRequestedBookingEntry()

        log.info("Booking user: " + booking.user)

        def date = new DateTime(booking.slot.startTime).toString("yyyy-MM-dd")
        def startTime = new DateTime(booking.slot.startTime).toString("HH:mm")
        def endTime = new DateTime(booking.slot.endTime).plusMillis(1).toString("HH:mm")

        entry.date = date
        entry.startTime = startTime
        entry.endTime = endTime
        entry.courtName = booking.slot.court.name
        if(booking.user) {
            entry.email = booking.user.email
            entry.name = booking.user.fullName()
            entry.telephone = booking.user.telephone
        }
        if(booking.group)
            entry.type = booking.group.type.name
        if(booking.price)
            entry.price = booking.price.price

        return entry
    }
}
