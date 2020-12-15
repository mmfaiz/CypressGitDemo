package com.matchi.api

import com.matchi.*
import com.matchi.adyen.AdyenException
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.PromoCode
import com.matchi.enums.RedeemType
import com.matchi.orders.Order
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import grails.validation.Validateable
import org.apache.commons.validator.EmailValidator

class BookingResourceController extends GenericAPIController {

    def bookingService
    def courtService
    def customerService
    def notificationService
    OrderStatusService orderStatusService

    def list() {
        def bookings = bookingService.getUserBookings(getCurrentUser())
        render bookings as JSON
    }

    def create() {
        def cmd = new BookingCommand(request?.JSON)
        cmd.payment = new PaymentCommand(request?.JSON?.payment?:[:])

        if(!cmd.payment.validate()) {
            error(400, Code.INPUT_ERROR, "Invalid payment information")
            return
        }

        // Using the same e-mail validation as in web (BookingPaymentController#payEntryPoint).
        if(cmd.playerEmails) {
            if (cmd.playerEmails.any { !EmailValidator.getInstance().isValid(it) }) {
                error(400, Code.INVALID_EMAIL, "Invalid player e-mail")
                return
            }
        }

        def slots = validateSlots(cmd.toSlots())
        try {
            Facility facility = slots[0].court.facility
            Customer customer = Customer.findByFacilityAndUser(facility, getCurrentUser())
            if(courtService.getCourtGroupRestrictions(customer, facility, slots) || !canBookMore(slots)) {
                error(400, Code.INPUT_ERROR, "You cannot have more bookings for this facility")
                return
            }

            def bookings = bookAndHandlePaymentAndSentEmails(slots, cmd.payment, cmd.playerEmails, cmd.promoCodeId)
            render bookings as JSON
        } catch (APIException apiException) {
            error(apiException.status, apiException.errorCode, apiException.userMessage)
        }
    }

    void notifyPlayers(Booking booking) {
        notificationService.sendNewBookingNotification(booking, null)
        booking.players.each {Player player ->
            if (!player.isBookingCustomer(booking.customer.email)) {
                notificationService.sendNewBookingPlayerNotification(booking, player)
            }
        }
    }

    def cancel(CancelBookingCommand cmd) {

        cmd.id = params.long("id")

        def booking = Booking.get(cmd.id)
        User sessionUser = getCurrentUser()

        if(!booking) {
            error(400, Code.RESOURCE_NOT_FOUND, "Could not locate booking (${cmd.id})")
        } else {
            if(booking.customer.user && booking.customer.user.id == sessionUser.id) {
                bookingService.cancelBooking(booking, "", false, RedeemType.NORMAL, getCurrentUser(), true)
                render booking as JSON
            } else {
                error(403, Code.ACCESS_DENIED, "Access denied")
            }
        }
    }

    private def bookAndHandlePaymentAndSentEmails(def slots, def paymentCommand, List<String> playerEmails, Long promoCodeId) {
        def orders = []

        try {
            // create all orders
            slots.each { slot ->
                if (slot.booking) {
                    throw new BookingException("Slot ${slot} is already booked. Could not execute booking")
                }
            }
            slots.each { slot ->
                Order order = createOrder(slot, playerEmails)
                orders << order
            }

            if (promoCodeId && paymentCommand.method != PaymentMethod.COUPON) {
                Facility facility = slots.first().court.facility
                User user = getCurrentUser()
                PromoCode promoCode = couponService.getValidPromoCode(promoCodeId, facility)
                if (promoCode && slots.every { Slot slot -> promoCode.accept(slot)} && promoCode.startDate.before(new Date()) &&
                        (promoCode.endDate.after(new Date()) || promoCode.endDate.equals(new Date())) &&
                        !couponService.isPromoCodeUsed(user, promoCode)) {
                    couponService.usePromoCodeForOrders(promoCode, orders, grailsApplication.config.matchi.settings.currency[facility.currency].decimalPoints)
                    new CustomerCoupon(customer: customerService.getOrCreateUserCustomer(user, facility), coupon: promoCode,
                            createdBy: user).save()
                }
            }

            // handle payments
            pay(orders, paymentCommand)

            // book
            orders.each {
                //this also sent notification
                bookingService.book(it)
            }

        } catch(PaymentException pe) {
            log.error("Error while processing payments", pe)
            rollback(orders)
            throw new APIException(400, Code.PAYMENT_ERROR, "Could not process payment")
        } catch(AdyenException ae) {
            handleAdyenException(ae)
            rollback(orders)
            throw new APIException(400, Code.PAYMENT_ERROR, ae.message?.size() > 0 ? ae.message : "Could not process payment")
        } catch(BookingException be) {
            log.error("An error occured while processing bookings", be)
            rollback(orders)
            throw new APIException(400, Code.INPUT_ERROR, "Could not process bookings")
        } catch(Throwable t) {
            log.error("An unknown error occured while processing bookings", t)
            rollback(orders)
            throw new APIException(400, Code.UNKNOWN_ERROR, "Could not process bookings")
        }

        return Booking.findAllByOrderInList(orders)
    }

