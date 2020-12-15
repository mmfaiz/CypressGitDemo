package com.matchi.api_ext

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.api.Code
import com.matchi.facility.FilterCustomerCommand
import grails.converters.JSON

class APIExtCustomerController extends APIExtGenericController {
    def customerService

    def customers(Long facilityId, FilterCustomerCommand filter) {
        Facility facility = getFacility(facilityId)
        render customerService.findCustomers(filter, facility) as JSON
    }

    def customer(Long facilityId, Long customerId) {
        Facility facility = getFacility(facilityId)
        Customer customer = customerService.getCustomer(customerId, facility)

        if (customer) {
            render customer as JSON
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Customer not found")
        }
    }

}
