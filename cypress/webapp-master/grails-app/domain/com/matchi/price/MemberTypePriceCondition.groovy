package com.matchi.price

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.coupon.Offer
import com.matchi.dynamicforms.Form
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import grails.util.Holders
import org.joda.time.LocalDate

class MemberTypePriceCondition extends CustomerPriceCondition {

    static hasMany = [ membershipTypes: MembershipType ]

    @Override
    boolean accept(Slot slot, Customer customer) {
        accept(slot.court.facility, customer)
    }

    @Override
    boolean accept(Offer coupon, Customer customer) {
        accept(coupon.facility, customer)
    }

    @Override
    boolean accept(Form form, Customer customer) {
        accept(form.facility, customer)
    }

    @Override
    boolean accept(Customer customer) {
        return accept(customer.facility, customer)
    }

    boolean accept(Facility facility, Customer customer, Date startTime = new Date()) {
        if (!customer) {
            return false
        }
        def facilityService = Holders.grailsApplication.mainContext.getBean('facilityService')
        def customerService = Holders.grailsApplication.mainContext.getBean('customerService')

        def facilities = facilityService.getAllHierarchicalFacilities(facility)
        def customers = customerService.findHierarchicalUserCustomers(customer)
        return customers.any {
            accept(facilities, it, startTime)
        }
    }

    boolean accept(Collection<Facility> facilities, Customer customer, Date startTime = new Date()) {
        if (!customer) {
            return false
        }

        if (!facilities.contains(customer.facility)) {
            return false
        }

        LocalDate membershipDate = new LocalDate(startTime)
        Membership customerMembership = customer.getMembership(membershipDate)

        if (customerMembership && !customerMembership.isActive(membershipDate)
                && !customerMembership.inStartingGracePeriod) {
            // if customer has 2 memberships at the same time
            // (1st is paid, ended, but still in grace period, 2nd is unpaid,
            // started and without starting grace period),
            // then we should use 1st one (paid) to get membership benefits
            customerMembership = customer.getActiveMembership(membershipDate)
        }

        return customerMembership && membershipTypes.any {it.id && it.id == customerMembership.type?.id}
    }

    static constraints = {
    }
}
