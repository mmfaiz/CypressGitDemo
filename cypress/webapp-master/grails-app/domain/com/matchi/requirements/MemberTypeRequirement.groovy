package com.matchi.requirements

import com.matchi.Customer
import com.matchi.StringHelper
import com.matchi.membership.MembershipType

class MemberTypeRequirement extends Requirement {

    static hasMany = [ types: MembershipType ]

    static mapping = {
        types lazy: false
    }

    static constraints = {
    }

    /**
     * Validates to true if the customer has any of the MembershipTypes of this requirement
     * @param customer
     * @return
     */
    @Override
    boolean validate(Customer customer) {
        if(types?.size() > 0) {
            return types?.any { MembershipType type ->
                return customer.hasMembershipType(type)
            }
        }

        return false
    }

    @Override
    Map getRequirementProperties() {
        return [ typeIds: types?.collect { it.id } ]
    }

    @Override
    void setValues(def values) {

        List<MembershipType> selectedTypes

        if(!values.typeIds.equals(StringHelper.LIST_PLACE_HOLDER)) {
            selectedTypes = values.typeIds
                    .findAll { !it.equals(StringHelper.LIST_PLACE_HOLDER) }
                    .collect { String typeId ->
                return MembershipType.get(Long.parseLong(typeId))
            }
        }


        this.setTypes(selectedTypes?.toSet())
    }
}
