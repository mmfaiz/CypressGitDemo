package com.matchi

import com.matchi.api.InvoiceUpdateCommand
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class InvoiceServiceIntegrationTests extends GroovyTestCase {

    def invoiceService
    def springSecurityService

    void testCreateInvoiceOrderPayment() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def facility = createFacility()
        def order = createOrder(user, facility)
        def customer = createCustomer(facility)
        order.customer = customer
        order.save(failOnError: true, flush: true)

        def payment = invoiceService.createInvoiceOrderPayment(order)

        assert payment
        assert 1 == InvoiceOrderPayment.count()
        order.refresh()
        assert 1 == order.payments.size()
        assert Order.Status.COMPLETED == order.status
        assert payment.issuer.id == user.id
        assert payment.amount == order.price
        assert payment.vat == order.vat
        assert payment.status == OrderPayment.Status.CAPTURED
        assert payment.invoiceRow
        assert payment.invoiceRow.price == order.price
        assert 1 == payment.invoiceRow.amount
        assert 1 == InvoiceRow.count()
    }

    void testGetInvoice() {
        def facility1 = createFacility()
        def facility2 = createFacility()
        def invoice1 = createInvoice(createCustomer(facility1))
        def invoice2 = createInvoice(createCustomer(facility2))

        assert invoiceService.getInvoice(invoice1.id, facility1)
        assert !invoiceService.getInvoice(invoice1.id, facility2)
        assert invoiceService.getInvoice(invoice2.id, facility2)
        assert !invoiceService.getInvoice(invoice2.id, facility1)
        assert !invoiceService.getInvoice(12345L, facility1)
    }

    void testListInvoices() {
        def facility = createFacility()
        def invoice1 = createInvoice(createCustomer(facility))
        invoice1.invoiceDate = new LocalDate("2018-01-01")
        invoice1.save(failOnError: true, flush: true)
        def invoice2 = createInvoice(createCustomer(facility))
        invoice2.invoiceDate = new LocalDate("2018-01-02")
        invoice2.save(failOnError: true, flush: true)
        def invoice3 = createInvoice(createCustomer())

        def result = invoiceService.listInvoices(facility, 1, 0,
                new LocalDate("2018-01-01"), new LocalDate("2018-01-02"))
        assert result.size() == 1
        assert result.totalCount == 2

        result = invoiceService.listInvoices(facility, 10, 0,
                new LocalDate("2018-01-01"), null)
        assert result.size() == 2

        result = invoiceService.listInvoices(facility, 10, 0,
                null, new LocalDate("2018-01-02"))
        assert result.size() == 2

        result = invoiceService.listInvoices(facility, 10, 0, null, null)
        assert result.size() == 2

        result = invoiceService.listInvoices(facility, 1, 0,
                new LocalDate("2018-01-01"), new LocalDate("2018-01-01"))
        assert result.size() == 1
        assert result[0].id == invoice1.id
    }

    void testUpdateInvoice() {
        def facility = createFacility()
        def invoice = createInvoice(createCustomer(facility))

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.INCORRECT

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, cancelled: true, number: 123L), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.CANCELLED
        assert Invoice.findById(invoice.id).number == 123L

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 0, paidDate: new LocalDate()), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.PAID
        assert Invoice.findById(invoice.id).paidDate

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, dueDate: new LocalDate().minusDays(10)), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.OVERDUE
        assert Invoice.findById(invoice.id).expirationDate == new LocalDate().minusDays(10)

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, booked: true), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.POSTED

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, credited: true, creditInvoiceReference: 123L), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.CREDITED

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, cancelled: true), facility)
        assert Invoice.findById(invoice.id).status == Invoice.InvoiceStatus.CREDITED

        invoiceService.updateInvoice(new InvoiceUpdateCommand(
                id: invoice.id, balance: 100, sent: true, dueDate: new LocalDate().plusDays(10)), facility)
        assert Invoice.findById(invoice.id).sent == Invoice.InvoiceSentStatus.SENT
        assert Invoice.findById(invoice.id).number == 123L
        assert Invoice.findById(invoice.id).expirationDate == new LocalDate().plusDays(10)
    }
}