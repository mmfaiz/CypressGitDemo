package com.matchi

import com.matchi.facility.FilterFacilityGroupsCommand
import com.matchi.membership.MembershipType
import com.matchi.price.CustomerGroupPriceCondition
import grails.transaction.Transactional

class MembershipTypeService {
    static transactional = false

    @Transactional
    MembershipType createMembershipType(MembershipType membershipType, Facility facility) {
        membershipType.facility = facility
        membershipType.save()
        return membershipType
    }

    MembershipType createMembershipType(String name, Facility facility) {
        MembershipType membershipType = new MembershipType(name: name)
        return createMembershipType(membershipType, facility)
    }
}
