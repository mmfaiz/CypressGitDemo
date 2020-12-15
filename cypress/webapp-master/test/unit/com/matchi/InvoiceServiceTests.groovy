package com.matchi

import com.matchi.api.ArticleCommand
import com.matchi.facility.Organization
import com.matchi.fortnox.FortnoxFacadeService
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceArticle
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.payment.PaymentMethod
import com.matchi.subscriptionredeem.SlotRedeem
import grails.test.GrailsMock
import grails.test.mixin.*
import org.joda.time.LocalDate
import org.junit.*

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.mockCriteria

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(InvoiceService)
@Mock([Invoice, InvoiceRow, InvoiceArticle, Customer, Facility, User, Membership, MembershipType, Region, FacilityProperty,
Municipality, MembershipFamily, Organization, InvoiceOrderPayment, Order])
class InvoiceServiceTests {

    def invoice
    def facility

    private GrailsMock fortnoxFacadeServiceMock
    private GrailsMock externalSynchronizationServiceMock

    @Before
    public void setUp() {

        fortnoxFacadeServiceMock = mockFor(FortnoxFacadeService)
        externalSynchronizationServiceMock = mockFor(ExternalSynchronizationService)

        facility = new Facility()
        invoice = new Invoice()
        invoice.invoiceDate = new LocalDate()
        invoice.expirationDate = new LocalDate().plusDays(10)
        invoice.text = "Invoice text"
        invoice.addToRows(createInvoiceRow("Test", 10, 25, 5))
        invoice.addToRows(createInvoiceRow("Test 2", 20, 25, 0))

        fortnoxFacadeServiceMock.demand.cancelInvoice(0..10) { def invoice ->
        }

        fortnoxFacadeServiceMock.demand.saveMatchiInvoiceToFortnox(1..10) { def invoice ->
            new FortnoxInvoice(DocumentNumber: invoice.id)
        }

        externalSynchronizationServiceMock.demand.getFortnoxInvoiceNumber(0..1) { def invoice ->
        }

        service.fortnoxFacadeService = fortnoxFacadeServiceMock.createMock()
        service.externalSynchronizationService = externalSynchronizationServiceMock.createMock()
    }

    void testSaveToFortnoxMarksInvoiceSynchronized() {
        def fortnoxInvoiceId = "190"

        fortnoxFacadeServiceMock.demand.saveMatchiInvoiceToFortnox(1..10) { def invoice ->
            new FortnoxInvoice(DocumentNumber: fortnoxInvoiceId)
        }

        externalSynchronizationServiceMock.demand.markSynchronized() { def externalSystem, def invoice, def id ->
            assert id == fortnoxInvoiceId
        }

        service.saveToFortnox(invoice, facility)

        assert fortnoxInvoiceId == invoice.number.toString()
    }

    void testSynchronizeCustomersIfUnsynchronized() {
        def fortnoxInvoiceId = "190"

        fortnoxFacadeServiceMock.demand.saveMatchiInvoiceToFortnox(1..10) { def invoice ->
            new FortnoxInvoice(DocumentNumber: fortnoxInvoiceId)
        }

        service.saveToFortnox(invoice, facility)

        assert fortnoxInvoiceId == invoice.number.toString()
    }

    void testMarkAsSentByEmail() {
        def invoice = new Invoice()

        service.markAsSentByEmail(invoice)

        assert Invoice.InvoiceSentStatus.EMAIL == invoice.sent
        assert invoice.lastSent
    }

    void testCreateFamilyMembershipDiscountRow() {
        def customer = createCustomer()
        def user = createUser()
        def mtype = createMembershipType()
        def membership = createMembership(customer)
        def row = createInvoiceRow("desc", mtype.price, 0, 0, customer, user).save(failOnError: true)
        def orderPayment = createInvoiceOrderPayment(user, membership.order, "abc")
        orderPayment.invoiceRow = row
        orderPayment.save(failOnError: true)
        membership.order.payments << orderPayment
        membership.order.price = mtype.price
        membership.order.save(failOnError: true)
        mockCriteria(Order)

        assert !service.createFamilyMembershipDiscountRow(customer, [row], user, mtype.price - 10)

        def mf = new MembershipFamily(contact: customer).save(failOnError: true)
        membership.family = mf
        membership.save()

        def result = service.createFamilyMembershipDiscountRow(customer, [row], user, mtype.price - 10)
        assert result
        assert -10 == result.price
        assert 0 == result.vat
        assert 0 == result.discount
        assert 1 == result.amount
        assert customer == result.customer
        assert user == result.createdBy
        assert result.description

        assert !service.createFamilyMembershipDiscountRow(customer, [row], user, mtype.price)
    }

