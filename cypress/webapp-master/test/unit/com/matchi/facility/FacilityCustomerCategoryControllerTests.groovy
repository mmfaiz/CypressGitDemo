package com.matchi.facility

import com.matchi.*
import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory
import com.matchi.SecurityService
import grails.test.GrailsMock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityCustomerCategoryController)
class FacilityCustomerCategoryControllerTests {

    User user
    Facility facility, facility2
    def mockSecurityService
    GrailsMock mockGroupService
    GrailsMock mockCustomerCategoryService

    @Before
    public void setUp() {
        facility = new Facility(id: 1, name: "Test", shortname: "test", lat: "1", lng: "1", openingHours: "07:00", closingHours: "10:00")
        facility.id = 1
        user = new User(id: 1, facility: facility, email: 'user@mail.com', firstname: "Test", lastname: "User")

        mockSecurityService = mockFor(SecurityService)
        controller.securityService = mockSecurityService.createMock()

        mockSecurityService.demand.getUserFacility(1..10) {  -> return facility }

        mockCustomerCategoryService = mockFor(CustomerCategoryService)
        mockGroupService = mockFor(GroupService)

        controller.customerCategoryService = mockCustomerCategoryService.createMock()
        controller.groupService = mockGroupService.createMock()
    }

    void testConfirmIfCustomerCategoryBelongsToPriceLists() {
        def customerCategory = new PriceListCustomerCategory()
        customerCategory.facility = facility
        customerCategory.prices = [new Price(priceCategory: new PriceListConditionCategory(pricelist: new PriceList()))]

        mockCustomerCategoryService.demand.getCustomerCategory(1..1) { id ->
            return customerCategory
        }

        controller.delete()
        assert view  == "/facilityCustomerCategory/confirmDelete"

    }

    void testDefaultCategoryNotDeletable() {
        def customerCategory = new PriceListCustomerCategory()
        customerCategory.defaultCategory = true
        customerCategory.facility = facility
        customerCategory.prices = [new Price(priceCategory: new PriceListConditionCategory(pricelist: new PriceList()))]

        mockCustomerCategoryService.demand.getCustomerCategory(1..1) { id ->
            return customerCategory
        }

        controller.delete()
        assert response.redirectedUrl  == "/facility/customercategories/index"
        assert flash.error.length() > 0

    }

    void testNoConfirmOnDelete() {
        def customerCategory = new PriceListCustomerCategory()
        customerCategory.facility = facility
        customerCategory.prices = []

        mockCustomerCategoryService.demand.getCustomerCategory(1..1) { id ->
            return customerCategory
        }

        // assert remove category
        mockCustomerCategoryService.demand.removeCustomerCategory(1..1) { category ->
        }

        controller.delete()
        assert response.redirectedUrl  == "/facility/customercategories/index"
        mockCustomerCategoryService.verify()

    }


}
