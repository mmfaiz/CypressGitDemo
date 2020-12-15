package com.matchi


import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.activities.Participation
import com.matchi.integration.IntegrationService
import com.matchi.orders.Order
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createFacility

@TestFor(ActivityService)
@Mock([ClassActivity, Facility, Municipality, Region, Participation, Customer])
class ActivityServiceTests {

    @Before
    void setUp() {
        MockUtils.mockLogging(BookingService, true)
    }

    @Test
    void testGetActivitiesByFacility() {
        def facility1 = createFacility()
        def activity1 = new ClassActivity(name: "a1", facility: facility1, archived: false)
                .save(failOnError: true)
        def activity2 = new ClassActivity(name: "a2", facility: facility1, archived: true)
                .save(failOnError: true)
        def facility2 = createFacility()
        def activity3 = new ClassActivity(name: "a3", facility: facility2, archived: false)
                .save(failOnError: true)

        def result = service.getActivitiesByFacility(facility1, false)

        assert 1 == result.size()
        assert activity1 == result[0]

        result = service.getActivitiesByFacility(facility1, true)

        assert 1 == result.size()
        assert activity2 == result[0]

        result = service.getActivitiesByFacility(facility2, false)

        assert 1 == result.size()
        assert activity3 == result[0]

        result = service.getActivitiesByFacility(facility2, true)

        assert !result
    }

    @Test
    void testTryAnnulParticipantRefundablePayment() {
        def mockParticipation = mockFor(Participation)
        def mockOrder = mockFor(Order)
        def mockCustomer = mockFor(Customer)
        def mockOrderStatusService = mockFor(OrderStatusService)

        Participation participation = mockParticipation.createMock()
        Customer customer = mockCustomer.createMock()
        Order order = mockOrder.createMock()
        service.orderStatusService = mockOrderStatusService.createMock()

        participation.customer = customer
        participation.order = order

        mockCustomer.demand.fullName(1..1) { ->
            return "Matte Chaisson"
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockParticipation.demand.isRefundable(1..1) { ->
            return true
        }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return true
        }

        mockOrder.demand.total(1..1) { -> return 100.00 }

        mockOrder.demand.annul(1..1) { def whyMessage, def amount ->
            assert whyMessage != null
            assert amount == 100.00
        }

        mockOrderStatusService.demand.annul(1..1) { def whyMessage, def amount ->
            assert whyMessage != null
            assert amount == 100.00
        }

        service.tryAnnulParticipantPayment(participation, TestUtils.createSystemEventInitiator())
    }

