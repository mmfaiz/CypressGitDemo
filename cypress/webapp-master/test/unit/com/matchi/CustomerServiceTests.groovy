package com.matchi

import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.facility.FilterCustomerCommand
import com.matchi.fortnox.FortnoxFacadeService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.springframework.context.MessageSource

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.mockCriteria

@TestFor(CustomerService)
@Mock([ Customer, User, Facility, Region, Municipality, Participant, Form, CourseActivity ])
class CustomerServiceTests {

    def mockUserService
    def mockGroupService
    def mockFortnoxFacadeService
    def mockExternalSynchronizationService
    def mockNotificationService
    def mockMessageSource
    def mockCustomerService

    Facility facility
    User user
    Customer customer

    String removedFirstname = "firstname"
    String removedLastname = "lastname"

    @Before
    void setUp() {
        mockUserService = mockFor(UserService)
        mockCustomerService = mockFor(CustomerService)
        mockGroupService = mockFor(GroupService)
        mockFortnoxFacadeService = mockFor(FortnoxFacadeService)
        mockExternalSynchronizationService = mockFor(ExternalSynchronizationService)
        mockNotificationService = mockFor(NotificationService)
        mockMessageSource = mockFor(MessageSource)

        facility = createFacility()
        user     = createUser()
        customer = createCustomer(facility)

        mockUserService.demand.getUserFacility(1..1) { -> return facility }

        service.userService = mockUserService.createMock()
        service.groupService = mockGroupService.createMock()
        service.fortnoxFacadeService = mockFortnoxFacadeService.createMock()
        service.externalSynchronizationService = mockExternalSynchronizationService.createMock()
        service.notificationService = mockNotificationService.createMock()
        service.messageSource = mockMessageSource.createMock()

        CourseActivity.metaClass.static.withCriteria = { Map obj, Closure cls -> 1 }
    }

    // Following tests are disabled due to:
    // http://jira.grails.org/browse/GRAILS-8841

    /*
    @Test
    void testCreateCustomerCreatesNewCustomerIfNoMatch() {
        CreateCustomerCommand cmd = _createCommand()

        def nrOfCustomers = facility.customers.size()

        service.createCustomer(cmd)

        assert nrOfCustomers + 1 == facility.customers.size()
    }


    @Test
    void testCreateCustomerDoesNotCreateIfMatchingCustomer() {
        CreateCustomerCommand cmd = _createCommand()
        cmd.email = customer.email
        cmd.firstname = customer.firstname
        cmd.lastname = customer.lastname

        def nrOfCustomers = facility.customers.size()

        service.createCustomer(cmd)

        assert nrOfCustomers == facility.customers.size()
    }

    @Test
    void testLinkToUserWithSameId() {
        service.linkCustomerToUser(customer, user)

        assert customer.user == user
    }

    @Test
    void tesGetUserCustomerReturnsCustomerIfFound() {
        service.linkCustomerToUser(customer, user)

        assert service.findUserCustomer(user, facility) == customer
    }

    @Test
    void tesGetUserCustomerReturnsNullIfNoCustomerFound() {
        user.email = "newEmail@matchi.se"
        assert !service.findUserCustomer(user, facility)
    }

    @Test
    void testGetOrCreateUserCustomerCreatesNewCustomerIfNoUserCustomerExistsOrMatchesUser() {
        def nrOfCustomers = facility.customers.size()

        user.email = "newEmail@matchi.se"
        def customer = service.getOrCreateUserCustomer(user, facility)
        assert nrOfCustomers + 1 == facility.customers.size()
        assert customer.user == user
    }


    @Test
    void testVerifyUserCustomerLinksUserToCustomerIfCustomerMatches() {
        def nrOfCustomers = facility.customers.size()

        customer.email = user.email
        customer.firstname = user.firstname
        customer.lastname = user.lastname

        def customer = service.getOrCreateUserCustomer(user, facility)
        assert customer.user == user
        assert nrOfCustomers == facility.customers.size()
    }
    */

    /*@Test
    void testFindCustomerDoesNotReturnDeletedCustomer() {
        FilterCustomerCommand filter = new FilterCustomerCommand()
        mockCriteria([Customer])

        mockMessageSource.demand.getMessage(1) { code, args, locale -> }

        assert service.findCustomers(filter, facility, false, true).any()

        customer.deleted = new Date()
        assert !service.findCustomers(filter, facility).any()
    }*/

    @Test
    void testLinkCustomerToUserIfUserIsNull() {
        assert !service.linkCustomerToUser(customer, null)
    }

    void testDisableClubMessages() {
        def customer = createCustomer()
        service.disableClubMessages(customer)
        assert customer.clubMessagesDisabled
    }
    @Test
    void testRemoveCustomer() {
        boolean first = true
        mockMessageSource.demand.getMessage(2) { code, args, locale ->
            String toReturn = first ? removedFirstname : removedLastname
            first = false
            return toReturn

        }

        createCourseParticipant(customer)
        assert customer.courseParticipants.size() == 1

        service.clearCustomer(customer)
        assert customer.courseParticipants == null
        assert customer.number == null

        assert removedFirstname == customer.firstname
        assert removedLastname == customer.lastname
    }

    @Test
    void testCollectPlayers() {
        def facility = createFacility()
        def customer1 = createCustomer(facility)
        def customer2 = createCustomer(facility)

        assert 3 == service.collectPlayers([customer1.id, customer2.id], 1).size()
        assert 1 == service.collectPlayers([], 1).size()
        assert !service.collectPlayers([], null)
    }

    void testGetCustomer() {
        def facility1 = createFacility()
        def facility2 = createFacility()
        def customer1 = createCustomer(facility1)
        def customer2 = createCustomer(facility2)

        assert service.getCustomer(customer1.id, facility1)
        assert !service.getCustomer(customer1.id, facility2)
        assert service.getCustomer(customer2.id, facility2)
        assert !service.getCustomer(customer2.id, facility1)
        assert !service.getCustomer(12345L, facility1)
    }

    void testGetCurrentCustomer() {
        def facility = createFacility()
        def user = createUser("jane@matchi.com")
        def customer = createCustomer(facility,
                user.email, user.firstname, user.lastname, null, user)
        def userService = mockFor(UserService)
        userService.demand.getLoggedInUser { -> user }
        service.userService = userService.createMock()

        def result = service.getCurrentCustomer(facility)

        assert result == customer
        userService.verify()
    }

    void testUpdateCustomersEmail() {
        def facility1 = createFacility()
        def facility2 = createFacility()
        def facility3 = createFacility()
        def user1 = createUser("jane@matchi.com")
        def user2 = createUser("paul@matchi.com")
        def customer1 = createCustomer(facility1,
                user1.email, user1.firstname, user1.lastname, null, user1)
        def customer2 = createCustomer(facility2,
                user1.email, user1.firstname, user1.lastname, null, user1)
        def customer3 = createCustomer(facility3,
                user2.email, user2.firstname, user2.lastname, null, user2)
        user1.email = "changed@matchi.com"
        user1.save(failOnError: true, flush: true)

        service.updateCustomersEmail(user1)

        assert customer1.email == "changed@matchi.com"
        assert customer2.email == "changed@matchi.com"
        assert customer3.email == "paul@matchi.com"
    }

    def _createCommand() {
        CreateCustomerCommand cmd = new CreateCustomerCommand()
        cmd.facilityId = 1
        cmd.number = 2
        cmd.type = Customer.CustomerType.MALE
        cmd.email = "email@matchi.se"
        cmd.firstname = "Firstname"
        cmd.lastname = "Lastname"
        cmd.companyname = "Company INC"

        return cmd
    }
}
