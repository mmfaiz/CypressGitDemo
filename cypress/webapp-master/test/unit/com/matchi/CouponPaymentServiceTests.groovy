package com.matchi

import com.matchi.coupon.Coupon
import com.matchi.coupon.CouponPrice
import com.matchi.coupon.Offer
import com.matchi.orders.Order
import com.matchi.price.PriceListCustomerCategory
import grails.test.mixin.*
import org.junit.*

import static com.matchi.TestUtils.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CouponPaymentService)
@Mock([Order, Coupon, Facility, Municipality, Region, PriceListCustomerCategory, CouponPrice, Customer, User, Offer])
class CouponPaymentServiceTests {

    def coupon
    def couponPrice
    def customer
    def user
    def couponName
    def facilityName

    @Before
    public void setUp() {
        def facility = createFacility()
        facilityName = facility.name

        couponName = "Stora klippkortet"
        coupon = new Coupon(facility: facility, name: couponName)
        coupon.id = 20
        coupon = coupon.save(validate: false)

        user = new User().save(validate: false)
        customer = new Customer(user: user, facility: facility).save(validate: false)


        def category = new PriceListCustomerCategory(facility: facility, name: "test").save(failOnError: true)
        couponPrice = new CouponPrice(price: 100, coupon: coupon, customerCategory: category).save(failOnError: true)
    }

    void testArticleCouponType() {
        def order = service.createCouponPaymentOrder(user, coupon)
        assert order.article == Order.Article.COUPON
    }

    void testOrderDescription() {
        def order = service.createCouponPaymentOrder(user, coupon)
        assert order.description == "${facilityName} ${couponName}, ${coupon.nrOfTickets} klipp"
    }

    void testOrderPrice() {
        couponPrice.price = 150
        coupon.facility.vat = 6

        def order = service.createCouponPaymentOrder(user, coupon)
        assert order.price == 150
        assert order.vat   == 8.49
    }
}
