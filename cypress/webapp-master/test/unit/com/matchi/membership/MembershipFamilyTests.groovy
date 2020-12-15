package com.matchi.membership

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.OrderStatusService
import com.matchi.Region
import com.matchi.User
import com.matchi.integration.IntegrationService
import com.matchi.orders.Order
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import spock.lang.Specification
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(MembershipFamily)
@Mock([Membership, Customer, Facility, User, Region, Municipality, Order])
class MembershipFamilyTests extends Specification {

    OrderStatusService orderStatusService

    @Before
    void setup() {
        orderStatusService = new OrderStatusService()
        def mockIntegrationService = mockFor(IntegrationService)
        orderStatusService.integrationService = mockIntegrationService.createMock()
        mockIntegrationService.demand.send(1..1) {  }
        defineBeans {
            orderStatusService(InstanceFactoryBean, orderStatusService, OrderStatusService)
        }
    }

    @Test
    void testSetSharedOrder() {
        def facility = createFacility()
        def customer1 = createCustomer(facility)
        def customer2 = createCustomer(facility)
        def customer3 = createCustomer(facility)
        def family = new MembershipFamily(contact: customer1).save(failOnError: true)
        def membership1 = createMembership(customer1)
        membership1.family = family
        membership1.save(failOnError: true)
        def order1 = membership1.order
        order1.price = 200
        order1.save(failOnError: true)
        def membership2 = createMembership(customer2)
        membership2.family = family
        membership2.save(failOnError: true)
        def order2 = membership2.order
        def membership3 = createMembership(customer3)
        membership3.family = family
        membership3.save(failOnError: true)
        def order3 = membership3.order
        order3.price = 100
        order3.save(failOnError: true)
        family.members = [membership1, membership2, membership3]
        family.save(failOnError: true)

        when:
        family.setSharedOrder(order1)

        then:
        order1.id
        order1.status == Order.Status.COMPLETED
        order2.status == Order.Status.COMPLETED
        order2.isFree()
        order3.status == Order.Status.ANNULLED
        membership1.order.id == order1.id
        membership2.order.id != order1.id
        membership3.order.id == order1.id
    }
}
