package com.matchi.api

import com.matchi.ActivityService
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.PaymentService
import com.matchi.User
import com.matchi.activities.*
import com.matchi.orders.Order
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createUser

@TestFor(ClassActivitySearchResourceController)
@Mock([ActivityOccasion, User, Customer, Activity, Facility])
class ClassActivitySearchResourceControllerTest {
    def mockActivityService
    def mockPaymentService

    @Before
    void setUp() {
        mockActivityService = mockFor(ActivityService)
        mockPaymentService = mockFor(PaymentService)

        controller.activityService = mockActivityService.createMock()
        controller.paymentService = mockPaymentService.createMock()
    }

    @Test
    void invalidPaymentMethod() {
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"INVALUD", "couponId":"1"} ,"userMessage":"","acceptTerms": true}'
        User user = createUser()
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService

        controller.createActivityBooking()

        assert response.status == 400
        assert response.contentAsString == '{"status":400,"code":"INPUT_ERROR","message":"Invalid payment information"}'
    }

    @Test
    void occasionNotFound() {
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"CREDIT_CARD_RECUR", "couponId":"1"} ,"userMessage":"","acceptTerms": true}'
        User user = createUser()
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService

        controller.createActivityBooking()

        assert response.status == 400 // TODO 404 would be more correct?
        assert response.contentAsString == '{"status":400,"code":"RESOURCE_NOT_FOUND","message":"Could not locate activity (1)"}'
    }

    @Test
    void alreadyParticipating() {
        Customer customer = new Customer(id: 1)
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"CREDIT_CARD_RECUR", "couponId":"1"} ,"userMessage":"","acceptTerms": true}'
        User user = createUser()
        GrailsMock mockActivityOccasion = mockFor(ActivityOccasion)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService
        mockActivityOccasion.demand.static.get() { Long id ->
            new ActivityOccasion(
                    activity: new ClassActivity(facility: new Facility()),
                    participations: new Participation(customer: customer)
            )
        }
        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.static.findByUserAndFacility() { User u, Facility facility -> customer }

        controller.createActivityBooking()
        assert response.status == HttpStatus.SC_CONFLICT
        assert response.contentAsString == '{"status":409,"code":"INPUT_ERROR","message":"You have been assigned to this activity already"}'
    }

    @Test
    void notAcceptedTerms() {
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"CREDIT_CARD_RECUR", "couponId":"1"} ,"userMessage":"","acceptTerms": false}'
        User user = createUser()
        GrailsMock mockActivityOccasion = mockFor(ActivityOccasion)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService

        mockActivityOccasion.demand.static.get() { Long id ->
            new ActivityOccasion(
                    activity: new ClassActivity(facility: new Facility(), terms: "TERMS"),
                    participations: new Participation(customer: new Customer(id: 2))
            )
        }
        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.static.findByUserAndFacility() { User u, Facility facility -> null }

        controller.createActivityBooking()
        assert response.status == 400
        assert response.contentAsString == '{"status":400,"code":"INPUT_ERROR","message":"You must accept terms of use"}'
    }

    @Test
    void nonTermActivity() {
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"CREDIT_CARD_RECUR", "couponId":"1"} ,"userMessage":"","acceptTerms": true}'
        User user = createUser()
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService

        GrailsMock mockActivityOccasion = mockFor(ActivityOccasion)

        mockActivityOccasion.demand.static.get() { Long id ->
            new ActivityOccasion(
                    activity: new EventActivity(facility: new Facility()),
                    participations: new Participation(customer: new Customer(id: 2))
            )
        }
        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.static.findByUserAndFacility() { User u, Facility facility -> null }

        controller.createActivityBooking()
        assert response.status == 400
        assert response.contentAsString == '{"status":400,"code":"INPUT_ERROR","message":"This activity is full"}'
    }

    @Test
    void order() {
        request.JSON = '{"activityOccasionId":1,  "payment": {"method":"CREDIT_CARD_RECUR", "couponId":"1"} ,"userMessage":"","acceptTerms": true}'
        User user = createUser()
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService

        GrailsMock mockActivityOccasion = mockFor(ActivityOccasion)

        mockActivityOccasion.demand.static.get(2) { Long id ->
            new ActivityOccasion(
                    activity: new ClassActivity(facility: new Facility(), terms: "TERMS"),
                    participations: new Participation(customer: new Customer(id: 2)),
                    maxNumParticipants: 2
            )
        }
        GrailsMock mockCustomer = mockFor(Customer)

        mockActivityService.demand.createActivityPaymentOrder() { User u, ActivityOccasion o, String origin ->
            def order = new Order()
            order.metadata = [activityOccasionId: 1L]
            order
           }
        mockActivityService.demand.book() { Order order -> }
        mockCustomer.demand.static.findByUserAndFacility() { User u, Facility facility -> null }

        mockPaymentService.demand.handleCreditCardPayment() {Order o, User u -> }

        controller.createActivityBooking()
        assert response.status == 200
    }
}