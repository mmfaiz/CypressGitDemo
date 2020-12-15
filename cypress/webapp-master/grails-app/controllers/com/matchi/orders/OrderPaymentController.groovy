package com.matchi.orders

import com.matchi.Court
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Sport
import com.matchi.price.Price
import com.matchi.slots.SlotFilter
import grails.validation.Validateable
import org.joda.time.DateTime

class OrderPaymentController {

    def bookingService
    def userService
    def slotService
    def priceListService

    def confirm(ConfirmBookingCommand cmd) {
        def facility = Facility.get(cmd.facilityId)

        if(!facility) {
            throw new IllegalArgumentException("Could not find facility with id ${cmd.facilityId}")
        }
        def alternativeSlots = []
        def slot

        if(cmd.start && cmd.end) {
            alternativeSlots = getAvailableSlotsWithPrice(facility, new DateTime(cmd.start), new DateTime(cmd.end), cmd.sportIds)
        }

        if(params.slotId) {
            slot = slotService.getSlot(params.slotId)
        } else if(alternativeSlots.size() > 0) {
            slot = alternativeSlots.first().slot
        } else {
            throw new IllegalArgumentException("Could not find any slots")
        }

        def user = userService.getLoggedInUser()
        def customer = Customer.findByUserAndFacility(user, slot.court.facility)

        if(!slot.isBookable()) {
            render view: "error", model: [errorMessage: message(code: "orderPayment.confirm.error1")]
        } else if(slot.booking) {
            render view: "error", model: [errorMessage: message(code: "orderPayment.confirm.error2")]
        } else {
            def validCoupons = []
            def paymentInfo  = null
            [ slot: slot, user:user, validCoupons:validCoupons, paymentInfo: paymentInfo,
                    slots: alternativeSlots]
        }
    }

    private def getAvailableSlotsWithPrice(Facility facility, DateTime start, DateTime end, List<Long> sportIds) {

        def slotsWithPrice = []
        def surfaces = Court.Surface.list()
        def sports = Sport.findAll()

        if (sportIds?.size() > 0) {
            sports = sports.find { sportIds.contains(it.id) }
        }

        SlotFilter slotsFilterCommand = new SlotFilter()
        slotsFilterCommand.courts = courtService.findUsersCourts([facility], sports, surfaces, getCurrentUser())
        slotsFilterCommand.from   = start
        slotsFilterCommand.to     = end
        slotsFilterCommand.onlyFreeSlots = true

        def slots = slotService.getSlots(slotsFilterCommand).sort()
        slots = slots.findAll { it.isBookable() }

        slots.each {
            Price price = priceListService.getBookingPrice(it, userService.getLoggedInUser())

            if (price?.price > 0) {
                slotsWithPrice << [
                        slot: it,
                        price: price
                ]
            }
        }

        return slotsWithPrice
    }
}

@Validateable(nullable = true)
class ConfirmBookingCommand {
    Long facilityId
    Long start
    Long end
    List<Long> sportIds
}
