package com.matchi

import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.coupon.GiftCard
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.payment.PaymentException
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.sql.GroovyRowResult
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(CouponService)
@Mock([Coupon, CustomerCoupon, CustomerCouponTicket, Customer, User, Booking, Facility, GiftCard])
class CouponServiceTests {

    CustomerCoupon customerCoupon
    Customer customer
    User user
    Facility facility
    Facility facility1
    int nrOfTickets = 2
    def sqlMock

    @Before
    public void setUp() {
        user = new User().save(validate: false)
        facility = new Facility(id: 1).save(validate:  false)
        facility1 = new Facility(id: 2).save(validate:  false)
        customer = new Customer(number: 123, user: user, facility: facility).save(validate: false)

        customerCoupon = createCustomerCoupon()
        customerCoupon.customer = customer
        customerCoupon.save(validate: true)
        sqlMock = mockFor(groovy.sql.Sql)
        service.groovySql = sqlMock.createMock()
    }

    @After
    public void tearDown() {
    }

    @Test
    void testConsumeUserCouponTicketConsumesTicket() {
        def booking = new Booking(id: 1, customer: customerCoupon.customer).save(validate: false)
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.getCurrentUser { ->
            return new User(id: 2L)
        }
        service.springSecurityService = springSecurityService.createMock()

        assert customerCoupon.nrOfTickets == nrOfTickets

        service.consumeTicket(customerCoupon, new Order(price: 30, article: Order.Article.BOOKING))

        assert customerCoupon.nrOfTickets == (nrOfTickets - 1)
        springSecurityService.verify()
    }

    @Test
    void testConsumeUserGiftCardTicketConsumesTicket() {
        GiftCard giftCard = new GiftCard(nrOfTickets: nrOfTickets, facility: facility, nrOfDaysValid: 10).save(validate: false)
        CustomerCoupon customerGiftCard = CustomerCoupon.link(user, customer, giftCard, 100)
        def booking = new Booking(id: 1, customer: customerCoupon.customer).save(validate: false)
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.getCurrentUser { ->
            return new User(id: 2L)
        }
        service.springSecurityService = springSecurityService.createMock()

        assert customerGiftCard.nrOfTickets == 100

        service.consumeTicket(customerGiftCard, new Order(price: 30, article: Order.Article.BOOKING))

        assert customerGiftCard.nrOfTickets == 70
        springSecurityService.verify()
    }

    @Test
    void testConsumeUserCouponTicketThrowsExceptionIfAllTicketsAreConsumed() {
        Booking booking = new Booking(id: 2, customer: customerCoupon.customer).save(validate: false)
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.getCurrentUser(2..2) { ->
            return new User(id: 2L)
        }
        service.springSecurityService = springSecurityService.createMock()

        assert customerCoupon.nrOfTickets == nrOfTickets

        //Consuming only ticket
        assert service.consumeTicket(customerCoupon, new Order(price: 100, article: Order.Article.BOOKING))
        //..next one
        assert service.consumeTicket(customerCoupon, new Order(price: 100, article: Order.Article.BOOKING))
        //no more available
        assert !service.consumeTicket(customerCoupon, new Order(price: 100, article: Order.Article.BOOKING))
        springSecurityService.verify()
    }

    @Test
    void testGetValidCouponsByCustomerReturnsCorrectCoupons() {
        assert customer.customerCoupons.size() == 1

        def facilityService = mockFor(FacilityService)
        facilityService.demand.getAllHierarchicalFacilities {
            return [customer.facility.id]
        }
        service.facilityService = facilityService.createMock()

        def customerService = mockFor(CustomerService)
        customerService.demand.findHierarchicalUserCustomers { it, a ->
            return [it]
        }
        service.customerService = customerService.createMock()

        CustomerCoupon uc1 = createCustomerCoupon(facility1)
        uc1.customer = customer
        uc1.save(failOnError: true)

        CustomerCoupon uc2 = createCustomerCoupon()
        uc2.customer = customer
        uc2.expireDate = new LocalDate().minusDays(1)
        uc2.save(failOnError: true)

        CustomerCoupon uc3 = createCustomerCoupon()
        uc3.customer = customer
        uc3.dateLocked = new Date()
        uc3.save(failOnError: true)

        assert customer.customerCoupons.size() == 4

        def validCustomerCoupons = service.getValidCouponsByCustomerUser(customer, Coupon,null)

        assert customerCoupon.isValid()
        assert uc1.isValid()
        assert !uc2.isValid()
        assert !uc3.isValid()
        assert validCustomerCoupons.size() == 2
    }

    @Test
    void testUpdateTicketIfExists() {
        def booking = new Booking(id: 1, customer: customerCoupon.customer, slot: new Slot(startTime: new Date(), court: new Court())).save(validate: false)
        def payment = new CouponOrderPayment(ticket: new CustomerCouponTicket(customerCoupon: customerCoupon))
        def order = new Order(payments: [payment] as Set)

        service.updateTicketIfExists(order, booking)

        assert payment.ticket.purchasedObjectId == booking.id
        assert payment.ticket.description
    }

    void testGetExpiresFirstCustomerCoupon() {
        def punchCard = new Coupon()
        punchCard.id == 100L
        def giftCard = new GiftCard()
        giftCard.id = 200L
        def cc1 = new CustomerCoupon(coupon: punchCard, expireDate: LocalDate.now().plusDays(10), nrOfTickets: 1)
        def cc2 = new CustomerCoupon(coupon: punchCard, expireDate: LocalDate.now().plusDays(5), nrOfTickets: 1)
        def cc3 = new CustomerCoupon(coupon: giftCard, expireDate: LocalDate.now().plusDays(3), nrOfTickets: 1000)
        def cc4 = new CustomerCoupon(coupon: giftCard, expireDate: LocalDate.now().plusDays(2), nrOfTickets: 1000)
        def customer = new Customer(customerCoupons: [cc1, cc2, cc3, cc4])

        def customerService = mockFor(CustomerService)
        customerService.demand.findHierarchicalUserCustomers(1..10) {
            return [customer]
        }
        service.customerService = customerService.createMock()

        assert cc2 == service.getExpiresFirstCustomerCoupon(customer, punchCard, new BigDecimal("500"))
        assert cc4 == service.getExpiresFirstCustomerCoupon(customer, giftCard, new BigDecimal("500"))
    }

    private CustomerCoupon createCustomerCoupon() {
        return createCustomerCoupon(null)
    }

    private CustomerCoupon createCustomerCoupon(Facility fac) {
        Coupon c = new Coupon(nrOfTickets: nrOfTickets, facility: fac ?: facility, nrOfDaysValid: 10).save(validate: false)

        CustomerCoupon cc = CustomerCoupon.link(user, customer, c, nrOfTickets)
        return cc
    }


    void testFindAnyCouponByFacilityAndId_InputDataValidation() {
        assertNull service.findAnyCouponById(null)

        sqlMock.demand.rows{sql, params ->
            new ArrayList<GroovyRowResult>()
        }
        assertNull service.findAnyCouponById(42)
    }
}
