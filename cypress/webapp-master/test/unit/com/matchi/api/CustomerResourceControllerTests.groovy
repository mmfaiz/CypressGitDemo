package com.matchi.api

import com.matchi.Customer
import com.matchi.CustomerService
import com.matchi.Facility
import com.matchi.FacilityService
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.marshallers.CustomerMarshaller
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(CustomerResourceController)
@Mock([Customer, CustomerService, Facility, Municipality, Region])
class CustomerResourceControllerTests {

    def facility
    def customer

    @Before
    void setUp() {
        facility = createFacility()
        customer = createCustomer(facility, "jdoe@matchi.com")
        def facilityService = mockFor(FacilityService)
        facilityService.demand.getFacility { fid -> facility }
        def customerService = mockFor(CustomerService)
        customerService.demand.getCustomer { cid, requestFacility -> customer }
        controller.facilityService = facilityService.createMock()
        controller.customerService = customerService.createMock()
    }

    void testShow() {
        new CustomerMarshaller().register()

        controller.show(customer.id)

        assert response.status == 200
        def json = response.json
        assert json.size() == 23
        assert json.id == customer.id
        assert json.name == "John Doe"
        assert json.email == "jdoe@matchi.com"
        assert json.type == "PRIVATE"
    }

    void testShowNotFound() {
        controller.show(createCustomer().id)

        assert response.status == 404
    }
}