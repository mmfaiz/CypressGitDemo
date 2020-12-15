package com.matchi.schedule

import org.joda.time.LocalDate
import com.matchi.ColorFetcher
import com.matchi.User

/**
 *
 */
class DateSchedule {
    User user
    LocalDate fromDate
    LocalDate toDate
    ColorFetcher colorFetcher
    Map<LocalDate, Integer> numFreeSlots = [:]
    def usersBookings = [:]

    def getNumFreeSlots(LocalDate onDate) {
        def num = numFreeSlots.get(onDate)
        return (num?num:0)
    }

    def addUserBooking(def userBooking) {
        LocalDate date = new LocalDate(userBooking.slot.startTime)
        def bookings = usersBookings.get(date)

        if(bookings == null) {
            bookings = []
            usersBookings.put(date, bookings)
        }

        bookings << userBooking
    }

    def status(LocalDate date) {
        def userBookings = usersBookings.get(date)
        def numFreeSlots = getNumFreeSlots(date)
        def result = []

        if(new LocalDate() > date)
            return [Schedule.Status.PAST]

        if(userBookings != null) {
            result << Schedule.Status.OWN_BOOKING

            if(userBookings.findAll { !it.paid }.size() > 0) {
                result << Schedule.Status.OWN_UNPAYED
            }
        }

        if(numFreeSlots < 1)
            result << Schedule.Status.FULL
        else
            result << Schedule.Status.FREE

        return result
    }

    def colorForDate(LocalDate onDate) {
        return ColorFetcher.color(status(onDate))
    }

}
