package com.matchi.fortnox.v3

import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.external.ExternalSynchronizationEntity
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import com.matchi.FacilityProperty
import org.junit.Before
import org.junit.Ignore

import static com.matchi.TestUtils.*

/**
 * @author Michael Astreiko
 */
@TestFor(Fortnox3InvoiceService)
//Using ControllerUnitTestMixin to enable JSON converters
@TestMixin(ControllerUnitTestMixin)
@Mock([FacilityProperty, Facility, Region, Municipality])
class Fortnox3InvoiceServiceTest extends Fortnox3CommonTest {
    @Before
    void setUp() {
        super.setUp()
        service.fortnox3Service = new Fortnox3Service()
        service.fortnox3Service.permitsPerSecond = 3.0d
    }

    void testSetAndGet() {
        String customerNumber = "2"
        FortnoxInvoice invoice = new FortnoxInvoice(CustomerNumber: customerNumber, DueDate: new Date() + 14)
        invoice.InvoiceRows.add(new FortnoxInvoiceRow(ArticleNumber: "1", DeliveredQuantity: '10'))

        assert !invoice.DocumentNumber
        assert !invoice.InvoiceDate

        invoice = service.set(facility, invoice)

        assert invoice.DocumentNumber
        assert invoice.InvoiceDate
        assert customerNumber == invoice.CustomerNumber
        assert "INVOICE" == invoice.InvoiceType
        assert Float.class == invoice.Net.getClass()
        assert 1 == invoice.InvoiceRows.size()
        assert new BigDecimal(10) == new BigDecimal(invoice.InvoiceRows[0].DeliveredQuantity)

        invoice = service.get(facility, invoice.DocumentNumber)

        assert invoice.DocumentNumber
        assert customerNumber == invoice.CustomerNumber
        assert 1 == invoice.InvoiceRows.size()
        assert new BigDecimal(10) == new BigDecimal(invoice.InvoiceRows[0].DeliveredQuantity)
        assert invoice.CreditInvoiceReference == "0"
        assert invoice.DueDate > new Date()
    }

    void testCancelAndList() {
        //First check List without filter
        def invoices = service.list(facility)

        assert invoices
        assert invoices.size() >= 1

        //Then cancel first Invoice
        FortnoxInvoice invoice = invoices.find { !it.Booked && !it.Cancelled }
        assert !invoice.Cancelled

        invoice = service.cancel(facility, invoice.DocumentNumber)
        assert invoice.Cancelled

        invoices = service.list(facility, FortnoxInvoice.Filters.CANCELLED)
        assert invoices.size() == invoices.findAll { it.Cancelled }.size()
        invoice = invoices.find { it.DocumentNumber == invoice.DocumentNumber }
        assert invoice
        assert invoice.Cancelled
    }

    void testPaidDate() {
        String customerNumber = "2"
        FortnoxInvoice unpaidInvoice = new FortnoxInvoice(CustomerNumber: customerNumber, DueDate: new Date() + 14)
        unpaidInvoice.InvoiceRows.add(new FortnoxInvoiceRow(ArticleNumber: "1", DeliveredQuantity: '10', Price: 10))

        unpaidInvoice = service.set(facility, unpaidInvoice)
        assert !service.getInvoiceDatePaid(facility, unpaidInvoice.DocumentNumber)

        def paidInvoices = service.list(facility, FortnoxInvoice.Filters.FULLY_PAID)
        assert service.getInvoiceDatePaid(facility, paidInvoices[0].DocumentNumber)
    }

    /**
     * Test so that "0.00" string is parsed as 0.00 and can be saved in a FortnoxInvoice instance
     */
    void testParseFloatyStrings() {
        def fakeJSON = ["TotalVAT": "0.00"]
        def result = service.getInvoicePropertiesBasedOnJSON(fakeJSON)
        FortnoxInvoice fortnoxInvoice = new FortnoxInvoice(result)

        assert result.TotalVAT == 0
        assert fortnoxInvoice.TotalVAT == 0
    }

    void testGetForCustomerExternalSynchronizationEntity() {
        Facility facility = createFacility()
        def mockFortnox3Service = mockFor(Fortnox3Service)
        service.fortnox3Service = mockFortnox3Service.createMock()

        mockFortnox3Service.demand.doGet(1..1) { def f, def i, def p, def p2, Closure callable ->
            callable.call([Invoices: [[DocumentNumber: "2"], [DocumentNumber: "1"]]])
        }

        ExternalSynchronizationEntity ese = new ExternalSynchronizationEntity(externalEntityId: 1)

        List result = service.getForCustomerExternalSynchronizationEntity(facility, ese)
        assert result.size() == 2
        assert result[0].DocumentNumber == "1"
        assert result[1].DocumentNumber == "2"
        mockFortnox3Service.verify()
    }

    @Ignore
    void testListWithRestriction(){
        def invoices = service.list(facility)
        assert invoices
        def totalSize = invoices.size()
        assert totalSize >= 130

        invoices = service.list(facility, null, new Date() - 5000)
        assert totalSize == invoices.size()

        invoices = service.list(facility, null, new Date() + 1)
        assert !invoices
    }
}
