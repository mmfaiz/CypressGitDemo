package com.matchi.fortnox.v3

import com.matchi.Customer
import com.matchi.ExternalSynchronizationService
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.fortnox.FortnoxFacadeService
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Ignore


/**
 * @author Michael Astreiko
 */
@TestFor(FortnoxFacadeService)
@Mock([Facility, Customer, ExternalSynchronizationEntity, FacilityProperty, Invoice, InvoiceRow])
class FortnoxFacadeServiceTest extends Fortnox3CommonTest {

    void setUp() {
        super.setUp()
        def fortnox3Service = new Fortnox3Service()
        fortnox3Service.permitsPerSecond = 3.0d
        def fortnox3CustomerService = new Fortnox3CustomerService()
        fortnox3CustomerService.fortnox3Service = fortnox3Service
        def fortnox3InvoiceService = new Fortnox3InvoiceService()
        fortnox3InvoiceService.fortnox3Service = fortnox3Service
        service.fortnox3CustomerService = fortnox3CustomerService
        service.fortnox3InvoiceService = fortnox3InvoiceService
        service.externalSynchronizationService = [markSynchronized: { a, b, c, d ->
            new ExternalSynchronizationEntity(externalEntityId: "100", entity: ExternalSynchronizationEntity.LocalEntity.CUSTOMER).save(validate: false)
        }, getFortnoxCustomerNumber:{a, b = null -> "100"}]
    }

    void testSaveMatchiCustomerToFortnox() {
        def customer = new Customer(firstname: "First", lastname: "Last", email: "customer@test.com",
                telephone: "031-212121", facility: facility).save(validate: false)

        //save
        FortnoxCustomer fortnoxCustomer = service.saveMatchiCustomerToFortnox(customer, facility)
        assert fortnoxCustomer.Email == customer.email
        assert fortnoxCustomer.id
        assert ExternalSynchronizationEntity.countByExternalEntityIdAndEntity(fortnoxCustomer.id, ExternalSynchronizationEntity.LocalEntity.CUSTOMER) == 1

        //update
        customer = Customer.get(customer.id)
        customer.email = "newmail@test.com"
        fortnoxCustomer = service.saveMatchiCustomerToFortnox(customer, facility)
        assert fortnoxCustomer.Email == customer.email
    }

    @Ignore
    void testSaveMatchiInvoiceToFortnox() {
        def customer = new Customer(firstname: "First", lastname: "Last", email: "customer@test.com",
                telephone: "031-212121", facility: facility).save(validate: false)
        def invoice = new Invoice(rows: [], invoiceDate: new Date() - 3,
                expirationDate: new Date() + 15, customer: customer, text: "Test invoice")
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10), unit: 1, discount: 1, externalArticleId: 5)
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10), vat: 25)
        invoice.save(validate: false)

        //save
        FortnoxInvoice fortnoxInvoice = service.saveMatchiInvoiceToFortnox(invoice, facility)
        assert fortnoxInvoice.Remarks == invoice.text
        assert fortnoxInvoice.id

        //update
        invoice = invoice.get(invoice.id)
        invoice.text = "New description"
        fortnoxInvoice = service.saveMatchiInvoiceToFortnox(invoice, facility)
        assert fortnoxInvoice.Remarks == invoice.text
    }
}
