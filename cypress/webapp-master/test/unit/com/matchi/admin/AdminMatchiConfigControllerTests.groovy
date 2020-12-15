package com.matchi.admin

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.FacilityUserRole
import com.matchi.MatchiConfig
import com.matchi.MatchiConfigMethodAvailability
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.UserService
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdminMatchiConfigController)
@Mock([MatchiConfigMethodAvailability, MatchiConfig, Facility, Form, Municipality, Order, Region, Submission, User, FacilityUserRole, Customer])
class AdminMatchiConfigControllerTests {
    AdminMatchiConfigController mockController() {
        def mockUserService = mockFor(UserService)
        AdminMatchiConfigController controller = new AdminMatchiConfigController()
        controller.userService = mockUserService.createMock()

        mockUserService.demand.getLoggedInUser(1..4) { ->
            return new User()
        }

        return controller
    }

    void testUpdate() {
        AdminMatchiConfigController controller = mockController()

        def params = [
                DISABLE_DEVIATION            : "no",
                DISABLE_SUBSCRIPTIONS        : 'no',
                DISABLE_FACILITY_STATISTICS  : 'no',
                DISABLE_FACILITY_BOOKING_LIST: 'no',
                MINIMUM_APP_VERSION          : '1.0.0'
        ]
        controller.metaClass.getParams = { -> params }
        controller.update()

        assert flash.error == null
    }
    void testUpdate1() {
        AdminMatchiConfigController controller = mockController()

        def params = [
                DISABLE_DEVIATION          : "no",
                DISABLE_SUBSCRIPTIONS      : 'no',
                DISABLE_FACILITY_STATISTICS: 'no',
                MINIMUM_APP_VERSION        : '1.0.0'
        ]
        controller.metaClass.getParams = { -> params }
        controller.update()

        assert flash.error != null
    }
    void testUpdate2() {
        AdminMatchiConfigController controller = mockController()

        def params = [
                DISABLE_DEVIATION:"no",
                DISABLE_SUBSCRIPTIONS: 'no',
                DISABLE_FACILITY_STATISTICS: 'no',
                DISABLE_FACILITY_BOOKING_LIST: 'not a possible value',
                MINIMUM_APP_VERSION: '1.0.0'
        ]
        controller.metaClass.getParams = { -> params }
        controller.update()

        assert flash.error != null
    }
}
