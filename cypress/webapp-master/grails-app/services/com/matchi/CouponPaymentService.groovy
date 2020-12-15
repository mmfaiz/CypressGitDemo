package com.matchi

import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.coupon.Offer
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import grails.transaction.Transactional
import org.joda.time.LocalDate

class CouponPaymentService {

    def couponService
    def customerService
    def notificationService
    UserService userService
    OrderStatusService orderStatusService

    static transactional = false

    /**
     * Creates a customer coupon and links the coupon to the users customer for the facility
     * @param user The user which is receiving the coupon
     * @param coupon The Coupon
     * @param numTickets Number of initial tickets
     * @param createdBy User which registered this coupon purchase (can be different from the one using it)
     * @param payment Optional payment information
     * @return The created customer coupon
     */
    def registerCouponPurchase(User user, Offer coupon, int numTickets, User createdBy, Order order = null) {
        log.info("Registering customer coupon ${coupon.name} (${coupon.id}) on ${user} ${order?"(order: " + order?.id + ")":""}")
        def customer = customerService.getOrCreateUserCustomer(user, coupon.facility)

        def expireDate = null

        if(order) {
            def exists = CustomerCoupon.findByOrder(order)
            if(exists) {
                log.debug("Coupon with order ${order.id} already delivery to customer")
                return null
            }
        }

        if(coupon.endDate || (coupon.nrOfDaysValid && coupon.nrOfDaysValid > 0)) {
            expireDate = coupon.endDate ? new LocalDate(coupon.endDate) :
                    new LocalDate().plusDays(coupon.nrOfDaysValid)
        }

        def customerCoupon = CustomerCoupon.link(createdBy, customer, coupon, numTickets,
                expireDate, "", CustomerCouponTicket.Type.CUSTOMER_PURCHASE)

        customerCoupon.order = order

        if(customerCoupon.order) {
            notificationService.sendOnlineCouponReceipt(customerCoupon)
        }

        return customerCoupon
    }

    /**
     * Creates a payment order for a online coupon sale
     * @param user the user buying the coupon
     * @param coupon the coupon beeing purchased
     * @return
     */
    @Transactional
    def createCouponPaymentOrder(User user, Offer coupon) {
        Facility facility = coupon?.facility
        Customer customer = Customer.findByUserAndFacility(user, facility)

        Order order        = new Order()
        order.article      = Order.Article.COUPON
        order.description  = coupon.createOrderDescription()
        order.metadata     = ["couponId" : coupon.id.toString()]
        order.customer     = customer
        order.user         = user
        order.issuer       = user
        order.facility     = coupon.facility
        order.dateDelivery = new Date()
        Amount amount      = coupon.toAmount(Customer.findByUserAndFacility(user, coupon.facility))
        order.origin       = Order.ORIGIN_WEB

        order.price        = amount.amount
        order.vat          = amount.VAT

        order.save(failOnError: true)
        return order;
    }

    /**
     * Makes CouponOrderPayment and consumes customer coupon ticket
     * @param order
     * @param customerCouponId
     * @return
     */
    @Transactional
    CouponOrderPayment createCouponOrderPayment(Order order, Long customerCouponId, PaymentMethod method) {
        def customerCoupon = CustomerCoupon.get(customerCouponId)

        CouponOrderPayment payment = new CouponOrderPayment()
        payment.issuer        = userService.getCurrentUser()
        payment.amount        = order.total()
        payment.vat           = order.vat()
        payment.status        = OrderPayment.Status.CAPTURED
        payment.ticket        = couponService.consumeTicket(customerCoupon, order)
        payment.method        = method
        payment.ticket.save(failOnError: true)
        payment.save(failOnError: true)

        log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Ticket with amount ${payment.ticket.getAmount()} consumed successfully for ${order?.id}")

        order.addToPayments(payment)

        log.debug("Added payment: ${payment.id} to order: ${order.id}")

        if(order.isFinalPaid()) {
            orderStatusService.complete(order, userService.getCurrentUser())
        } else {
            orderStatusService.confirm(order, userService.getCurrentUser())
        }
        order.save(failOnError: true)

        return payment
    }
}
