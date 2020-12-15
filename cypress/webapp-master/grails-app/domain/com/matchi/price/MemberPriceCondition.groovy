package com.matchi.price

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.coupon.Offer
import com.matchi.dynamicforms.Form
import grails.util.Holders

class MemberPriceCondition extends CustomerPriceCondition {

    static constraints = {
    }

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

    boolean accept(Facility facility, Customer customer) {
        if (!customer) {
            return false
        }
        def facilityService = Holders.grailsApplication.mainContext.getBean('facilityService')
        def customerService = Holders.grailsApplication.mainContext.getBean('customerService')

        def facilities = facilityService.getAllHierarchicalFacilities(facility)

        return customerService.findHierarchicalUserCustomers(customer).any {
            test(facilities, it)
        }
    }

    boolean test(Collection<Facility> facilities, Customer customer) {
        if (!customer) {
            return false
        }

        if (!facilities.contains(customer.facility)) {
            return false
        }

        customer.hasActiveMembership() || customer.membership?.inStartingGracePeriod
    }

    boolean accept(Facility facility, Customer customer, Slot slot) {
        if (!customer) {
            return false
        }

        def facilities = facilityService.getAllHierarchicalFacilities(facility)
        return customerService.findHierarchicalUserCustomers(customer).any {
            test(facilities, it, slot)
        }
    }

    boolean test(Collection<Facility> facilities, Customer customer, Slot slot) {
        if (!customer) {
            return false
        }

        if (!facilities.contains(customer.facility)) {
            return false
        }
        customer.hasAnyMembershipAtSlotTime(slot)
    }
}
