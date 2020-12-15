package com.matchi.api

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.api.v2.RetrieveFreeSlotsCommand
import com.matchi.api.v2.SlotResourceController
import org.joda.time.DateTime
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*

class SlotResourceControllerIntegrationTests extends GroovyTestCase {

    def courtService
    def slotService

    void testList() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        setupListTest(controller, facility)

        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 2 == controller.response.json[0].courts[0].slots.size()
    }

    void testList_MaxBookings() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        def slot1 = setupListTest(controller, facility)
        def user = createUser()
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        createBooking(customer, slot1)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService


        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 0 == controller.response.json[0].courts[0].slots.size()
        assert SlotResourceController.ErrorCode.LIMIT_REACHED.toString() == controller.response.json[0].unavailableCode
    }

    void testList_TooSoo() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        def slot1 = setupListTest(controller, facility)
        def user = createUser()
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService


        controller.params.from = new LocalDate().plusDays(30)
        controller.params.to = new LocalDate().plusDays(31)

        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 0 == controller.response.json[0].courts[0].slots.size()
        assert SlotResourceController.ErrorCode.TOO_SOON.toString() == controller.response.json[0].unavailableCode
    }

    void testList_NotBookable() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        facility.bookable = false
        facility.save(failOnError: true)
        setupListTest(controller, facility)
        def user = createUser()
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService


        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 0 == controller.response.json[0].courts[0].slots.size()
        assert SlotResourceController.ErrorCode.NOT_BOOKABLE.toString() == controller.response.json[0].unavailableCode
    }

    void testList_MaxBookings_NoUser() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        def slot1 = setupListTest(controller, facility)
        def user = createUser()
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        createBooking(customer, slot1)

        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 1 == controller.response.json[0].courts[0].slots.size()
    }

    void testList_MaxBookings_ExcludedCustomer() {
        def controller = new SlotResourceController(courtService: courtService, slotService: slotService)
        def facility = createFacility()
        def slot1 = setupListTest(controller, facility)
        def user = createUser()
        def customer = createCustomer(facility)
        customer.user = user
        customer.exludeFromNumberOfBookingsRule = true
        customer.save(failOnError: true)
        createBooking(customer, slot1)
        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService


        def facilityIds = [facility.id]
        controller.list(new RetrieveFreeSlotsCommand(facilityIds: facilityIds))

        assert 200 == controller.response.status
        assert 1 == controller.response.json.size()
        assert facility.id == controller.response.json[0].id
        assert facility.name == controller.response.json[0].name
        assert 1 == controller.response.json[0].courts.size()
        assert 1 == controller.response.json[0].courts[0].slots.size()
    }

    private def setupListTest(controller, facility) {
        facility.setFacilityProperty(FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER, "1")
        facility.setFacilityProperty(FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER, "1")
        def court = createCourt(facility)
        def slot1 = createSlot(court, new DateTime().plusHours(1).toDate(),
                new DateTime().plusHours(2).toDate())
        def slot2 = createSlot(court, new DateTime().plusHours(2).toDate(),
                new DateTime().plusHours(3).toDate())
        controller.params.facilityIds = facility.id
        controller.params.from = new LocalDate()
        controller.params.to = new LocalDate().plusDays(1)
        slot1
    }
}