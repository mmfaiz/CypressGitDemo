package com.matchi.facility

import com.matchi.Facility
import com.matchi.PriceList
import com.matchi.User
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory
import com.matchi.price.TimeBeforeBookingCondition
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import javax.servlet.http.HttpServletResponse

@TestFor(FacilityPriceListConditionController)
@Mock([PriceList, TimeBeforeBookingCondition, PriceListConditionCategory, PriceListCustomerCategory])
class FacilityPriceListConditionControllerTests {
    void setUp() {
        new PriceList(name: 'test').save(validate: false)

        def mockSecurityService = mockFor(SecurityService)
        controller.securityService = mockSecurityService.createMock()
        mockSecurityService.demand.getUserFacility(1..2) {  -> new Facility() }
    }

    void testAddTimeBeforeConditionCreatedCorrectly() {
        params.id = "1"
        params.type = "TIMEBEFORE"
        params.hours = "1"

        controller.add()

        assert HttpServletResponse.SC_FOUND == response.status
        assert '/facility/pricelist/conditions/form/1?categoryId=&hiddenCategoryName=' == response.redirectedUrl
        def condition = session.getAttribute(FacilityPriceListConditionController.PRICE_CONDITIONS_KEY).added.find {
            it.class == TimeBeforeBookingCondition
        }
        assert condition
        assert 1 == condition.hours

    }

    void testAddTimeBeforeConditionFailed() {
        params.id = "1"
        params.type = "TIMEBEFORE"
        params.minutes = "sds"

        controller.add()

        assert HttpServletResponse.SC_OK == response.status
        def condition = model.conditionBean
        assert condition
        assert 1 == condition.errors.errorCount
        assert "timeBeforeBooking.condition.anythingShouldBeSpecified" == condition.errors?.getAt('hours').code

    }
}
