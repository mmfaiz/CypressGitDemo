package com.matchi.orders

import com.matchi.*
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(InvoiceOrderPayment)
@Mock([Customer, Facility, InvoiceRow, Municipality, Region, User, Membership])
class InvoiceOrderPaymentTests {

    void testRefund() {
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def invoiceRow = new InvoiceRow(customer: customer, amount: 1, createdBy: user)
                .save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user, invoiceRow: invoiceRow,
                status: OrderPayment.Status.CAPTURED).save(failOnError: true)

        payment.refund(100.0)

        assert !InvoiceRow.count()
        assert !payment.invoiceRow
        assert OrderPayment.Status.CREDITED == payment.status
        assert 100.0 == payment.credited
    }

    @Test
    void testAllowLateRefund() {
        assert domain.allowLateRefund()
    }
}