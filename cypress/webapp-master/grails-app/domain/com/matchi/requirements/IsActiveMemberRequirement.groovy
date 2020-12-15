package com.matchi.requirements

import com.matchi.Customer

class IsActiveMemberRequirement extends Requirement {

    boolean membershipRequired

    static constraints = {
        membershipRequired(nullable: false)
    }

    static mapping = {
        
    }

    @Override
    boolean validate(Customer customer) {
        if(membershipRequired) {
            return customer.hasActiveMembership() || customer.membership?.inStartingGracePeriod
        }

        return !customer.hasActiveMembership() && !customer.membership?.inStartingGracePeriod
    }

    @Override
    Map getRequirementProperties() {
        return [ membershipRequired: membershipRequired ]
    }

    @Override
    void setValues(def values) {
        this.membershipRequired = Boolean.parseBoolean(values.membershipRequired)
    }
}
