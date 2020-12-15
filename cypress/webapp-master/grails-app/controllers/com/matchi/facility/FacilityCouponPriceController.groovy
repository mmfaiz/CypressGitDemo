package com.matchi.facility

import com.matchi.GenericController
import com.matchi.price.PriceListCustomerCategory

import javax.servlet.http.HttpServletResponse

/**
 * @author Sergei Shushkevich
 */
class FacilityCouponPriceController extends GenericController {

    def couponService

    def index(Long id, String type) {
        def coupon = couponService.findCouponByTypeAndId(type, id)

        if (coupon) {
            List<PriceListCustomerCategory> priceListCustomerCategories = PriceListCustomerCategory.available(coupon.facility).listDistinct()
            return [coupon: coupon, priceListCustomerCategories: priceListCustomerCategories]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def save(Long id, String type) {
        def coupon = couponService.findCouponByTypeAndId(type, id)

        if (coupon) {
            coupon.properties = params

            if (couponService.save(coupon)) {
                flash.message = message(code: "facilityCouponPrice.save.success")
                redirect(action: "index", id: coupon.id, mapping: type + "Prices")
            } else {
                render(view: "index", model: [coupon: coupon])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}
