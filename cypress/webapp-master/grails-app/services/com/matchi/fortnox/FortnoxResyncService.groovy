package com.matchi.fortnox

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.invoice.Invoice
import org.joda.time.DateTime

/**
 * Job to make sure all invoices in MATCHi have a connection to their corresponding invoice in Fortnox
 */
class FortnoxResyncService {

    static transactional = false
    def fortnox3InvoiceService
    def externalSynchronizationService
    def dateUtil

    int syncedCount = 0
    int totalCount = 0

    def startResyncInvoices(List fortnoxFacilities) {
        log.info "Starting FortnoxResyncService..."

        fortnoxFacilities.each { Facility facility ->
            processFacility(facility)

            if(facility.hasOrganization()) {
                processOrganization(facility.getOrganization())
            }
        }

        log.info "Synced ${syncedCount} out of ${totalCount} MATCHi invoices to Fortnox"
    }

    /**
     * Gets non synced invoices for facility and sends them to processInstance
     * @param facility
     */
    protected void processFacility(Facility facility) {
        List invoiceList = getNonsyncedInvoices(facility)
        log.info "${facility.id} has ${invoiceList.size()} non-synced invoices..."
        processInstance(invoiceList, facility.getFortnoxAuthentication().db) { ExternalSynchronizationEntity ese ->
            return fortnox3InvoiceService.getForCustomerExternalSynchronizationEntity(facility, ese)
        }
    }

    /**
     * Gets non synced invoices for organization and sends them to processInstance
     * @param facility
     */
    protected void processOrganization(Organization organization) {
        List invoiceList = getNonsyncedInvoices(organization)
        log.info "${organization.facility.id} with organization ${organization.id} has ${invoiceList.size()} non-synced invoices..."
        processInstance(invoiceList, organization.fortnoxAccessToken) { ExternalSynchronizationEntity ese ->
            return fortnox3InvoiceService.getForCustomerExternalSynchronizationEntity(organization, ese)
        }
    }

    /**
     * With the invoices of an instance, this method looks up fortnox invoices per customer and syncs them.
     * @param invoices
     * @param accessToken
     * @param callFortnox
     */
    private void processInstance(List invoices, String accessToken, Closure callFortnox) {
        totalCount += invoices.size()

        Map customerInvoices = invoices.groupBy() { Invoice i ->
            return i.customer
        }

        // Every customer's fortnox invoices will be fetched using the customer's external entity id
        customerInvoices.each { Customer customer, List invoicesPerCustomer ->
            ExternalSynchronizationEntity ese = getCustomerSyncEntity(customer, accessToken)

            if(ese) {
                // Get the fortnox invoices for this customer, from the facility
                log.info "ESE found for customer ${customer.fullName()}, syncing..."
                List fortnoxInvoices = callFortnox.call(ese)
                syncAndSave(accessToken, fortnoxInvoices, invoicesPerCustomer)
            } else {
                log.error "ESE NOT FOUND for customer ${customer.fullName()} with id ${customer.id}"
            }
        }
    }

    /**
     * Syncs invoices towards an instance of Fortnox
     * @param accessToken
     * @param fortnoxInvoices
     * @param customerInvoices
     */
    protected void syncAndSave(String accessToken, List fortnoxInvoices, List customerInvoices) {
        List nonsyncedFortnoxInvoices = filterAwaySyncedFortnoxInvoices(fortnoxInvoices, accessToken)

        nonsyncedFortnoxInvoices.each { FortnoxInvoice fortnoxInvoice ->
            log.info "Finding MATCHi invoice for Fortnox Invoice ${fortnoxInvoice.DocumentNumber}"
            log.info fortnoxInvoice.dump()
            Invoice invoice = findMatchingInvoices(fortnoxInvoice, customerInvoices)

            if(invoice) {
                log.info "Syncing Fortnox invoice ${fortnoxInvoice.id} with MATCHi invoice ${invoice.id}"
                saveInvoiceExternalSynchronizationEntity(fortnoxInvoice, invoice, accessToken)
                syncedCount++

                if(fortnoxInvoice.Cancelled && !invoice.status.equals(Invoice.InvoiceStatus.CANCELLED)) {
                    invoice.setStatus(Invoice.InvoiceStatus.CANCELLED)
                    invoice.save(flush: true)
                }
            } else {
                log.info "No MATCHi Invoice found for ${fortnoxInvoice.DocumentNumber}"
            }
        }
    }

    /**
     * Saves an ExternalSynchronizationEntity for an invoice, connecting it to a fortnox invoice.
     * @param fortnoxInvoice
     * @param invoice
     * @param accessToken
     */
    protected void saveInvoiceExternalSynchronizationEntity(FortnoxInvoice fortnoxInvoice, Invoice invoice, String accessToken) {
        externalSynchronizationService.markSynchronized(
                ExternalSynchronizationEntity.ExternalSystem.FORTNOX, accessToken,
                invoice, fortnoxInvoice.id)

        invoice.number = fortnoxInvoice.id.toLong()
        invoice.save(flush: true)
    }

