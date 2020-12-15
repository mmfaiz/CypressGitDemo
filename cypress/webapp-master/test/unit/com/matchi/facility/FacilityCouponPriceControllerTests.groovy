package com.matchi.facility

import com.matchi.CouponService
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.coupon.Coupon
import com.matchi.price.PriceListCustomerCategory
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityCouponPriceController)
@Mock([Coupon, Facility, Municipality, PriceListCustomerCategory, Region])
class FacilityCouponPriceControllerTests {

    void testIndex() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def coupon = createCoupon(facility)
        def couponServiceControl = mockCouponService()

        couponServiceControl.demand.findCouponByTypeAndId {type, id ->
            assert type =="Coupon"
            assert id == coupon.id
            return coupon
        }
        def model = controller.index(coupon.id, "Coupon")

        assert model
        assert coupon == model.coupon
        securityServiceControl.verify()
    }

    void testSave() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def coupon = createCoupon(facility)
        def category = new PriceListCustomerCategory(facility: facility, name: "test").save(failOnError: true)
        params.prices = [[price: 100, customerCategory: [id: category.id]]]
        def couponServiceControl = mockCouponService()

        couponServiceControl.demand.findCouponByTypeAndId {type, id ->
            assert type =="Coupon"
            assert id == coupon.id
            return coupon
        }
        couponServiceControl.demand.save { c -> c }

        controller.save(coupon.id, "Coupon")

        assert "/facility/coupons/prices/index/$coupon.id" == response.redirectedUrl
        assert 1 == coupon.prices.size()
        def price = coupon.prices.iterator().next()
        assert 100 == price.price
        assert category == price.customerCategory
        securityServiceControl.verify()
        couponServiceControl.verify()
    }

    private mockSecurityService(facility) {
        def serviceControl = mockFor(SecurityService)
        controller.securityService = serviceControl.createMock()
        serviceControl
    }

    private mockCouponService() {
        def couponServiceMock = mockFor(CouponService)
        controller.couponService = couponServiceMock.createMock()
        couponServiceMock
    }
}
