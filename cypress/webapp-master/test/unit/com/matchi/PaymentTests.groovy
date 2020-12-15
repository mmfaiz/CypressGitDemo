package com.matchi

import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentMethod
import com.matchi.payment.PaymentStatus
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

@TestFor(Payment)
@Mock([PaymentTransaction, Payment])
class PaymentTests {

    def mockedConfig
    @Before
    public void setUp() {
        mockForConstraintsTests(Payment)
        mockDomain(Payment)

        mockedConfig = new ConfigObject()
        mockedConfig.payment.secret="secret"
        mockedConfig.payment.merchantId="9999"
        mockedConfig.payment.version="3"
        mockedConfig.payment.methods="KORTINSE"
        mockedConfig.payment.currency="SEK"
        mockedConfig.payment.language="SWE"
        mockedConfig.payment.country="SE"
        mockedConfig.payment.country="SE"
        mockedConfig.payment.url="https://test-epayment.auriganet.eu/paypagegw"
    }

    @Test
    void testValidPayment() {
        Payment payment = new Payment()
        payment.facility = new Facility()
        payment.articleType = ArticleType.ACTIVITY
        payment.orderNumber = "123-123"
        payment.status = PaymentStatus.OK

        payment.validate()
        System.out.println(payment.errors);
        assertTrue(payment.validate())

    }

    @Test
    void testTotalPaid() {
        Payment payment = new Payment()
        payment.amount = "10000" // Represents 100.00
        payment.save(validate: false)

        PaymentTransaction transaction1 = new PaymentTransaction()
        transaction1.paidAmount = 50
        payment.addToPaymentTransactions(transaction1)
        payment.save(validate: false)

        assert payment.getTotalPaid() == 50

        PaymentTransaction transaction2 = new PaymentTransaction()
        transaction2.paidAmount = 50
        payment.addToPaymentTransactions(transaction2)
        payment.save(validate: false)

        assert payment.getTotalPaid() == 100

        PaymentTransaction transaction3 = new PaymentTransaction()
        transaction3.paidAmount = -50
        payment.addToPaymentTransactions(transaction3)
        payment.save(validate: false)

        assert payment.getTotalPaid() == 50
    }

    @Test
    void testGetPaymentStatus() {
        Payment payment = new Payment()
        payment.amount = "10000" // Represents 100.00
        payment.save(validate: false)

        assert payment.getPaymentStatus() == PaymentStatus.PENDING

        PaymentTransaction transaction1 = new PaymentTransaction()
        transaction1.paidAmount = 50
        payment.addToPaymentTransactions(transaction1)
        payment.save(validate: false)

        assert payment.getPaymentStatus() == PaymentStatus.PARTLY

        PaymentTransaction transaction2 = new PaymentTransaction()
        transaction2.paidAmount = 50
        payment.addToPaymentTransactions(transaction2)
        payment.save(validate: false)

        assert payment.getPaymentStatus() == PaymentStatus.OK
    }
    @Test
    void testIsOnlineIsTrueWhenCreditCard() {
        Payment payment = new Payment()

        payment.method  = PaymentMethod.CREDIT_CARD
        assert payment.isCreditCard()

        payment.method  = PaymentMethod.CREDIT_CARD_RECUR
        assert payment.isCreditCard()
    }
    @Test
    void testIsOnlineIsFalseWhenNotCreditCard() {
        Payment payment = new Payment()

        payment.method  = PaymentMethod.COUPON
        assert !payment.isCreditCard()

        payment.method  = PaymentMethod.CASH
        assert !payment.isCreditCard()
    }
}
