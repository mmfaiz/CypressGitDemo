package com.matchi

import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import grails.test.mixin.Mock
import grails.util.Holders
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*

class FortnoxResyncServiceTests extends GroovyTestCase {

    public final static String TEST_ACCESS_CODE = "d634b1bf-dd7e-4881-ad2c-a50beac97bf3"
    def externalSynchronizationService
    def fortnoxResyncService

    void testGetFortnoxFacilites() {
        Facility facility1 = getFortnoxFacility()
        Facility facility2 = createFacility()

        List result = fortnoxResyncService.getFortnoxFacilities()

        assert facility1.hasFortnox()
        assert !facility2.hasFortnox()
        assert result.size() == 1
        assert result.contains(facility1)
        assert !result.contains(facility2)
    }

    void testGetAllInvoicesForFacility() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))
        Invoice invoice3 = createInvoice(createCustomer(facility))
        Invoice invoice4 = createInvoice(createCustomer(facility))

        Organization organization = createOrganization(facility)
        organization.fortnoxAccessToken = '123124-asdfasf-435435'
        invoice3.organization = organization
        invoice3.save(flush: true, failOnError: true)

        invoice1.dateCreated = invoice2.dateCreated.plusDays(1)
        invoice4.dateCreated = invoice4.dateCreated.minusYears(1).minusDays(10)

        List result = fortnoxResyncService.getAllInvoicesForFacility(facility)
        assert result.size() == 2
        assert result[0].equals(invoice2)
        assert result[1].equals(invoice1)
        assert !result.contains(invoice3)
        assert !result.contains(invoice4)
    }

    void testGetAllInvoicesForOrganization() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))
        Invoice invoice3 = createInvoice(createCustomer(facility))
        Invoice invoice4 = createInvoice(createCustomer(facility))

        Organization organization = createOrganization(facility)
        organization.fortnoxAccessToken = '123124-asdfasf-435435'
        invoice1.organization = organization
        invoice1.dateCreated = invoice2.dateCreated.plusDays(1)
        invoice1.save(flush: true, failOnError: true)

        invoice2.organization = organization
        invoice2.save(flush: true, failOnError: true)

        invoice4.dateCreated = invoice4.dateCreated.minusYears(1).minusDays(10)
        invoice4.organization = organization
        invoice4.save(flush: true, failOnError: true)

        List result = fortnoxResyncService.getAllInvoicesForOrganization(organization)
        assert result.size() == 2
        assert result[0].equals(invoice2)
        assert result[1].equals(invoice1)
        assert !result.contains(invoice3)
        assert !result.contains(invoice4)
    }

    void testGetSyncEntitiesForInvoices() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))
        String instance = facility.getFortnoxAuthentication().db

        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(invoice1, instance)

        List result = fortnoxResyncService.getSyncEntitiesForInvoices([invoice1, invoice2], instance)
        assert result.size() == 1
        assert result[0].entityId == invoice1.id
    }

    void testFilterOutNonsyncedInvoices() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))

        String instance = facility.getFortnoxAuthentication().db

        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(invoice1, instance)

        List result = fortnoxResyncService.filterOutNonsyncedInvoices([invoice1, invoice2], instance)
        assert result.size() == 1
        assert result.contains(invoice2)
    }

    void testGetNonsyncedInvoicesFacility() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))

        String instance = facility.getFortnoxAuthentication().db
        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(invoice1, instance)

        List result = fortnoxResyncService.getNonsyncedInvoices(facility)
        assert result.size() == 1
        assert result.contains(invoice2)
    }

    void testGetNonsyncedInvoicesFacilityEmpty() {
        Facility facility = getFortnoxFacility()
        Invoice invoice1 = createInvoice(createCustomer(facility))
        Invoice invoice2 = createInvoice(createCustomer(facility))

        String instance = facility.getFortnoxAuthentication().db
        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(invoice1, instance)
        ExternalSynchronizationEntity ese2 = createExternalSynchronizationEntity(invoice2, instance)

        List result = fortnoxResyncService.getNonsyncedInvoices(facility)
        assert result.size() == 0
    }

    void testGetNonsyncedInvoicesOrganization() {
        Facility facility = getFortnoxFacility()

        Invoice invoice1 = createInvoice(createCustomer(facility))
        Organization organization = createOrganization(facility)
        organization.fortnoxAccessToken = '123124-asdfasf-435435'
        invoice1.organization = organization
        invoice1.save(flush: true, failOnError: true)

        Invoice invoice2 = createInvoice(createCustomer(facility))

        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(invoice1, organization.fortnoxAccessToken)

        List result = fortnoxResyncService.getNonsyncedInvoices(facility)
        assert result.size() == 1
        assert result.contains(invoice2)
    }

    void testGetCustomerSyncEntity() {
        Facility facility = getFortnoxFacility()
        Customer customer = createCustomer(facility)
        String instance = facility.getFortnoxAuthentication().db
        ExternalSynchronizationEntity ese = createExternalSynchronizationEntity(customer, instance)

        ExternalSynchronizationEntity result = fortnoxResyncService.getCustomerSyncEntity(customer, instance)
        assert result.equals(ese)
    }

    void testFindMatchingInvoices() {
        Facility facility = getFortnoxFacility()
        Customer customer = createCustomer(facility)
        User user = createUser()

        Invoice invoice1 = createInvoice(customer)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)

        Invoice invoice2 = createInvoice(customer)
        invoice2.expirationDate = invoice2.invoiceDate.plusDays(20)
        invoice2.rows = [
                new InvoiceRow(amount: 1, price: 200, invoice: invoice2, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]

        FortnoxInvoice fortnoxInvoice = createFortnoxInvoice()
        fortnoxInvoice.Total = 100
        fortnoxInvoice.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice.DueDate = invoice1.expirationDate.toDate()

        Invoice result = fortnoxResyncService.findMatchingInvoices(fortnoxInvoice, [invoice1, invoice2])
        assert result.equals(invoice1)

        Invoice result2 = fortnoxResyncService.findMatchingInvoices(fortnoxInvoice, [invoice2, invoice1])
        assert result2.equals(invoice1)
    }

    void testFindMatchingInvoicesCancelled() {
        Facility facility = getFortnoxFacility()
        Customer customer = createCustomer(facility)
        User user = createUser()

        Invoice invoice1 = createInvoice(customer)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)

        Invoice invoice2 = createInvoice(customer)
        invoice1.invoiceDate = new LocalDate()
        invoice2.expirationDate = invoice2.invoiceDate.plusDays(22)
        invoice2.rows = [
                new InvoiceRow(amount: 1, price: 200, invoice: invoice2, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]

        FortnoxInvoice fortnoxInvoice = createFortnoxInvoice()
        fortnoxInvoice.Total = 0
        fortnoxInvoice.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice.DueDate = invoice1.expirationDate.toDate()
        fortnoxInvoice.Cancelled = true

        Invoice result = fortnoxResyncService.findMatchingInvoices(fortnoxInvoice, [invoice1, invoice2])
        assert result.equals(invoice1)

        Invoice result2 = fortnoxResyncService.findMatchingInvoices(fortnoxInvoice, [invoice2, invoice1])
        assert result2.equals(invoice1)
    }

    void testSyncAndSave() {
        fortnoxResyncService.externalSynchronizationService = externalSynchronizationService

        Facility facility = getFortnoxFacility()
        String instance = facility.getFortnoxAuthentication().db
        Customer customer = createCustomer(facility)
        User user = createUser()

        Invoice invoice1 = createInvoice(customer)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)

        Invoice invoice2 = createInvoice(customer)
        invoice2.expirationDate = invoice2.invoiceDate.plusDays(20)
        invoice2.rows = [
                new InvoiceRow(amount: 1, price: 200, invoice: invoice2, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]

        FortnoxInvoice fortnoxInvoice = createFortnoxInvoice()
        fortnoxInvoice.Total = 100.00
        fortnoxInvoice.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice.DueDate = invoice1.expirationDate.toDate()

        fortnoxResyncService.syncAndSave(instance, [fortnoxInvoice], [invoice1, invoice2])
        assert invoice1.number == fortnoxInvoice.DocumentNumber.toLong()

        ExternalSynchronizationEntity ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice1.id, fortnoxInvoice.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db
    }

    void testSyncAndSaveCancelled() {
        fortnoxResyncService.externalSynchronizationService = externalSynchronizationService

        Facility facility = getFortnoxFacility()
        String instance = facility.getFortnoxAuthentication().db
        Customer customer = createCustomer(facility)
        User user = createUser()

        Invoice invoice1 = createInvoice(customer)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)

        Invoice invoice2 = createInvoice(customer)
        invoice2.expirationDate = invoice2.invoiceDate.plusDays(22)
        invoice2.rows = [
                new InvoiceRow(amount: 1, price: 200, invoice: invoice2, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]

        FortnoxInvoice fortnoxInvoice = createFortnoxInvoice()
        fortnoxInvoice.Total = 0
        fortnoxInvoice.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice.DueDate = invoice1.expirationDate.toDate()
        fortnoxInvoice.Cancelled = true

        fortnoxResyncService.syncAndSave(instance, [fortnoxInvoice], [invoice1, invoice2])
        assert invoice1.number == fortnoxInvoice.DocumentNumber.toLong()
        assert invoice1.status.equals(Invoice.InvoiceStatus.CANCELLED)

        ExternalSynchronizationEntity ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice1.id, fortnoxInvoice.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db
    }

    void testProcessFacility() {
        fortnoxResyncService.externalSynchronizationService = externalSynchronizationService

        Facility facility = getFortnoxFacility()
        User user = createUser()
        String instance = facility.getFortnoxAuthentication().db
        Customer customer1 = createCustomer(facility)
        createExternalSynchronizationEntity(customer1, instance)

        Customer customer2 = createCustomer(facility)
        createExternalSynchronizationEntity(customer2, instance)

        Invoice invoice1 = createInvoice(customer1)
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer1, createdBy: user).save(flush: true, failOnError: true)
        ]

        Invoice invoice2 = createInvoice(customer1)
        invoice2.expirationDate = invoice2.invoiceDate.plusDays(20)
        invoice2.rows = [
                new InvoiceRow(amount: 1, price: 130, invoice: invoice2, customer: customer1, createdBy: user).save(flush: true, failOnError: true)
        ]

        Invoice invoice3 = createInvoice(customer2)
        invoice3.expirationDate = invoice3.invoiceDate.plusDays(20)
        invoice3.rows = [
                new InvoiceRow(amount: 1, price: 150, invoice: invoice3, customer: customer2, createdBy: user).save(flush: true, failOnError: true)
        ]

        FortnoxInvoice fortnoxInvoice1 = createFortnoxInvoice()
        fortnoxInvoice1.Total = 100
        fortnoxInvoice1.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice1.DueDate = invoice1.expirationDate.toDate()

        FortnoxInvoice fortnoxInvoice2 = createFortnoxInvoice()
        fortnoxInvoice2.Total = 130
        fortnoxInvoice2.InvoiceDate = invoice2.invoiceDate.toDate()
        fortnoxInvoice2.DueDate = invoice2.expirationDate.toDate()

        FortnoxInvoice fortnoxInvoice3 = createFortnoxInvoice()
        fortnoxInvoice3.Total = 150
        fortnoxInvoice3.InvoiceDate = invoice3.invoiceDate.toDate()
        fortnoxInvoice3.DueDate = invoice3.expirationDate.toDate()

        fortnoxResyncService.fortnox3InvoiceService = new Object() {
            List getForCustomerExternalSynchronizationEntity(Facility f, ExternalSynchronizationEntity ese) {
                if(ese.entityId == customer1.id) {
                    return [fortnoxInvoice1, fortnoxInvoice2]
                } else if (ese.entityId == customer2.id) {
                    return [fortnoxInvoice3]
                }
            }
        }

        fortnoxResyncService.processFacility(facility)

        assert invoice1.number == fortnoxInvoice1.DocumentNumber.toLong()
        assert invoice2.number == fortnoxInvoice2.DocumentNumber.toLong()
        assert invoice3.number == fortnoxInvoice3.DocumentNumber.toLong()

        ExternalSynchronizationEntity ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice1.id, fortnoxInvoice1.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db

        ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice2.id, fortnoxInvoice2.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db

        ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice3.id, fortnoxInvoice3.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db
    }

    void testGetSyncEntitiesForFortnoxInvoices() {
        Facility facility1 = getFortnoxFacility()
        Facility facility2 = getFortnoxFacility("12345-12312-sdb-234ww")
        FortnoxInvoice fortnoxInvoice1 = createFortnoxInvoice()
        FortnoxInvoice fortnoxInvoice2 = createFortnoxInvoice()

        def config = Holders.config
        def tmpAccessTokenOverride = config.matchi.fortnox.api.v3.override?.accessToken
        config.matchi.fortnox.api.v3.override?.accessToken = null

        ExternalSynchronizationEntity ese1 = createExternalSynchronizationEntity(fortnoxInvoice1, facility1.getFortnoxAuthentication().db)
        ExternalSynchronizationEntity ese2 = createExternalSynchronizationEntity(fortnoxInvoice2, facility2.getFortnoxAuthentication().db)

        List result = fortnoxResyncService.getSyncEntitiesForFortnoxInvoices([fortnoxInvoice2, fortnoxInvoice1], facility1.getFortnoxAuthentication().db)
        config.matchi.fortnox.api.v3.override?.accessToken = tmpAccessTokenOverride

        assert result.size() == 1
        assert result.contains(ese1)
    }

    void testFilterAwaySyncedFortnoxInvoices() {
        Facility facility1 = getFortnoxFacility()
        FortnoxInvoice fortnoxInvoice1 = createFortnoxInvoice()
        FortnoxInvoice fortnoxInvoice2 = createFortnoxInvoice()
        ExternalSynchronizationEntity ese1 = createExternalSynchronizationEntity(fortnoxInvoice1, facility1.getFortnoxAuthentication().db)

        List result = fortnoxResyncService.filterAwaySyncedFortnoxInvoices([fortnoxInvoice1, fortnoxInvoice2], facility1.getFortnoxAuthentication().db)
        assert result.size() == 1
        assert result.contains(fortnoxInvoice2)
    }

    void testSaveInvoiceExternalSynchronizationEntity() {
        Facility facility = getFortnoxFacility()
        Customer customer = createCustomer(facility)
        User user = createUser()

        Invoice invoice1 = createInvoice(customer)
        invoice1.rows = [
                new InvoiceRow(amount: 1, price: 100, invoice: invoice1, customer: customer, createdBy: user).save(flush: true, failOnError: true)
        ]
        invoice1.expirationDate = invoice1.invoiceDate.plusDays(20)

        FortnoxInvoice fortnoxInvoice = createFortnoxInvoice()
        fortnoxInvoice.Balance = 100
        fortnoxInvoice.InvoiceDate = invoice1.invoiceDate.toDate()
        fortnoxInvoice.DueDate = invoice1.expirationDate.toDate()

        assert fortnoxInvoice.DocumentNumber.toLong() != invoice1.number

        fortnoxResyncService.externalSynchronizationService = externalSynchronizationService

        fortnoxResyncService.saveInvoiceExternalSynchronizationEntity(fortnoxInvoice, invoice1, facility.getFortnoxAuthentication().db)
        assert invoice1.number == fortnoxInvoice.DocumentNumber.toLong()

        ExternalSynchronizationEntity ese = ExternalSynchronizationEntity.findByEntityIdAndExternalEntityId(invoice1.id, fortnoxInvoice.id)
        assert ese != null
        assert ese.instance == facility.getFortnoxAuthentication().db
    }

    private getFortnoxFacility(String code) {
        Facility facility = createFacility()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.toString(), value: code ?: TEST_ACCESS_CODE, facility: facility)
        ]

        facility.save(flush: true, failOnError: true)
        return facility
    }
}
