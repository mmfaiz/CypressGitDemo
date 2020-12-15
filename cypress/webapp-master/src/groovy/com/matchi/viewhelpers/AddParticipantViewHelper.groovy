package com.matchi.viewhelpers

import com.matchi.Customer
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.facility.FilterCustomerCommand
import grails.util.Holders
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class AddParticipantViewHelper extends ParticipantViewHelper {

    def customerService = Holders.grailsApplication.mainContext.getBean('customerService')

    @Override
    void entryAction(GrailsParameterMap params, def user, def flow) {
        List customers

        FilterCustomerCommand cmd = FilterCustomerCommand.buildFromParameters(params)

        def customerIds = params.allselected ?
                customerService.findCustomers(cmd, user.facility, false, true) :
                params.list('customerId')

        if(customerIds) {
            customers = customerIds.collect {
                Customer c = Customer.findById(it as Long)
                [name: c.fullName(), customerId: c.id]
            }
        } else {
            throw new IllegalArgumentException("facilityCourseParticipant.index.noneSelected")
        }

        flow.customers = customers
    }

    @Override
    int execute(def flow, CourseActivity course) {
        List<Customer> customers = getCustomersToChange(flow.customers)
        editParticipantService.addCustomersToCourse(customers, course)

        return customers.size()
    }
}
