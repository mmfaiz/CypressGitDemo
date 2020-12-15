package com.matchi.invoice

import grails.test.mixin.*
import org.joda.time.LocalDate
import org.junit.*

@TestFor(Invoice)
@Mock([Invoice, InvoicePayment])
class InvoiceTests {
    Invoice invoice

    @Before
    public void setUp() {
        invoice = new Invoice(rows: [], status: Invoice.InvoiceStatus.POSTED)
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10), vat: (10))
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10), vat: (0))
    }

    @Test
    void testGetTotalPriceExlVat() {
        assert invoice.getTotalExcludingVAT().setScale(1,1) == 19
    }

    @Test
    void testGetTotalPriceInclVat() {
        assert invoice.getTotalIncludingVAT() == 20
    }

    @Test
    void testIsEditableWhenStatusReady() {
        invoice.status = Invoice.InvoiceStatus.READY
        assert invoice.isEditable()
    }

    @Test
    void testIsEditableWhenStatusIncorrect() {
        invoice.status = Invoice.InvoiceStatus.INCORRECT
        assert invoice.isEditable()
    }

    @Test
    void testIsNotEditableWhenStatusNotReadyOrIncorrect() {
        invoice.status = null
        assert !invoice.isEditable()

        def notEditableStatuses = Invoice.InvoiceStatus.values().toList()
        notEditableStatuses.remove(Invoice.InvoiceStatus.READY)
        notEditableStatuses.remove(Invoice.InvoiceStatus.INCORRECT)

        notEditableStatuses.each {
            invoice.status = it
            assert !invoice.isEditable()
        }
    }

    @Test
    void testRoundingValueUp() {
        invoice.rows = []
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10.57), vat: (0))

        assert invoice.getTotalIncludingVATRounded().equals(new BigDecimal(11.0))
    }

    @Test
    void testRoundingValueDown() {
        invoice.rows = []
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10.49), vat: (0))

        assert invoice.getTotalIncludingVATRounded().equals(new BigDecimal(10.0))
    }

    @Test
    void testRoundingValueUpWhenInMiddle() {
        invoice.rows = []
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10.50), vat: (0))

        assert invoice.getTotalIncludingVATRounded().equals(new BigDecimal(11.0))
    }

    @Test
    void testRoundedValueDown() {
        invoice.rows = []
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10.4), vat: (0))

        assert invoice.getRoundedAmount().toFloat() == -0.4f
    }

    @Test
    void testRoundedValueUp() {
        invoice.rows = []
        invoice.rows << new InvoiceRow(amount: 1, price: new BigDecimal(10.7), vat: (0))

        assert invoice.getRoundedAmount().toFloat() == 0.3f
    }

    @Test
    void testSetPaidStatusWhenInvoiceIsFullyPaid() {
        invoice.addPayment(new LocalDate(), new BigDecimal(20))
        assert invoice.status == Invoice.InvoiceStatus.PAID
    }

    @Test
    void testSetPaidDateThenInvoiceIsFullyPaid() {
        invoice.addPayment(new LocalDate().minusDays(1), new BigDecimal(20))
        assert invoice.paidDate != null
        assert invoice.paidDate == new LocalDate().minusDays(1)
    }

    @Test
    void testSetPaidStatusWhenInvoiceIsFullyPaidAndOverdue() {
        invoice.status = Invoice.InvoiceStatus.OVERDUE
        invoice.addPayment(new LocalDate(), new BigDecimal(20))
        assert invoice.status == Invoice.InvoiceStatus.PAID
    }

    @Test
    void testDoesNotSetPaidStatusWhenInvoiceIsNotPaid() {
        invoice.addPayment(new LocalDate(), new BigDecimal(10))
        assert invoice.status == Invoice.InvoiceStatus.POSTED
    }

    @Test
    void testSetNotPaidWhenPaymentIsRemoved() {
        def payment = invoice.addPayment(new LocalDate(), new BigDecimal(20))
        assert invoice.status == Invoice.InvoiceStatus.PAID

        invoice.removePayment(payment)
        assert invoice.status == Invoice.InvoiceStatus.POSTED
    }

    @Test
    void testDontChangeStatusIfCancelledAndPaid() {
        invoice.status = Invoice.InvoiceStatus.CANCELLED
        invoice.addPayment(new LocalDate(), new BigDecimal(100))

        assert invoice.status == Invoice.InvoiceStatus.CANCELLED
    }

    @Test
    void testIdToOCR() {
        invoice.number = 555
        assert invoice.getOCR() == "55558"
    }

    @Test
    void testLongIdToOCR() {
        invoice.number = 1002232023230
        assert invoice.getOCR() == "100223202323058"
    }

    @Test
    void testNullIdToOCR() {
        invoice.number = null
        assert invoice.getOCR() == null
    }

    @Test
    void testIdsOCRs() {
        def ids = [555, 123, 12312]
        def ocrs = Invoice.numbersToOCRs(ids)

        assert ocrs == ["55558", "12351", "1231273"]
    }

    @Test
    void testOCRsToIds() {
        def ocrs = ["55558", "12351", "1231273"]
        def ids  = Invoice.ocrsToNumbers(ocrs)

        assert ids == [555, 123, 12312]
    }

    @Test
    void testInvoiceByOCR() {
        def invoice = new Invoice()
        invoice.number = 10
        invoice.save(validate: false)

        def ocr = invoice.getOCR()

        assert Invoice.findByOCR(ocr).findWhere().number == 10
    }

    @Test
    void testInvoiceByOCRs() {

        [555, 123, 12312].each {
            def invoice = new Invoice()
            invoice.number = it
            invoice.save(validate: false)
        }

        assert Invoice.findAllByOCRs(["55558", "12351", "1231273"]).list().size() == 3

    }

}
