package com.matchi

import com.matchi.async.ScheduledTaskService
import com.matchi.integration.IntegrationService
import com.matchi.orders.Order
import com.matchi.season.CreateSeasonCommand
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotFilter
import grails.test.GrailsMock
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.spock.Integration
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(OrderStatusService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Order])
class OrderStatusServiceTests {

    def mockIntegrationService

    @Before
    public void setUp() {
        MockUtils.mockLogging(IntegrationService, true)
        mockIntegrationService= mockFor(IntegrationService)
        service.integrationService = mockIntegrationService.createMock()
        mockIntegrationService.demand.send(1..1) { Order order, Closure codeToExecute -> codeToExecute() }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testFutureDeliveryDateOnAnnullation() {
        Order order = new Order()
        order.dateDelivery = new Date().plus(1)
        service.annul(order, TestUtils.createSystemEventInitiator())
        assert !order.dateDelivery.after(new Date())
    }

    @Test
    void testPastDeliveryDateOnAnnullation() {
        Order order = new Order()
        Date originalDate = new Date().minus(1)
        order.dateDelivery = originalDate
        service.annul(order, TestUtils.createSystemEventInitiator())

        assert order.dateDelivery.equals(originalDate)
    }
}
