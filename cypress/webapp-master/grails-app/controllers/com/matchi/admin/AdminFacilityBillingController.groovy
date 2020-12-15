package com.matchi.admin


import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.facility.Organization
import org.joda.time.DateTime

class AdminFacilityBillingController extends GenericController {
    private static final int MONTHS = 12
    def fortnoxFacadeService

    def index() {
        def facility = Facility.get(params.id)

        if(facility == null) {
            render(view: "noFacility")
            return
        }

        def fortnoxInvoices = []
        fortnoxInvoices << [name: facility?.name, invoices: loadInvoices(facility?.fortnoxCustomerId)]

        Organization.findAllByFacility(facility).each {organization ->
            if (organization?.fortnoxCustomerId) {
                fortnoxInvoices << [name: organization?.name, invoices: loadInvoices(organization?.fortnoxCustomerId)]
            }
        }

        return [facility:facility, invoiceGroups: fortnoxInvoices]
    }

    def loadInvoices(fortnoxCustomerId) {
        if (fortnoxCustomerId) {
            def params = [sortby: "invoicedate",
                          sortorder: "descending",
                          customernumber: fortnoxCustomerId,
                          sent: true]

            // Don't include cancelled (Makulerade) invoices.
            fortnoxFacadeService.listMatchiInvoices(new DateTime().minusMonths(MONTHS).toDate(), params).findAll {fortnoxInvoice ->
                !fortnoxInvoice.getCancelled()
            }
        }
    }

    def preview() {
        String documentNumber = params.id
        response.contentType = 'application/pdf'
        response.setHeader("Content-Disposition","Attachment; filename=MATCHi-Invoice-${documentNumber}.pdf")
        response.getOutputStream().write(fortnoxFacadeService.previewMatchiInvoice(documentNumber))
        response.outputStream.flush()
        response.outputStream.close()
    }

}
