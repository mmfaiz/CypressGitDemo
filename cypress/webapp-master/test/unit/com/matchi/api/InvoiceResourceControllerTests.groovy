package com.matchi.api

import com.matchi.*
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import com.matchi.marshallers.InvoiceMarshaller
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.LocalDate
import org.junit.Before

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.mockCriteria
/**
 * @author Sergei Shushkevich
 */
@TestFor(InvoiceResourceController)
@Mock([Customer, Facility, Invoice, InvoiceRow, InvoiceService, Municipality, Region])
@TestMixin(DomainClassUnitTestMixin)
class InvoiceResourceControllerTests {

    @Before
    void setUp() {
        controller.invoiceService = new InvoiceService()
        controller.invoiceService.transactionManager = getTransactionManager()
    }

    void testShow() {
        new InvoiceMarshaller().register()
        JSON.registerObjectMarshaller(LocalDate, { d -> d.toString() })
        def facility = createFacility()
        def customer = createCustomer(facility)
        def invoice = createInvoice(customer)
        invoice.addToRows(new InvoiceRow(price: 100, amount: 1))
        def facilityService = mockFor(FacilityService)
        facilityService.demand.getFacility { fid -> facility }
        controller.facilityService = facilityService.createMock()
        mockCriteria(Invoice)

        controller.show(invoice.id)

        assert response.status == 200
        def json = response.json
        assert json.size() == 12
        assert json.id == invoice.id
        assert json.currency == facility.currency
        assert json.customerId == customer.id
        assert json.credited == false
        assert json.cancelled == false
        assert json.invoiceDate == new LocalDate().toString()
        assert !json.dueDate
        assert json.rows.size() == 1
        def row = json.rows[0]
        assert row.total == 100
        assert row.discountType == "AMOUNT"
        assert row.quantity == 1
        facilityService.verify()
    }

    void testShowNotFound() {
        def facilityService = mockFor(FacilityService)
        facilityService.demand.getFacility { fid -> createFacility() }
        controller.facilityService = facilityService.createMock()
        mockCriteria(Invoice)

        controller.show(createInvoice(createCustomer()).id)

        assert response.status == 404
        facilityService.verify()
    }

    void testUpdateInvoices() {
        def mockMemberService = mockFor(MemberService)
        mockMemberService.demand.handleMembershipInvoicePayment {  inv ->  }

        defineBeans{
            memberService(InstanceFactoryBean, mockMemberService.createMock(), MemberService)
        }
        def facility = createFacility()
        def invoice = createInvoice(createCustomer(facility))
        def facilityService = mockFor(FacilityService)
        facilityService.demand.getFacility { fid -> facility }
        controller.facilityService = facilityService.createMock()
        mockCriteria(Invoice)
        request.json = """[{"id": $invoice.id, "balance": 0, "paidDate": "2018-01-02", "number": 123}]""".toString()

        controller.updateInvoices()

        assert response.status == 200
        assert invoice.status == Invoice.InvoiceStatus.PAID
        assert invoice.number == 123L
        facilityService.verify()
    }

    void testUpdateInvoicesWithInvalidRequest() {
        request.json = '{"id": 123, "balance": 0}'
        shouldFail(APIException) {
            controller.updateInvoices()
        }
    }

    void testUpdateInvoicesWithValidationError() {
        request.json = '[{"id": 123}]'
        controller.updateInvoices()
        assert response.status == 422
        assert response.json.errors.size() == 1
        assert response.json.errors[0].path == "/0/balance"
    }
}