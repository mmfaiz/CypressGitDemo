package com.matchi.subscriptionredeem

import com.matchi.Customer
import com.matchi.User
import com.matchi.facility.Organization
import com.matchi.invoice.InvoiceRow
import com.matchi.price.Price
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(InvoiceRowRedeemStrategy)
@Mock([ User, Customer, Price, InvoiceRow, Organization ])
@TestMixin(GrailsUnitTestMixin)
class InvoiceRowRedeemStrategyTests {

    InvoiceRowRedeemStrategy invoiceRowRedeemStrategy

    @Before
    void setUp() {
        invoiceRowRedeemStrategy = new InvoiceRowRedeemStrategy(id: 1l)
    }

    @Test
    void testTypeIsCorrect() {
        assert invoiceRowRedeemStrategy.type == "INVOICE_ROW"
    }

    @Test
    void testRedeemReturnesCorrectInvoiceRowWithPercetageBack() {
        User user = new User(id: 1l)
        Customer customer = new Customer(id: 1l)
        Price price = new Price(price: 100)

        invoiceRowRedeemStrategy.redeemAmountType = InvoiceRowRedeemStrategy.RedeemAmountType.PERCENTAGE_BACK
        invoiceRowRedeemStrategy.amount = 80
        InvoiceRow invoiceRow = invoiceRowRedeemStrategy.redeem(user, customer, price, "DESC", Boolean.FALSE)

        assert invoiceRow
        assert invoiceRow.customer == customer
        assert invoiceRow.price == -80
    }

    @Test
    void testRedeemReturnesCorrectInvoiceRowWithPriceReductionBack() {
        User user = new User(id: 1l)
        Customer customer = new Customer(id: 1l)
        Price price = new Price(price: 100)


        invoiceRowRedeemStrategy.redeemAmountType = InvoiceRowRedeemStrategy.RedeemAmountType.PRICE_REDUCTION_BACK
        invoiceRowRedeemStrategy.amount = 30
        InvoiceRow invoiceRow = invoiceRowRedeemStrategy.redeem(user, customer, price, "DESC", Boolean.FALSE)

        assert invoiceRow
        assert invoiceRow.customer == customer
        assert invoiceRow.price == -70
    }

    @Test
    void testRedeemFully() {
        User user = new User(id: 1l)
        Customer customer = new Customer(id: 1l)
        Price price = new Price(price: 100)

        InvoiceRow invoiceRow = invoiceRowRedeemStrategy.redeem(user, customer, price, "DESC", Boolean.TRUE)

        assert invoiceRow
        assert invoiceRow.customer == customer
        assert invoiceRow.price == -100
    }

    @Test
    void testRedeemOnZeroPriceDoesntCreateInvoiceRow() {
        User user = new User(id: 1l)
        Customer customer = new Customer(id: 1l)
        Price price = new Price(price: 0)

        InvoiceRow invoiceRow = invoiceRowRedeemStrategy.redeem(user, customer, price, "DESC", Boolean.TRUE)

        assert !invoiceRow
    }
}
