package com.matchi.api

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class InvoiceResourceControllerIntegrationTests extends GroovyTestCase {

    def invoiceService
    def facilityService

    void testList() {
        def facility = createFacility()
        def invoice1 = createInvoice(createCustomer(facility))
        def invoice2 = createInvoice(createCustomer(facility))
        def invoice3 = createInvoice(createCustomer())
        def cmd = new InvoiceListCommand(max: 1)
        cmd.validate()
        def controller = new InvoiceResourceController(
                invoiceService: invoiceService, facilityService: facilityService)
        controller.request.facilityId = facility.id

        controller.list(cmd)

        assert controller.response.status == 200
        def json = controller.response.json
        assert json.meta.max == 1
        assert json.meta.offset == 0
        assert json.meta.total == 2
        assert json.data.size() == 1
        assert json.data[0].id == invoice1.id
    }
}