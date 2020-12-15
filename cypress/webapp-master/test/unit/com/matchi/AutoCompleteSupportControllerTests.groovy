package com.matchi

import com.matchi.test.domain.UserMother
import grails.converters.deep.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(AutoCompleteSupportController)
@Mock([ User, Customer, Facility ])
class AutoCompleteSupportControllerTests {


    def springSecurityMockController
    def userServiceMockController
    def customerServiceMockController
    def priceListServiceMockController
    def couponServiceMockController

    def user
    def facility
    def customer
    def autoCompleteQueryCommand

    def userMother = new UserMother()

    @Before
    public void setUp() {
        springSecurityMockController = mockFor(SpringSecurityService)
        userServiceMockController = mockFor(UserService)
        customerServiceMockController = mockFor(CustomerService)
        priceListServiceMockController = mockFor(PriceListService)
        couponServiceMockController = mockFor(CouponService)

        controller.springSecurityService = springSecurityMockController.createMock()
        controller.userService = userServiceMockController.createMock()
        controller.priceListService = priceListServiceMockController.createMock()
        controller.couponService = couponServiceMockController.createMock()

        facility = new Facility(id: 1)

        user = new User()
        user.firstname = "Test"
        user.lastname = "Testsson"
        user.facility = facility
        user.facility.name = "Tennis TK"
        user.save(validate: false)

        customer = new Customer()
        customer.firstname = "Kundtest"
        customer.lastname  = "Kundtestsson"
        customer.facility = facility
        customer.save(validate: false)

        autoCompleteQueryCommand = new AutoCompleteQueryCommand()
        autoCompleteQueryCommand.query = "test"

        priceListServiceMockController.demand.getBookingPrice(1..1) { def slot, def user ->
            return 100
        }
        couponServiceMockController.demand.getValidCouponsByUserAndFacility(1..1) { def user, def facility ->
        }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testErrorMessageWhenNotLoggedIn() {
        springSecurityMockController.demand.getCurrentUser(1..1) { ->
            return null
        }
        controller.userOnEmail(autoCompleteQueryCommand)

        def result = jsonResult()
        assert result.type == "ERROR"
    }

    @Test
    public void testErrorMessageWhenNoFacility() {
        springSecurityMockController.demand.getCurrentUser(1..1) { ->
            user.facility = null
            return user
        }

        controller.userOnEmail(autoCompleteQueryCommand)

        def result = jsonResult()
        assert result.type == "ERROR"
    }



    def jsonResult() {
        return JSON.parse(controller.response.contentAsString)
    }

    def _createCustomer() {
        def customer = new Customer()
        customer.facility = facility
        customer.save(validate: false)

        return customer
    }

}
