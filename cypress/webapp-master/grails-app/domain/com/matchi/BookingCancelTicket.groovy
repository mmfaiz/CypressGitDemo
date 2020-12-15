package com.matchi

/**
 * @author Sergei Shushkevich
 */
class BookingCancelTicket extends Ticket implements Serializable {

    Long bookingId
    Long bookingCustomerId

    static constraints = {
        bookingId(nullable: false)
        bookingCustomerId(nullable: false)
    }
}