    void testGetOrganization() {
        def organization = createOrganization(createFacility())
        assert organization == service.getOrganization(organization.id)
    }

    void testUpdateItems() {
        facility = createFacility()
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_EXTERNAL_ARTICLES.name(), value: "1")
        ]
        service.updateItems(facility.id, [new ArticleCommand(
                articleNumber: 100,
                name: "Article 1",
                description: "Description 1",
                price: 200,
                vat: 6)])

        def result = service.getItems(facility)

        assert result[0].id == "100"
        assert result[0].descr == "Article 1"
        assert result[0].Description == "Description 1"
        assert result[0].firstPrice == 200
        assert result[0].SalesPrice == 200
        assert result[0].VAT == 6
    }

    void testDeleteInvoiceRow() {
        GrailsMock mockInvoiceRow = mockFor(InvoiceRow)
        InvoiceRow invoiceRow = mockInvoiceRow.createMock()

        GrailsMock mockStaticInvoiceOrderPayment = mockFor(InvoiceOrderPayment)
        GrailsMock mockInvoiceOrderPayment = mockFor(InvoiceOrderPayment)
        InvoiceOrderPayment invoiceOrderPayment = mockInvoiceOrderPayment.createMock()

        GrailsMock mockOrder = mockFor(Order)
        Order order = mockOrder.createMock()

        GrailsMock mockStaticSubscription = mockFor(Subscription)
        GrailsMock mockStaticSlotRedeem = mockFor(SlotRedeem)
        GrailsMock mockStaticPaymentTransaction = mockFor(PaymentTransaction)

        mockStaticSubscription.demand.static.findByInvoiceRow(1) { InvoiceRow row ->
            assert row == invoiceRow
            return []
        }

        mockStaticSlotRedeem.demand.static.findByInvoiceRow(1) { InvoiceRow row ->
            assert row == invoiceRow
            return []
        }

        mockStaticPaymentTransaction.demand.static.findByInvoiceRow(1) { InvoiceRow row ->
            assert row == invoiceRow
            return []
        }

        mockStaticInvoiceOrderPayment.demand.static.findByInvoiceRow(1) { InvoiceRow row ->
            assert row == invoiceRow
            return invoiceOrderPayment
        }

        mockInvoiceOrderPayment.demand.asBoolean(1) { ->
            return true
        }

        mockInvoiceOrderPayment.demand.getMethod(1) { ->
            return PaymentMethod.INVOICE
        }

        mockInvoiceOrderPayment.demand.getOrders(1) { ->
            return [order]
        }

        mockOrder.demand.removeFromPayments(1) { InvoiceRow row ->
            assert row == invoiceRow
        }

        mockOrder.demand.getPayments(1) { ->
            return []
        }

        mockOrder.demand.setStatus(1) { Order.Status status ->
            assert status.equals(Order.Status.NEW)
        }

        mockOrder.demand.save(1) { -> }
        mockInvoiceOrderPayment.demand.delete(1) { -> }
        mockInvoiceRow.demand.delete(1) { -> }

        service.delete(invoiceRow)

        mockInvoiceRow.verify()
        mockStaticSubscription.verify()
        mockStaticSlotRedeem.verify()
        mockStaticPaymentTransaction.verify()
        mockStaticInvoiceOrderPayment.verify()
        mockInvoiceOrderPayment.verify()
        mockOrder.verify()
    }

    def createInvoiceRow(description = "Test description",
            def price = new BigDecimal(10), def vat = new BigDecimal(25),
            def discount = new BigDecimal(0), customer = null, user = null, amount = 1) {
        def row = new InvoiceRow()
        row.description = description
        row.price = price
        row.vat = vat
        row.discount = discount
        row.amount = amount
        row.customer = customer
        row.createdBy = user

        row
    }

    def assertRowsEquals(def row, def invoiceRow) {
        assert row.getPriceExcludingVAT() == invoiceRow.price
        assert row.vat   == invoiceRow.vat
        assert row.discount   == invoiceRow.discount
        assert row.description == invoiceRow.descr
        assert row.amount == invoiceRow.amount
        assert row.unit == invoiceRow.unit
    }
}
