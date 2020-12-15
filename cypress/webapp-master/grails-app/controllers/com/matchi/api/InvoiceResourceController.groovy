package com.matchi.api

import com.matchi.InvoiceService
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.CompileStatic
import org.joda.time.LocalDate
import org.springframework.util.StopWatch

/**
 * @author Sergei Shushkevich
 */
@CompileStatic
class InvoiceResourceController extends GenericAPIController {

    InvoiceService invoiceService

    def show(Long id) {
        def invoice = invoiceService.getInvoice(id, requestFacility)
        if (invoice) {
            render invoice as JSON
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Invoice not found")
        }
    }

    def list(InvoiceListCommand cmd) {
        if (cmd.hasErrors()) {
            renderValidationErrors(cmd.errors)
            return
        }

        def max = Math.min(cmd.max ?: 50, 500)
        def offset = cmd.offset ?: 0

        def invoices = invoiceService.listInvoices(requestFacility, max, offset, cmd.from, cmd.to)

        render([meta: [max: max, offset: offset, total: invoices.totalCount],
                data: invoices] as JSON)
    }

    def updateInvoices() {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def cmds = requestJSONArray.collect {
            def cmd = new InvoiceUpdateCommand()
            bindData(cmd, it)
            cmd
        }
        cmds.each {InvoiceUpdateCommand cmd -> cmd.validate()}
        if (cmds.any {InvoiceUpdateCommand cmd -> cmd.hasErrors()}) {
            renderValidationErrors(cmds*.errors)
            return
        }

        def facility = requestFacility
        cmds.each { InvoiceUpdateCommand cmd ->
            invoiceService.updateInvoice(cmd, facility)
        }

        stopWatch.stop()
        log.info("Updated ${requestJSONArray.size()} invoices for ${facility?.name} in ${stopWatch.totalTimeMillis} ms.")

        render([:] as JSON)
    }
}

@Validateable(nullable = true)
class InvoiceListCommand {

    Integer max
    Integer offset
    LocalDate from
    LocalDate to

    static constraints = {
        max nullable: true
        offset nullable: true
        from nullable: true
        to nullable: true
    }
}

@Validateable(nullable = true)
class InvoiceUpdateCommand {

    Long id
    Long number
    Boolean booked
    Boolean cancelled
    Boolean credited
    Long creditInvoiceReference
    Integer balance
    LocalDate dueDate
    LocalDate paidDate
    Boolean sent

    static constraints = {
        id nullable: false
        number nullable: true
        booked nullable: true
        cancelled nullable: true
        credited nullable: true
        creditInvoiceReference nullable: true
        balance nullable: false
        dueDate nullable: true
        paidDate nullable: true
        sent nullable: true
    }
}