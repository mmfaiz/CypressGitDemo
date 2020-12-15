package com.matchi.api_ext


import com.matchi.Facility
import com.matchi.api.Code
import grails.converters.JSON

class APIExtAccountingController extends APIExtGenericController {
    def invoiceService

    def invoices(Long facilityId, APIExtFilterCommand cmd) {
        Facility facility = getFacility(facilityId)
        def invoices = invoiceService.listInvoices(facility, cmd.max, cmd.offset, cmd.from, cmd.to)

        render([meta: [
                        max: cmd.max,
                        offset: cmd.offset,
                        total: invoices.totalCount],
                data: invoices] as JSON)
    }

    def invoice(Long facilityId, Long invoiceId) {
        Facility facility = getFacility(facilityId)
        def invoice = invoiceService.getInvoice(invoiceId, facility)
        if (invoice) {
            render invoice as JSON
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Invoice not found")
        }
    }

}