    @Test
    void testTryAnnulParticipantNotRefundablePayment() {
        def mockParticipation = mockFor(Participation)
        def mockOrder = mockFor(Order)
        def mockCustomer = mockFor(Customer)
        def mockOrderStatusService = mockFor(OrderStatusService)

        Participation participation = mockParticipation.createMock()
        Customer customer = mockCustomer.createMock()
        Order order = mockOrder.createMock()
        service.orderStatusService = mockOrderStatusService.createMock()

        participation.customer = customer
        participation.order = order

        mockCustomer.demand.fullName(1..1) { ->
            return "Matte Chaisson"
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockParticipation.demand.isRefundable(1..1) { ->
            return false
        }

        mockOrderStatusService.demand.annul(1..1) { def eventInitiator, def whyMessage, def amount ->
            assert whyMessage == null
            assert amount == null
        }

        service.tryAnnulParticipantPayment(participation, TestUtils.createSystemEventInitiator())
    }

    @Test
    void testTryAnnulParticipantNotStillRefundablePayment() {
        def mockParticipation = mockFor(Participation)
        def mockOrder = mockFor(Order)
        def mockCustomer = mockFor(Customer)
        def mockOrderStatusService = mockFor(OrderStatusService)

        Participation participation = mockParticipation.createMock()
        Customer customer = mockCustomer.createMock()
        Order order = mockOrder.createMock()
        service.orderStatusService = mockOrderStatusService.createMock()

        participation.customer = customer
        participation.order = order

        mockCustomer.demand.fullName(1..1) { ->
            return "Matte Chaisson"
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockParticipation.demand.isRefundable(1..1) { ->
            return true
        }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return false
        }

        mockOrderStatusService.demand.annul(1..1) { def eventInitiator, def whyMessage, def amount ->
            assert whyMessage == null
            assert amount == null
        }

        service.tryAnnulParticipantPayment(participation, TestUtils.createSystemEventInitiator())
    }

    @Test
    void isDeletableNullOccasionsTest() {
        def mockActivity = mockFor(ClassActivity)
        mockActivity.demand.getOccasions(1) { ->
            return null
        }

        assert service.isDeletable(mockActivity.createMock())
        mockActivity.verify()
    }

    @Test
    void isDeletableNoOccasionsTest() {
        def mockActivity = mockFor(ClassActivity)
        mockActivity.demand.getOccasions(1) { ->
            return new HashSet<>()
        }

        assert service.isDeletable(mockActivity.createMock())
        mockActivity.verify()
    }

    @Test
    void isDeletableTest() {
        def mockActivity = mockFor(ClassActivity)
        def mockOccasion = mockFor(ActivityOccasion)

        mockOccasion.demand.isPast(1) { ->
            return true
        }

        mockActivity.demand.getOccasions(1) { ->
            return new HashSet<>([mockOccasion.createMock()])
        }

        assert service.isDeletable(mockActivity.createMock())
        mockOccasion.verify()
        mockActivity.verify()
    }

    @Test
    void isNotDeletableTest() {
        def mockActivity = mockFor(ClassActivity)
        def mockOccasion = mockFor(ActivityOccasion)

        mockOccasion.demand.isPast(1) { ->
            return false
        }

        mockOccasion.demand.isDeleted(1) { ->
            return false
        }

        mockActivity.demand.getOccasions(1) { ->
            return new HashSet<>([mockOccasion.createMock()])
        }

        assert !service.isDeletable(mockActivity.createMock())
        mockOccasion.verify()
        mockActivity.verify()
    }

    /**
     * This is the most relevant test where an activity has two occasions where one is past and
     * one is not. The method under test should iterate both occasions and on the second find that it
     * is not past and return false (e.g. not deleable). Hence the sorted TreeSet to make sure isPast() is
     * invoked on both mocked occasions.
     */
    @Test
    void isNotDeletableTwoOccasionsTest() {
        def mockActivity = mockFor(ClassActivity)
        def mockOccasion1 = mockFor(ActivityOccasion)
        def mockOccasion2 = mockFor(ActivityOccasion)

        mockOccasion1.demand.isPast(1) { ->
            return true
        }

        mockOccasion2.demand.isPast(1) { ->
            return false
        }

        mockOccasion2.demand.isDeleted(1..1) { ->
            return false
        }

        mockActivity.demand.getOccasions(1) { ->

            Comparator<ActivityOccasion> comparator = new Comparator<ActivityOccasion>() {
                @Override
                int compare(ActivityOccasion o1, ActivityOccasion o2) {
                    return 1
                }
            }

            Set occasions = new TreeSet(comparator)
            occasions.add(mockOccasion1.createMock())
            occasions.add(mockOccasion2.createMock())

            return occasions
        }

        assert !service.isDeletable(mockActivity.createMock())
        mockOccasion1.verify()
        mockOccasion2.verify()
        mockActivity.verify()
    }

    @Test
    void isDeletableWithUpcomingAndDeletedOccasionsTest() {
        def mockActivity = mockFor(ClassActivity)
        def mockOccasion = mockFor(ActivityOccasion)

        mockOccasion.demand.isPast(1) { ->
            return false
        }

        mockOccasion.demand.isDeleted(1) { ->
            return true
        }

        mockActivity.demand.getOccasions(1) { ->
            return new HashSet<>([mockOccasion.createMock()])
        }

        assert service.isDeletable(mockActivity.createMock())
        mockOccasion.verify()
        mockActivity.verify()
    }
}
