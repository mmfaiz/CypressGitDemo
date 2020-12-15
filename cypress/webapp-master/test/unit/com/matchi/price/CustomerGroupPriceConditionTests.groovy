package com.matchi.price

import com.matchi.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CustomerGroupPriceCondition)
@Mock([CustomerGroupPriceCondition, Group, Slot, User, Customer, CustomerGroup])
class CustomerGroupPriceConditionTests {

    CustomerGroupPriceCondition condition
    Slot slot, slot2
    Group group,group2
    User user
    User user2

    Customer customer
    Customer customer2

    @Before
    public void setUp() {
        condition = new CustomerGroupPriceCondition()

        group = new Group(id: 1, name: "Grupp 1")
        group2 = new Group(id: 2, name: "Grupp 2")

        group.id = 1
        group2.id = 2

        slot = new Slot()
        slot2 = new Slot()

        user = new User(id: 1, email: "c@c.se")
        user2 = new User(id: 2, email: "d@d.se")

        user.id = 1
        user2.id = 2

        customer = new Customer(id: 1, email: "c@c.se")
        customer2 = new Customer(id: 2, email: "d@d.se")

        customer.id = 1
        customer2.id = 2

        customer.user = user
        customer2.user = user2

        customer.customerGroups = [new CustomerGroup(customer: customer, group: group)]
        customer2.customerGroups = [new CustomerGroup(customer: customer2, group: group2)]

        customer
        customer2
    }


    void testCustomerIsAcceptedIfBelongsToGroup() {

        condition.addToGroups(group)
        assert condition.accept(slot, customer)
    }

    void testCustomerIsNotAcceptedIfBelongsToGroup() {
        condition.addToGroups(group)
        assert !condition.accept(slot, customer2)
    }
}
