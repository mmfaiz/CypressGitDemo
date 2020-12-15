package com.matchi

import com.matchi.orders.Order
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.ControllerUnitTestCase
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(UserProfileController)
@Mock([User, Order, Region, Facility, Municipality])
class UserProfileControllerTests {

    def springSecurityServiceMock
    def remotePaymentServiceMock
    def orderServiceMock
    User user
    Order order

    void setUp() {
        springSecurityServiceMock = mockFor(SpringSecurityService, true)
        remotePaymentServiceMock = mockFor(RemotePaymentService, true)
        orderServiceMock = mockFor(OrderService, true)
        user = TestUtils.createUser()
        order = TestUtils.createOrder(user, TestUtils.createFacility(), Order.Article.BOOKING)
    }

    void tearDown() {
    }



    void testRemotePayment() {
        controller.remotePaymentService = remotePaymentServiceMock.createMock()
        controller.orderService = orderServiceMock.createMock()
        controller.springSecurityService = springSecurityServiceMock.createMock()

        springSecurityServiceMock.demand.getCurrentUser(1) { ->
            user
        }

        remotePaymentServiceMock.demand.getRemotePayableOrdersFor { User u ->
            assert u == user
            return []
        }

        def result = controller.remotePayments()
        assert result
    }

    void testRemotePaymentForOrderId() {
        controller.params.put("showOrderId", "42")

        controller.remotePaymentService = remotePaymentServiceMock.createMock()
        controller.orderService = orderServiceMock.createMock()
        controller.springSecurityService = springSecurityServiceMock.createMock()

        springSecurityServiceMock.demand.getCurrentUser(1) { ->
            user
        }

        remotePaymentServiceMock.demand.getRemotePayableOrdersFor { User u ->
            assert u == user
            return [order]
        }

        orderServiceMock.demand.getOrder(1) { Long id ->
            assert id == 42
            return order
        }

        def result = controller.remotePayments()
        assert result
    }
}
