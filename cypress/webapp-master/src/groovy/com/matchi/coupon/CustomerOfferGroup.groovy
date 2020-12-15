package com.matchi.coupon

import grails.compiler.GrailsCompileStatic

/**
 * Represents grouped customer's offers (CustomerCoupon).
 *
 * @author Sergei Shushkevich
 */
@GrailsCompileStatic
class CustomerOfferGroup {

    Long id
    String name
    Integer remainingNrOfTickets

    public static List<CustomerOfferGroup> fromCustomerCoupons(
            Collection<CustomerCoupon> customerCoupons) {

        customerCoupons?.groupBy {
            it.coupon.id
        }.collect { id, cc ->
            def group = new CustomerOfferGroup(id: id, name: cc[0].coupon.name)
            if (!cc[0].coupon.unlimited) {
                group.remainingNrOfTickets = (Integer) cc.sum(0) {
                    ((CustomerCoupon) it).nrOfTickets
                }
            }
            group
        }
    }
}