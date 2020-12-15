package com.matchi

import com.matchi.membership.Membership
import grails.validation.Validateable

/* Created: 2012-05-05 Mattias (mattias@tdag.se) */

@Validateable(nullable = true)
class FacilityMemberUserCommand {
    Long facilityId
    String email
    String membernumber
    String firstname
    String lastname
    String address
    String zipcode
    String city
    Long municipality
    String country
    String birthday
    String telephone
    String genderValue

    static constraints = {
        membernumber(blank: true, nullable: true, validator: {membernumber, obj ->
                    def facility = Facility.get(obj.properties['facilityId'])
                    def facilityMember = Membership.findByFacilityAndMembernumber(facility, membernumber)
                    facilityMember ? ['invalid.membernrnotunique'] : true
        })
        email(blank:false, nullable: false, email: true, validator: {email, obj ->
            def user = User.findByEmail(email)
            user ? ['invalid.emailnotunique'] : true
        })
        firstname(blank: false, nullable:false)
        lastname(blank: false, nullable:false)
    }
}
