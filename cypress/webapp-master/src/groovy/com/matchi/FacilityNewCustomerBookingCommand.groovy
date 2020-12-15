package com.matchi

import grails.validation.Validateable

@Validateable(nullable = true)
class FacilityNewCustomerBookingCommand {

    Long facilityId
    Long userId
    String email
    String firstname
    String lastname
    String telephone

    static constraints = {
        facilityId(nullable: false, blank: false)
        userId(nullable:true, blank: true)
        email(nullable:true, blank: true, email: true)
        firstname(nullable:false, blank: false)
        lastname(nullable:false, blank: false)
        telephone(nullable:true, blank: true)
    }

    String toString() {
        return "[firstname: ${firstname}, lastname: ${lastname}]"
    }
}