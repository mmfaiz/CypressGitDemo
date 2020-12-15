package com.matchi.facility
import com.matchi.*
import com.matchi.invoice.InvoiceRow
import grails.validation.Validateable
import org.joda.time.DateMidnight
import org.joda.time.LocalDate

class FacilityInvoiceRowController extends GenericController {

    def invoiceService

    def index(FilterInvoiceRowCommand filter) {

        def facility = getUserFacility()
        def rows = invoiceService.getInvoiceRows(filter)

        [rows: rows, filter: filter, facility: facility]
    }

    def remove(FilterInvoiceRowCommand filter) {

        def rowIds

        if (filter.allselected) {
            rowIds = invoiceService.getInvoiceRows(filter).collect { it.id }
        } else {
            rowIds = params.list("rowIds")
        }

        rowIds.each {
            invoiceService.delete(InvoiceRow.get(it))
        }

        flash.message = message(code: "facilityInvoiceRow.remove.success")

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index")
        }
    }
}

@Validateable(nullable = true)
class FilterInvoiceRowCommand {
    String q
    int offset = 0
    int max = 100
    String order = "desc"
    String sort = "dateCreated"
    LocalDate start = new DateMidnight().minusMonths(1).withDayOfMonth(1).toLocalDate()
    LocalDate end = new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1).toLocalDate()
    boolean allselected = false
    List<Long> organizations = []
}

