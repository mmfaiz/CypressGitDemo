package com.matchi.jobs

import com.matchi.Facility
import com.matchi.facility.Organization
import com.matchi.invoice.Invoice
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.springframework.util.StopWatch

/**
 * Periodic job that synchronizes Fortnox invoices with the matchi equivalent. Walk through all
 * facilities that has fortnox enabled and synchronizes the invoices.
 */
class FortnoxInvoiceJob {
    private static final int INVOICE_MAX_MONTHS = 6

    def fortnoxFacadeService
    def facilityService
    def invoiceService
    def externalSynchronizationService

    static triggers = {
        cron name: 'FortnoxInvoiceJob.trigger', cronExpression: "30 2 3 * * ?" // 03:02:30 am
    }

    def group = "FortnoxInvoiceJob"
    def sessionRequired = true

    /**
     * Synchronize fortnox information with Matchi information.
     * @return Nada
     */
    def execute() {
        log.info("Running Fortnox Invoice Job")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def facilities = Facility.findAllByActive(true).findAll() { it.hasFortnox() }

        // Facilities
        facilities.each { facility ->
            try {
                // Update articles for facility
                invoiceService.updateItems(facility?.id, fortnoxFacadeService.articleCommands(facility))

                // Process invoices for facility
                processFacilityInvoices(facility)
            } catch (Exception e) {
                log.error("Error while processing facility (${facility.name}) invoices: $e.message", e)
            }
        }

        // Organizations
        Organization.findAllByFortnoxAccessTokenIsNotNull()?.each{Organization organization ->
            try {
                Facility facility = organization?.facility

                // Update articles for organization
                invoiceService.updateItems(facility?.id, organization?.id, fortnoxFacadeService.articleCommands(facility, organization))

                // Process invoices for organization
                processOrganizationInvoices(organization)
            } catch (Exception e) {
                log.error("Error while processing organization (${organization.name}) invoices: $e.message", e)
            }
        }

        stopWatch.stop()
        log.info("Finished FortnoxInvoiceJob in ${stopWatch.totalTimeMillis} ms")
    }

    /**
     * Synchronizes all fortnox invoices on a given facility
     * @param facility
     */
    void processFacilityInvoices(Facility facility) {
        log.info("Begin processing ${facility?.name} facility invoices")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        //load all invoices to avoid additional api calls
        def fortnoxInvoices = fortnoxFacadeService.listInvoices(
                facility,
                new DateTime().minusMonths(Holders.config.matchi.fortnox.invoices.modifiedMonthsAgo).toDate())

        def fortnoxInvoicesById = fortnoxInvoices.groupBy { it.id }

        stopWatch.stop()
        log.info("Retrieved ${fortnoxInvoices.size()} invoices for ${facility?.name}. Timer: ${stopWatch.totalTimeMillis} ms.")

        stopWatch.start()

        // retrieve all invoices to be synchronized (that ar NOT organization invoices)
        def invoices = Invoice.createCriteria().listDistinct {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            notEqual("status", Invoice.InvoiceStatus.CANCELLED)
            ge("invoiceDate", new DateTime().minusMonths(INVOICE_MAX_MONTHS).toLocalDate())
            eq("c.facility", facility)
            isNull("organization")
        }

        stopWatch.stop()
        log.info("Retrieved ${invoices.size()} from database for ${facility?.name}. Timer: ${stopWatch.totalTimeMillis} ms.")

        stopWatch.start()

        log.info("Processing ${invoices.size()} invoices from ${facility.name}")
        invoices.each { invoice ->
            try {
                processFortnoxInvoice(invoice, fortnoxInvoicesById)
            } catch (Exception e) {
                log.error("Error while processing invoice for ${invoice.customer?.facility?.name}: id: ${invoice.id}, customer: ${invoice.customer?.fullName()}", e)
            }
        }

        stopWatch.stop()
        log.info("Finished processing invoices for ${facility?.name}. Timer: ${stopWatch.totalTimeMillis} ms.")
    }

    /**
     * Synchronizes all fortnox invoices on a given organization
     * @param organization
     */
    void processOrganizationInvoices(Organization organization) {
        log.info("Begin processing ${organization?.name} organization invoices")

        //load all invoices to avoid additional api calls
        def fortnoxInvoices = fortnoxFacadeService.listInvoicesForOrganization(
                organization, new DateTime().minusMonths(Holders.config.matchi.fortnox.invoices.modifiedMonthsAgo).toDate())

        def fortnoxInvoicesById = fortnoxInvoices.groupBy { it.id }

        // retrieve all invoices to be synchronized
        def invoices = Invoice.createCriteria().listDistinct {
            notEqual("status", Invoice.InvoiceStatus.CANCELLED)
            ge("invoiceDate", new DateTime().minusMonths(INVOICE_MAX_MONTHS).toLocalDate())
            eq("organization", organization)
        }

        log.info("Processing ${invoices.size()} invoices from ${organization.name}")

        invoices.each { invoice ->
            try {
                processFortnoxInvoice(invoice, fortnoxInvoicesById)
            } catch (Exception e) {
                log.error("Error while processing invoice for ${organization.name}: id: ${invoice.id}, customer: ${invoice.customer?.fullName()}", e)
            }

        }

    }

    /**
     * Synchronizes a fortnox invoice with a matchi invoice, it synchronizes:
     *  - statuses
     *  - final pay date
     *  - invoice print/emailed status
     *
     * @param invoice The invoice to synchronize
     * @param fortnoxInvoicesById All fortnox invoices grouped by id
     * @param fullyPaidFortnoxInvoicesById Work-around for bad fortnox API
     * @param authentication The fortnox authentication
     */
    private void processFortnoxInvoice(Invoice invoice, def fortnoxInvoicesById) {
        log.debug("processFortnoxInvoice with id: ${invoice?.id}")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def fortnoxInvoiceId = getFortnoxId(invoice)

        stopWatch.stop()
        log.debug("Retrieved fortnoxInvoiceId ${fortnoxInvoiceId} from invoice. Timer: ${stopWatch.totalTimeMillis} ms.")

        stopWatch.start()

        if (fortnoxInvoiceId) {
            def fortnoxInvoice = fortnoxInvoicesById.get(fortnoxInvoiceId)?.get(0)

            if (fortnoxInvoice) {
                log.info("Processing fortnox invoice with id ${fortnoxInvoiceId}")
                fortnoxFacadeService.processInvoiceStatus(invoice, fortnoxInvoice)
                fortnoxFacadeService.processInvoiceFinalPayDate(invoice, fortnoxInvoice)
                fortnoxFacadeService.processInvoiceOutputStatus(invoice, fortnoxInvoice)
                if (!invoice.number) {
                    invoice.number = Long.parseLong(fortnoxInvoice.id)
                }
                invoice.save(failOnError: true)
            }
        } else {
            log.warn "Matchi Invoice with id ${invoice?.id} for facility ${invoice?.customer?.facility?.name} and " +
                    "organization ${invoice?.organization?.name} not exists in Fortnox (at least not synched)"
        }

        stopWatch.stop()
        log.debug("Finished processFortnoxInvoice with id: ${invoice?.id}. Timer: ${stopWatch.totalTimeMillis} ms.")
    }

    def getFortnoxId(Invoice invoice) {
        externalSynchronizationService.getFortnoxInvoiceNumber(invoice)
    }
}
