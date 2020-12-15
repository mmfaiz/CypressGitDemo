package com.matchi.orders

import com.matchi.Booking
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

import static com.matchi.TestUtils.createCustomer

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Order)
@Mock([Order, OrderPayment, CouponOrderPayment, AdyenOrderPayment, Booking, Region, Municipality, Facility, Customer, User])
class OrderTests {

    @Test
    void testFinalPaidReturnsTrueIfPaid() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))

        assert domain.isFinalPaid()
    }

    @Test
    void testNotPaidIfPaymentIsNotCaptured() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.NEW))

        assert !domain.isFinalPaid()
    }

    @Test
    void testNotPaidIfPaymentIsPaymentAmountToSmall() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CouponOrderPayment(amount: 50, status: OrderPayment.Status.CAPTURED))

        assert !domain.isFinalPaid()
    }

    @Test
    void testNotPaidIfNoPayments() {
        domain.price = 100
        domain.vat = 0

        assert !domain.isFinalPaid()
    }

    @Test
    void testPaidIfToMuchPaid() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CouponOrderPayment(amount: 150, status: OrderPayment.Status.CAPTURED))

        assert domain.isFinalPaid()
    }

    @Test
    void testRestAmountToPayReturnsCorrectAmount() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CashOrderPayment(amount: 50, status: OrderPayment.Status.CAPTURED))

        assert domain.getRestAmountToPay() == 50
    }

    @Test
    void testRestAmountToPayReturnsZeroAmountIfFinalPaid() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CashOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))

        assert domain.getRestAmountToPay() == 0
    }

    @Test
    void testIfPartlyPaidReturnsTrue() {
        domain.price = 100
        domain.vat = 0
        domain.addToPayments(new CashOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))

        assert domain.isPartlyPaid()
        assert !domain.isFinalPaid()
    }

    @Test
    void testOldOrderNotRefundableWithAdyenPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert !domain.isStillRefundable()
    }

    @Test
    void testOldOrderRefundableWithCashOrderPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)
        domain.addToPayments(new CashOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testOldOrderRefundableWithInvoiceOrderPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)
        domain.addToPayments(new InvoiceOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testOldOrderRefundableWithCouponOrderPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)
        domain.addToPayments(new CouponOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testOldOrderNotRefundableWithMixedPayments() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        domain.addToPayments(new CashOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert !domain.isStillRefundable()
    }

    @Test
    void testEdgeCaseOrderRefundableWithAdyenPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS)
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testNewOrderRefundableWithAdyenPayment() {
        domain.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS - 1)
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testTodayOrderRefundableWithAdyenPayment() {
        domain.dateDelivery = new Date()
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testFutureOrderRefundableWithAdyenPayment() {
        domain.dateDelivery = new Date().plus(1)
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.isStillRefundable()
    }

    @Test
    void testProcessableFree() {
        domain.price = 0
        domain.payments = []
        assert domain.isProcessable()
    }

    @Test
    void testProcessableCheckAllPaymentsNew() {
        domain.price = 1
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.NEW)]
        assert !domain.isProcessable()
    }

    @Test
    void testProcessableCheckOnePaymentFailed() {
        domain.price = 1
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.FAILED)]
        assert !domain.isProcessable()
    }

    @Test
    void testProcessableCheckAllPaymentsAuthed() {
        domain.price = 1
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)]
        assert domain.isProcessable()
    }

    @Test
    void testProcessableCheckAllPaymentsCaptured() {
        domain.price = 1
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.CAPTURED)]
        assert domain.isProcessable()
    }

    @Test
    void testProcessableCheckIfFreeAndNoPayments() {
        domain.price = 0
        assert domain.isProcessable()
    }

    @Test
    void testNoIArticleItemIsReturnedIfNoneExists() {
        domain.article = Order.Article.BOOKING

        assert !domain.retrieveArticleItem()
    }

    @Test
    void testNoRefundIsMadeIfArticleExists() {
        domain.article = Order.Article.BOOKING
        Booking booking = new Booking(order: domain).save(validate: false)

        assert domain.retrieveArticleItem() == booking
    }

    @Test
    void testIsDeletable() {
        Order.Status.list().each { Order.Status status ->
            domain.status = status

            if (status.equals(Order.Status.NEW)) {
                assert domain.isDeletable()
            } else {
                assert !domain.isDeletable()
            }
        }

        domain.status = Order.Status.NEW

        OrderPayment.Status.list().each { OrderPayment.Status status ->
            domain.payments = [new AdyenOrderPayment(status: status)]

            if (status.equals(OrderPayment.Status.NEW)) {
                assert domain.isDeletable()
            } else {
                assert !domain.isDeletable()
            }
        }

        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.CAPTURED),
                           new AdyenOrderPayment(status: OrderPayment.Status.NEW)]

        assert !domain.isDeletable()

        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.NEW),
                           new AdyenOrderPayment(status: OrderPayment.Status.NEW)]

        assert domain.isDeletable()
    }

    @Test
    void testAddPlayersToMetadata() {
        def c1 = createCustomer(null, "player0@email.com")
        def c2 = createCustomer(null, "player1@email.com")
        def c3 = createCustomer()
        def c4 = new Customer(email: "unknown@email.com")
        List<Customer> customers = [c1, c2, c3, c4, new Customer()]

        domain.metadata = [:]

        domain.addPlayersToMetadata(customers)

        assert domain.metadata.size() == 5
        assert domain.metadata["playerCustomerId_0"] == c1.id.toString()
        assert domain.metadata["playerCustomerId_1"] == c2.id.toString()
        assert domain.metadata["playerCustomerId_2"] == c3.id.toString()
        assert domain.metadata["playerEmail_3"] == c4.email
        assert domain.metadata["playerEmail_4"] == null
        assert domain.metadata.containsKey("playerEmail_4")
    }

    @Test
    void testGetPlayersFromMetadata() {
        domain.metadata = [playerEmail_0: "player0@email.com", playerEmail_1: "player1@email.com"]

        assert domain.getPlayersFromMetadata().size() == 2
    }

    @Test
    void testRefundableCoupon() {
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))
        assert domain.hasRefundablePayment()
    }

    @Test
    void testRefundableAdyen() {
        domain.addToPayments(new AdyenOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert domain.hasRefundablePayment()
    }

    @Test
    void testNotRefundableInvoice() {
        domain.addToPayments(new InvoiceOrderPayment(amount: 80, status: OrderPayment.Status.CAPTURED))
        assert !domain.hasRefundablePayment()
    }

    @Test
    void testNotRefundableCash() {
        domain.addToPayments(new CashOrderPayment(amount: 50, status: OrderPayment.Status.CAPTURED))
        assert !domain.hasRefundablePayment()
    }

    @Test
    void testNotRefundableNoPayment() {
        assert !domain.hasRefundablePayment()
    }

    @Test
    void testNotRefundableOnePayment() {
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.NEW))
        assert !domain.hasRefundablePayment()
    }

    @Test
    void testNotRefundableTwoPayment() {
        domain.addToPayments(new CouponOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))
        domain.addToPayments(new CashOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED))
        assert !domain.hasRefundablePayment()
    }

    @Test
    void testGetRemotePayables() {
        assert Order.Article.getRemotePayables() == [Order.Article.BOOKING, Order.Article.MEMBERSHIP]
    }

    @Test
    void testIsRemotePayableBooking() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.NEW
        domain.payments = [].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testFreeOrderIsNotRemotePayableBooking() {
        domain.price = 0
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.NEW
        domain.payments = [].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableMembership() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.NEW
        domain.payments = [].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableBookingFailedAttempt() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.CANCELLED
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.FAILED)].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableMembershipFailedAttempt() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.CANCELLED
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.FAILED)].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsNotRemotePayableBooking() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.COMPLETED
        domain.payments = [].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testIsNotRemotePayableMembership() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.NEW
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testNotIsRemotePayableBookingFailedAttempt() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.ANNULLED
        domain.payments = [].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableMembershipFailedAttempt2() {
        domain.price = 100
        domain.origin = Order.ORIGIN_WEB
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.CANCELLED
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.FAILED)].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsNotRemotePayableBookingWeb() {
        domain.price = 100
        domain.origin = Order.ORIGIN_WEB
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.COMPLETED
        domain.payments = [].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableMembershipApi() {
        domain.price = 100
        domain.origin = Order.ORIGIN_API
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.NEW
        domain.payments = [].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsNotRemotePayableSubmission() {
        domain.price = 100
        domain.origin = Order.ORIGIN_FACILITY
        domain.article = Order.Article.FORM_SUBMISSION
        domain.status = Order.Status.NEW
        domain.payments = [].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testIsRemotePayableConfirmedNew() {
        domain.price = 100
        domain.origin = Order.ORIGIN_WEB
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.CONFIRMED
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.NEW)].toSet()

        assert domain.isRemotePayable()
    }

    @Test
    void testIsNotRemotePayableConfirmedAuthed() {
        domain.price = 100
        domain.origin = Order.ORIGIN_WEB
        domain.article = Order.Article.BOOKING
        domain.status = Order.Status.CONFIRMED
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)].toSet()

        assert !domain.isRemotePayable()
    }

    @Test
    void testGetActivePaymentNoPayments() {
        domain.payments = null
        assert !domain.getActivePayment()

        domain.payments = [].toSet()
        assert !domain.getActivePayment()
    }

    @Test
    void testGetActivePaymentOnePayment() {
        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)].toSet()
        assert domain.getActivePayment()

        domain.payments = [new AdyenOrderPayment(status: OrderPayment.Status.CREDITED)].toSet()
        assert !domain.getActivePayment()
    }
}
