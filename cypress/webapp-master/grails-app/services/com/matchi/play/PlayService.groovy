package com.matchi.play

import com.matchi.Booking
import com.matchi.BookingService
import com.matchi.Customer
import com.matchi.CustomerService
import com.matchi.Facility
import com.matchi.RecordingPaymentService
import com.matchi.RecordingPurchase
import com.matchi.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.NotTransactional

class PlayService {
    CustomerService customerService
    RecordingPaymentService recordingPaymentService
    SpringSecurityService springSecurityService
    BookingService bookingService
    def grailsApplication


    Recording getRecordingFromBooking(Booking booking) {
        if (booking && booking.slot?.court?.cameras?.any()) {
            Recording recording = getRecording(booking, booking.slot.startTime, booking.slot.endTime)
            if ((recording && booking?.facility?.hasCameraFeature()) || recording?.recordingPurchase?.isFinalPaid()) {
                return recording
            }
        }
        return null
    }

    Recording getRecording(Booking booking, Date startTime, Date endTime) {
        Recording recording = new Recording()
        // NOTE! Magic strings are used in first step and will be changed in upcoming work when adding support for multiple camera providers.
        recording.bookingId = booking.id
        recording.start = startTime
        recording.end = endTime
        if (booking.customer.user) {
            recording.recordingPurchase = recordingPaymentService.getRecordingPurchaseByUser(recording, booking.customer.user)
        }

        return recording
    }

    Map<Long, Recording> getRecordingsByBookingId(List<Booking> bookings) {
        Map<Long, Recording> recordingsByBookingId = new HashMap<>();
        bookings.each { Booking booking ->
            Facility facility = booking.slot.court.facility
            if (booking) {
                Recording recording = getRecordingFromBooking(booking)
                if ((recording && facility.hasCameraFeature()) || recording?.recordingPurchase?.isFinalPaid()) {
                    recordingsByBookingId.put(booking.id, recording)
                }
            }
        }
        return recordingsByBookingId
    }

    Boolean userCanViewRecording(User user, Recording recording) {
        if (!userCanAccessRecording(user, recording)) {
            return false
        }
        if (recording.requiresPayment()) {
            Customer customer = customerService.findUserCustomer(user, recording.recordingPurchase.order.facility)
            RecordingPurchase recordingPurchase = RecordingPurchase.findByBookingAndCustomer(recording.booking, customer)
            if (recordingPurchase && recordingPurchase.order.isFinalPaid()) {
                return true
            }
            return false
        }
        return true
    }

    @NotTransactional
    List<Recording> getUserRecordings(def user, int offset = 0, int maxPerPage = 0) {
        List<Booking> bookings = bookingService.getUserBookings(user, false, 0, 0, [], true, "desc")
        println bookings
        List<Recording> recordings = []

        bookings.each {
            def recording = getRecordingFromBooking(it)
            if (recording) {
                if (userCanAccessRecording(user, recording) && recording.isPossiblyAccessed()) {
                    recordings.add(recording)
                }
            }
        }

        recordings.subList(
                Math.min(recordings.size(), offset),
                Math.min(recordings.size(), offset + (maxPerPage ?: Integer.MAX_VALUE)))
    }

    Boolean userCanAccessRecording(User user, Recording recording) {
        return recording.booking && user && (recording.booking.customer?.user == user || (recording.booking.players.collect {
            it.customer?.user
        }.contains(user)))
    }
}
