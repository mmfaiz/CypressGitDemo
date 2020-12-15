package com.matchi.fortnox

import com.matchi.fortnox.v3.FortnoxInvoice
import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime

class FortnoxInvoiceTests extends GrailsUnitTestCase {

    void testCreditDueDate() {
        DateTime current = new DateTime("2017-09-30")
        def total = -100
        assert FortnoxInvoice.createInternalInvoiceDueDate(current, total).equals(new DateTime("2017-10-15").toDate())
    }

    void testDebetDueDate() {
        DateTime current = new DateTime("2017-09-30")
        def total = 100
        assert FortnoxInvoice.createInternalInvoiceDueDate(current, total).equals(new DateTime("2017-11-14").toDate())
    }

}