    /**
     * Matches a fortnox invoice to MATCHi invoices.
     * Assumes that the list of MATCHi invoices are not synced with Fortnox
     * @param fortnoxInvoice
     * @param invoices
     * @return
     */
    protected Invoice findMatchingInvoices(FortnoxInvoice fortnoxInvoice, List invoices) {
        return invoices.find { Invoice invoice ->
            return invoice.expirationDate.toDate().equals(fortnoxInvoice.DueDate) &&
                    invoice.invoiceDate.toDate().equals(fortnoxInvoice.InvoiceDate) &&
                    (invoice.totalIncludingVAT.toInteger() == fortnoxInvoice.Total || fortnoxInvoice.Cancelled)
        }
    }

    /**
     * Takes a list of Fortnox invoices and a Fortnox instance id, returns those invoices not having an ESE.
     * @param fortnoxInvoices
     * @param instance
     * @return
     */
    protected List filterAwaySyncedFortnoxInvoices(List fortnoxInvoices, String instance) {
        List fortnoxSyncEntities = getSyncEntitiesForFortnoxInvoices(fortnoxInvoices, instance)
        List externalIds = fortnoxSyncEntities.collect { ExternalSynchronizationEntity ese -> ese.externalEntityId }
        return fortnoxInvoices.findAll { FortnoxInvoice fortnoxInvoice ->
            return !externalIds.contains(fortnoxInvoice.DocumentNumber)
        }
    }

    /**
     * Returns the ExternalSynchronizationEntity connecting the customer to Fortnox
     * @param customer
     * @return
     */
    protected ExternalSynchronizationEntity getCustomerSyncEntity(Customer customer, String instance) {
        return ExternalSynchronizationEntity.createCriteria().get() {
            eq("entity", ExternalSynchronizationEntity.LocalEntity.CUSTOMER)
            eq("externalSystem", ExternalSynchronizationEntity.ExternalSystem.FORTNOX)
            eq("entityId", customer.id)
            eq("instance", instance)
        }
    }

    /**
     * Return all facilities with Fortnox
     * @return
     */
    protected List getFortnoxFacilities() {
        return Facility.all.findAll { Facility f ->
            return f.hasFortnox()
        }.unique { Facility f ->
            return f.id
        }
    }

    /**
     * Returns a list of Invoices with and ESE for a facility
     * @param facility
     * @return
     */
    protected List getNonsyncedInvoices(Facility f) {
        List facilityInvoices = getAllInvoicesForFacility(f)
        return filterOutNonsyncedInvoices(facilityInvoices, f.getFortnoxAuthentication().db)
    }

    /**
     * Returns a list of Invoices with and ESE for several facilities
     * @param facilities
     * @return
     */
    protected List getNonsyncedInvoices(Organization o) {
        List organizationInvoices = getAllInvoicesForOrganization(o)
        return filterOutNonsyncedInvoices(organizationInvoices, o.fortnoxAccessToken)
    }

    /**
     * Returns a list of Invoice objects not having an ExternalSynchronizationEntity
     * @param f
     * @param invoices
     * @return
     */
    protected List filterOutNonsyncedInvoices(List invoices, String instance) {
        if(invoices.isEmpty()) return []

        List externalSynchronizationEntities = getSyncEntitiesForInvoices(invoices, instance)
        List eseIds = externalSynchronizationEntities.collect { ExternalSynchronizationEntity ese -> ese.entityId }

        return invoices.findAll { Invoice invoice ->
            return !eseIds.contains(invoice.id)
        }
    }

    /**
     * Returns all invoices for a facility
     * @param f
     * @return
     */
    protected List getAllInvoicesForFacility(Facility f) {
        return Invoice.createCriteria().list {
            createAlias("customer", "c")
            eq("c.facility", f)
            gte("dateCreated", dateUtil.beginningOfYear(new DateTime()))
            order("dateCreated")
            isNull("organization")
        }
    }

    /**
     * Returns all invoices for a facility
     * @param f
     * @return
     */
    protected List getAllInvoicesForOrganization(Organization o) {
        return Invoice.createCriteria().list {
            eq("organization", o)
            gte("dateCreated", dateUtil.beginningOfYear(new DateTime()))
            order("dateCreated")
        }
    }

    /**
     * Returns a list of ExternalSynchronizationEntity matching a list of invoices
     * @param invoices
     * @return
     */
    protected List getSyncEntitiesForInvoices(List invoices, String instance) {
        List invoiceIds = invoices?.collect { Invoice invoice -> invoice.id }
        return ExternalSynchronizationEntity.createCriteria().list {
            eq("entity", ExternalSynchronizationEntity.LocalEntity.INVOICE)
            eq("externalSystem", ExternalSynchronizationEntity.ExternalSystem.FORTNOX)
            inList("entityId", invoiceIds)
            eq("instance", instance)
        }
    }

    /**
     * Returns if FortnoxInvoice has a corresponding ExternalSynchronizationEntity
     * @return
     */
    protected List getSyncEntitiesForFortnoxInvoices(List invoices, String instance) {
        List documentNumbers = invoices?.collect { FortnoxInvoice fortnoxInvoice -> fortnoxInvoice.DocumentNumber }
        if(documentNumbers.isEmpty()) return []
        return ExternalSynchronizationEntity.createCriteria().list() {
            eq("entity", ExternalSynchronizationEntity.LocalEntity.INVOICE)
            eq("externalSystem", ExternalSynchronizationEntity.ExternalSystem.FORTNOX)
            inList("externalEntityId", documentNumbers)
            eq("instance", instance)
        }
    }

}
