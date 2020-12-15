package com.matchi

import com.matchi.enums.RedeemType
import com.matchi.mpc.CodeRequest
import grails.validation.Validateable

import javax.servlet.http.HttpServletResponse

class UserBookingController extends GenericBookingController {

    def userService
    def facilityService
    def bookingService
    def courtService
    def slotService
    def priceListService
    def paymentService
    def ticketService

    def dateUtil

    def cancel(UserCancelBookingCommand cmd) {
        def slot = slotService.getSlot(cmd.slotId)

        if(slot && slot.booking) {
            bookingService.cancelBooking(slot.booking, "")
        }

        render(view: "cancelReceipt", model: [slot: slot, returnUrl:params.returnUrl])
    }

    def cancelConfirm(UserCancelBookingCommand cmd) {

        log.info("User ${getCurrentUser().email} confirms cancelling booking with id ${cmd.slotId}")

        def slot = slotService.getSlot(cmd.slotId)
        def facility = slot?.court?.facility
        def payment = null
        def redeemBooking = false
        def authCancel = true
        def accessCode

        if (facility?.hasMPC()) {
            accessCode = CodeRequest.findByBooking(slot?.booking)?.code
        } else {
            accessCode = FacilityAccessCode.validAccessCodeFor(slot)?.content
        }

        if (facility.subscriptionRedeem && slot?.subscription && slot?.booking?.customer == slot?.subscription?.customer) {
            redeemBooking = true
        }

        if(slot && slot.booking) {
            payment = slot.booking.payment
        } else {
            flash.error = message(code: "userBooking.cancelConfirm.error")
        }

        if (payment?.paymentTransactions && payment?.paymentTransactions?.size() > 0) {
            authCancel = false
        }

        render(view: "cancel", model: [ slot:slot, payment:payment, redeemBooking:redeemBooking,
                accessCode:accessCode, authCancel:authCancel, returnUrl:params.returnUrl ])
    }

    def showByTicket() {
        def ticket = BookingCancelTicket.findByKey(params.ticket)
        if (ticket && ticket.isValid()) {
            def booking = Booking.get(ticket.bookingId)
            if (booking && booking.customer.id == ticket.bookingCustomerId) {
                return [ticket: ticket, booking: booking]
            } else {
                response.sendError HttpServletResponse.SC_NOT_FOUND
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def cancelByTicket() {
        def ticket = BookingCancelTicket.findByKey(params.ticket)
        if (ticket && ticket.isValid()) {
            def booking = Booking.get(ticket.bookingId)
            if (booking && booking.customer.id == ticket.bookingCustomerId) {
                def date = booking.slot.startTime
                def court = booking.slot.court
                bookingService.cancelBooking(booking, "", false, RedeemType.NORMAL, springSecurityService.getCurrentUser(), true, false, true)
                ticketService.consumeBookingCancelTicket(ticket)
                return [date: date, court: court]
            } else {
                response.sendError HttpServletResponse.SC_NOT_FOUND
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}

@Validateable(nullable = true)
class UserCancelBookingCommand {
    String slotId

    static constraints = {
        slotId(blank: false, nullable: false)
    }
}
