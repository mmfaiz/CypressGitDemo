package com.matchi.requirements

import com.matchi.BookingRestriction
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.User

class RequirementProfile {

    Facility facility
    String name
    Date dateCreated
    Date lastUpdated

    static hasMany = [ requirements: Requirement, bookingRestrictions: BookingRestriction ]

    static constraints = {
        facility(nullable: false)
        name(nullable: false)
    }

    static mapping = {
        sort name: "desc"
        requirements lazy: false
        bookingRestrictions joinTable: [name: "booking_restriction_requirement_profiles", key: "profile_id"]
        bookingRestrictions cascade: 'delete'
    }

    /**
     * Checks if a customer belongs to this RequirementProfile by checking all requirements
     * @param customer
     * @return
     */
    boolean validate(Customer customer) {
        return requirements.every { Requirement requirement ->
            requirement.validate(customer)
        }
    }

    /**
     * Checks if a user belongs to this RequirementProfile by checking all requirements
     * @param user
     * @return
     */
    boolean validate(User user) {
        Customer customer = Customer.findByUserAndFacility(user, this.facility)
        return customer && validate(customer)
    }
}
