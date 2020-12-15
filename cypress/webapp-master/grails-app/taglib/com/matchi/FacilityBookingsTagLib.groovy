package com.matchi

import com.matchi.coupon.CustomerOfferGroup
import com.matchi.coupon.GiftCard
import com.matchi.invoice.Invoice
import org.joda.time.DateTime

class FacilityBookingsTagLib {

    def couponService
    def priceListService
    def paymentService
    def seasonService
    def activityService
    def userService
    def orderService

    def bookingFormInfo = { attrs, body ->
        Booking booking = attrs.booking

        if(!booking) {
            return
        }

        def facility = (Facility)userService.getUserFacility()
        def slots = attrs.slots
        def customer  = booking.customer
        def customers = booking.players.collect { it.customer ?: new Customer() }
        def coupons = CustomerOfferGroup.fromCustomerCoupons(
                couponService.getValidCouponsByCustomerAndSlots(customer, [booking.slot]))
        def giftCards = CustomerOfferGroup.fromCustomerCoupons(
                couponService.getValidCouponsByCustomerAndSlots(customer, [booking.slot], null, GiftCard.class))
        def promoCode = booking?.order?.refunds?.find {it.promoCode != null}?.promoCode

        def payments = orderService.getByPaymentsAndSlots(slots)
        def bookingPrices = (facility.isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())) ?
                priceListService.getAvgBookingPrices(slots, customers ?: [customer]) :
                priceListService.getBookingPrices(slots, customer)
        def occasions = activityService.getOccasionsBySlots(slots)

        out << render(template:"/templates/booking/facilityBookingInfo", model: [facility:facility, slots: slots, customer: customer,
                bookingPrices: bookingPrices, coupons: coupons, giftCards: giftCards, coupon: booking.getCustomerCoupon(), booking: booking, occasions: occasions, payments:payments, promoCode: promoCode ])
    }

    def bookingFormCustomer = { attrs, body ->

        Customer customer = attrs.customer
        if(!attrs.customer) {
            return
        }

        def invoiceAlert = Invoice.findAllByCustomerAndStatus(customer, Invoice.InvoiceStatus.OVERDUE)?.size() > 0

        out << render(template: "/templates/booking/facilityBookingCustomer", model: [customer:customer, invoiceAlert:invoiceAlert])
    }

    def bookingFormPaidPrice = { attrs, body ->
        def bookingPayments = attrs.bookingPayments

        out << render(template: "/templates/booking/facilityBookingPayments", model: [bookingPayments: bookingPayments])
    }

    def bookingFormTotalPrice = { attrs, body ->
        def bookingPrices = attrs.bookingPrices
        if(!bookingPrices) {
            return
        }

        out << render(template: "/templates/booking/facilityBookingPrices", model: [bookingPrices: bookingPrices])
    }

    def bookingFormRecurrence = { attrs, body ->

        def frequency = new BookingGroupFrequencyHandler()
        def slotDateTime = new DateTime(attrs.slot.startTime)

        def date        = slotDateTime.toDate()
        def weekDay     = slotDateTime.dayOfWeek
        def season      = seasonService.getSeasonByDate(date)
        def frequencies = frequency.getFrequencies()

        out << render(template: "/templates/booking/facilityBookingRecurrence", model: [date:date, weekDay:weekDay, season:season, frequencies:frequencies])
    }

    def bookingRecurrenceInfo = { attrs, body ->

        def start = new DateTime(attrs.start)
        def end = new DateTime(attrs.end)
        def weekDays = attrs.weekDays.collect { it.toInteger() }
        //def frequency = new BookingGroupFrequencyHandler().getFrequency(attrs.frequency)
        def frequency = new BookingGroupFrequencyHandler().WEEKLY.type
        def interval = attrs.interval

        out << render(template: "/templates/booking/facilityBookingRecurrenceInfo", model: [ start:start, end:end, weekDays: weekDays, frequency:frequency, interval:interval ])
    }

    def bookingPlayers = { attrs, body ->
        if (!attrs.players) {
            return
        }

        def names = []
        def unknownPlayers = 0

        attrs.players.each {
            if (it.customer) {
                names << it.customer.fullName()
            } else if (it.email) {
                names << it.email
            } else {
                unknownPlayers++
            }
        }

        if (unknownPlayers) {
            names << message(code: "player.unknown.amount", args: [unknownPlayers])
        }

        if (attrs.var) {
            names.each {
                out << body((attrs.var): it)
            }
        } else {
            out << names.join(", ")
        }
    }
}
