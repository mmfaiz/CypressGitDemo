package com.matchi

import com.matchi.async.ScheduledTaskService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(NotificationService)
@Mock([ User, Customer ])
class NotificationServiceTests {

    Customer customer

    @Before
    public void setUp() {
        customer = new Customer(id: 1l).save(validate: false)
    }

    @Test
    public void testGetCustomerEmailReturnsCustomerEmailIfNoUser() {
        customer.email = "customer@mail.com"
        customer.save(validate: false)

        assert service.getCustomerEmailAddress(customer) == customer.email
    }

    @Test
    public void testGetCustomerEmailReturnsGuardiansEmail() {
        customer.guardianEmail = "guardian@mail.com"
        customer.save(validate: false)

        assert service.getCustomerEmailAddress(customer) == customer.guardianEmail
    }

    @Test
    public void testGetCustomerEmailReturnsNullIfNoCustomerEmailAndNoUser() {
        assert !service.getCustomerEmailAddress(customer)
    }

    @Test
    void testExecuteSendingWithSomeFailing() {

        def mockScheduledTaskService = mockFor(ScheduledTaskService)
        service.scheduledTaskService = mockScheduledTaskService.createMock()

        Facility f = new Facility()
        Long facilityId = 1l
        f.id = facilityId

        List<Integer> collectionToRun = [1,2,3,4,5]
        String tName = "Sending mails"

        mockScheduledTaskService.demand.scheduleTask(1) { String taskName, Long domainIdentifier, Facility facility, Closure closure ->
            assert tName == taskName
            assert f == facility
            assert domainIdentifier == facilityId

            closure.call()
        }

        shouldFail {
            service.executeSending(collectionToRun, tName, f) { Integer i ->
                if(i == 1) {
                    throw new RuntimeException("Crash")
                }
            }
        }

        mockScheduledTaskService.verify()
    }
}
