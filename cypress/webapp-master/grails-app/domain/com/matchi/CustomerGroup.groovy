package com.matchi

class CustomerGroup implements Serializable {

    static belongsTo = [ group: Group ]
    static hasOne = [ customer: Customer]

    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        autoTimestamp true
    }

    static CustomerGroup link(customer, group) {
        def customerGroup = CustomerGroup.findByCustomerAndGroup(customer, group)
        if (!customerGroup)
        {
            customerGroup = new CustomerGroup(group: group, customer: customer)
            customerGroup.save()
        }
        return customerGroup
    }

    static void unlink(customer, group) {
        def customerGroup = CustomerGroup.findByCustomerAndGroup(customer, group)
        if (customerGroup)
        {
            customer.removeFromCustomerGroups(customerGroup)
            group.removeFromCustomerGroups(customerGroup)
            customerGroup.delete(flush: true)
        }
    }
}
