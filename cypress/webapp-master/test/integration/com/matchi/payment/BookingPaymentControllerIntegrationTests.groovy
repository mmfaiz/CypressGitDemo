package com.matchi.payment

import com.matchi.Booking
import com.matchi.Player
import com.matchi.PriceList
import com.matchi.PriceListService
import com.matchi.async.ScheduledTaskService
import com.matchi.coupon.CustomerCoupon
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.price.CourtPriceCondition
import com.matchi.price.DatePriceCondition
import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class BookingPaymentControllerIntegrationTests extends GroovyTestCase {

    def bookingService
    def couponService
    def customerService
    def facilityService
    def springSecurityService

    void testBookingPlayers() {
        def facility = createFacility()
        def user = createUser()
        def customer1 = createCustomer(facility,
                user.email, user.firstname, user.lastname, null, user)
        def customer2 = createCustomer(facility, "jane@matchi.com", "Jane", "Doe")
        def booking1 = createBooking(customer1)
        new Player(booking: booking1, email: customer2.email, customer: customer2)
                .save(failOnError: true, flush: true)
        def controller = new BookingPaymentController(
                customerService: customerService, facilityService: facilityService)
        springSecurityService.reauthenticate user.email

        controller.bookingPlayers(facility.id, "j")

        assert controller.response.status == 200
        assert controller.response.json.size() == 1
        assert controller.response.json[0].id == "jane@matchi.com"
        assert controller.response.json[0].name == "Jane Doe (jane@matchi.com)"
        assert controller.response.json[0].fieldValue == "Jane Doe"
    }

    void testPayWithCouponFailed() {
        def facility = createFacility()
        def customer = createCustomer(facility)
        def user = createUser()
        def slot = createSlot(createCourt(facility))
        def order = createOrder(user, facility, Order.Article.BOOKING, [slotId: slot.id])
        order.customer = customer
        order.save(failOnError: true, flush: true)
        def coupon = createCoupon(facility)
        def customerCoupon = CustomerCoupon.link(user, customer, coupon, 10)
        def cmd = new MultipleBookingPaymentCommand(slotIds: [slot.id], customerCouponId: customerCoupon.id, method: PaymentMethod.COUPON)
        def controller = new BookingPaymentController(bookingService: bookingService,
                couponService: couponService, springSecurityService: springSecurityService)

        springSecurityService.reauthenticate user.email

        controller.params.method = PaymentMethod.COUPON

        controller.payEntryPoint(cmd)

        assert controller.modelAndView.viewName == "/bookingPayment/showError"
    }
}