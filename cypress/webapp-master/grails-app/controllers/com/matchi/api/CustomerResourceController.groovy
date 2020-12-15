package com.matchi.api

import com.matchi.CustomerService
import grails.converters.JSON
import groovy.transform.CompileStatic

/**
 * @author Sergei Shushkevich
 */
@CompileStatic
class CustomerResourceController extends GenericAPIController {

    CustomerService customerService

    def show(Long id) {
        def customer = customerService.getCustomer(id, requestFacility)
        if (customer) {
            render customer as JSON
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Customer not found")
        }
    }
}