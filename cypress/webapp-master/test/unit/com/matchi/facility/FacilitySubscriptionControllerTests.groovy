package com.matchi.facility

import com.matchi.*
import com.matchi.SecurityService
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(FacilitySubscriptionController)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Facility, Availability, Customer, User, Season, Court, Slot, Subscription, BookingGroup, Region, Municipality, Sport])
class FacilitySubscriptionControllerTests {

    def mockSlotService
    def mockSecurityService
    def mockSubscriptionService
    def mockSeasonService
    Facility facility
    User user
    Customer customer
    Season season

    @Before
    public void setUp() {
        MockUtils.mockLogging(FacilitySubscriptionController, true)

        mockSecurityService = mockFor(SecurityService)
        controller.securityService = mockSecurityService.createMock()

        mockSubscriptionService = mockFor(SubscriptionService)
        controller.subscriptionService = mockSubscriptionService.createMock()

        mockSeasonService = mockFor(SeasonService)
        controller.seasonService = mockSeasonService.createMock()

        mockSlotService = mockFor(SlotService)
        controller.slotService = mockSlotService.createMock()

        facility = new Facility(id: 1, name: "Test", shortname: "testfac", lat: 1, lng: 1).save(validate: false)
        user = new User(id: 1, facility: facility, email: 'user@mail.com', firstname: "Firstname", lastname: "Lastname").save(validate: false)
        new Court(id: 1l, facility: facility, listPosition: 1).save(validate: false)

        customer = new Customer(id: 1, number: 1, facility: facility, user: user).save(validate: false)

        season = new Season(name: "Testseason", facility: facility, startTime: new Date(), endTime: new DateTime().plusDays(10).toDate()).save(validate: false)

        mockSecurityService.demand.getUserFacility(1..2) {  -> return facility }

        mockSubscriptionService.demand.getSubscriptions(1..2) { -> return [] }

        mockSlotService.demand.getRecurrenceSlots(1..2) { DateTime fromDate, DateTime toDate, List<String> weekDays,
                        int frequency, int interval, List<Slot> recurrenceSlots, def onlyFreeSlots ->
            return []
        }

        mockSeasonService.demand.getAvailableSeasons(0..2) { Date date -> return [] }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testIndexReturnsCorrectFacility() {
        mockSeasonService.demand.getSeasonByDate(1..1) { Date date -> return season }
        mockSubscriptionService.demand.getSubscriptions(1..1) { Facility facility, def cmd ->
            return [ rows: [] ]
        }

        def model = controller.index()
        assert model.facility.name == facility.name
    }

    @Test
    public void testInitFormContainsAllData() {
        def model = controller.create()

        assert model.form.facility != null
        assert model.form.availableHours != null
        assert model.form.seasons != null
    }

    @Test
    public void testConfirmReturnsModel() {
        CreateSubscriptionCommand cmd = createCommand()

        mockSlotService.demand.sortSubscriptionSlots(1..1) { def slots, def command ->
            return []
        }

        def model = controller.confirm(cmd)

        assert model.cmd.customerId == cmd.customerId
        assert model.facility == facility
        assert model.slots == []
        assert model.sortedSlots == []
    }

    @Test
    void testChangeStatus() {
        def s = createSubscription()
        params.subscriptionId = s.id.toString()
        params.newStatus = Subscription.Status.CANCELLED.name()

        controller.changeStatus(new FacilitySubscriptionFilterCommand())

        assert Subscription.Status.CANCELLED == s.status
        assert "/facility/subscriptions/index?subscriptionId=1&newStatus=CANCELLED" == response.redirectedUrl
    }

    @Test
    void testChangeReminder() {
        def s = createSubscription()
        params.subscriptionId = s.id.toString()
        params.enabled = "false"

        controller.changeReminder(new FacilitySubscriptionFilterCommand())

        assert !s.reminderEnabled
        assert "/facility/subscriptions/index?subscriptionId=1&enabled=false" == response.redirectedUrl
    }

    void testCustomerAction() {
        def s = createSubscription()
        params.targetController = "facilityCustomerMessage"
        params.targetAction = "message"
        params.subscriptionId = s.id.toString()
        params.returnUrl = "url"

        controller.customerAction(new FacilitySubscriptionFilterCommand())

        assert "/facility/customers/message/message?returnUrl=url&customerId=${s.customer.id}" == response.redirectedUrl
    }

    private CreateSubscriptionCommand createCommand() {
        CreateSubscriptionCommand cmd = new CreateSubscriptionCommand()
        cmd.customerId = 1
        cmd.description = ""
        cmd.dateFrom = new DateTime().toString("yyyy-MM-dd")
        cmd.dateTo = new DateTime().plusDays(10).toString("yyyy-MM-dd")
        cmd.time = "08:00"
        cmd.courtId = 1l
        cmd.season = season.id
        cmd.interval = 1

        return cmd
    }
}