    private def createOrder(Slot slot, List<String> playerEmails) {
        def customers = []
        playerEmails.each { email ->
            def c = Customer.findByFacilityAndEmailAndArchived(slot.court.facility, email as String, false)
            customers << (c ?: new Customer(email: email))
        }
        List<Long> playerCustomerIds =  customers.collect { Customer c -> c.id }

        def order = paymentService.createBookingOrder(slot, getCurrentUser(), Order.ORIGIN_API, playerCustomerIds)
        order.addPlayersToMetadata(customers)
        order.assertCustomer()
        return order
    }

    private def validateSlots(def slots) {

        if(slots.isEmpty()) {
            throw new APIException(400, Code.INPUT_ERROR, "No free slots found")
        }

        def facilities = slots.collect { it.court.facility } as Set

        if(facilities.size() > 1) {
            throw new APIException(400, Code.INPUT_ERROR, "Only slots from one facility can be booked at the same time")
        }

        return slots
    }

    private def rollback(def orders) {
        orders.each { order ->
            def booking = Booking.findByOrder(order)
            if(booking) {
                bookingService.cancelBooking(booking , "", true, RedeemType.NORMAL, getCurrentUser(), false)
            } else {
                orderStatusService.annul(order, getCurrentUser(), "Transaction rollback", order.total())
            }
        }
    }

    private def getFacility(def slots) {
        slots.first().court.facility
    }

    private void handleAdyenException(AdyenException e) {
        String msg = "Error while processing payments"
        if (e.resultCode == "Refused") {
            log.warn(msg)
        } else {
            log.error(msg, e)
        }
    }

    private boolean canBookMore(List<Slot> slots) {
        Facility facility = slots[0].court.facility
        Customer customer = Customer.findByFacilityAndUser(facility, getCurrentUser())

        if(customer) {
            return customer.canBookMoreSlots(slots.size())
        } else if(facility.hasBookingLimitPerCustomer()) {
            return facility.getMaxBookingsPerCustomer() >= slots.size()
        }

        return true
    }

}


@Validateable(nullable = true)
class BookingCommand {
    List<String> slotIds
    PaymentCommand payment
    List<String> playerEmails
    Long promoCodeId

    static constraints = {
        slotIds(nullable: false, blank: false)
        playerEmails(nullable:true, blank: true)
        promoCodeId(nullable: true, blank: true)
    }

    def toSlots() {
        def slots = slotIds.collect { Slot.get(it) }
        slots.findAll { it != null }
    }
}

@Validateable(nullable = true)
class CancelBookingCommand {
    Long id

    static constraints = {
        id nullable: false
    }
}

@Validateable(nullable = true)
class PaymentCommand {

    static def availablePaymentMethods =
            [ PaymentMethod.COUPON, PaymentMethod.GIFT_CARD, PaymentMethod.FREE, PaymentMethod.CREDIT_CARD_RECUR ]

    String method
    String couponId

    PaymentMethod getPaymentMethod() {
        return PaymentMethod.valueOf(method)
    }

    static constraints = {
        method(nullable: false, validator: { val, obj ->
            try {
                return availablePaymentMethods.contains(PaymentMethod.valueOf(val))
            } catch(IllegalArgumentException e) {
                return false
            }
        })
        couponId(nullable: true, blank: true)
    }
}
