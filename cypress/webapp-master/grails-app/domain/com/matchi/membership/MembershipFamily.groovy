package com.matchi.membership

import com.matchi.Customer
import com.matchi.orders.Order

class MembershipFamily implements Serializable {

    static hasMany = [ members: Membership ]

    Customer contact

    static constraints = {
    }

    static mapping = {
        members sort: 'id', order: 'asc'
    }

    Set<Membership> getMembersNotContact() {
        return members.findAll { it.isFamilyMember() }
    }

    void setSharedOrder(Order order) {
        membersNotContact.each { fm ->
            if (!fm.order.isFree()) {
                fm.setSharedOrder(order)
            }
        }
    }
}
