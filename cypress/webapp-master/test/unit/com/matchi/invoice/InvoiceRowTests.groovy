package com.matchi.invoice

import grails.test.mixin.*
import org.joda.time.DateTime
import org.junit.*
import java.text.DecimalFormat

@TestFor(InvoiceRow)
class InvoiceRowTests {

    def row

    @Before
    public void setUp() {
        row = new InvoiceRow()
    }

    @Test
    void testToStringFormatting() {
        row.amount = 1
        row.price  = 120
        row.description = "Artikel"
        assert row.toString() == "Artikel ${new DecimalFormat('###,##0.00').format(120)}"
    }

    @Test
    void testGetTotalVAT() {
        row.amount = 1
        row.price  = 100
        row.vat = 25
        row.discount = 0

        assert row.getTotalVAT() == 20
    }

    @Test
    void testGetTotalIncludingVAT() {
        row.amount = 10
        row.price  = 5
        row.vat = 10

        assert row.getTotalIncludingVAT() == 50
    }

    @Test
    void testGetTotalExcludingVAT() {
        row.amount = 2
        row.price  = 20
        row.vat = 50

        assert row.getTotalExcludingVAT().setScale(2,1) ==
                new BigDecimal(26.66).setScale(2,1)
    }

    @Test
    void testGetTotalWithDiscount() {
        row.amount   = 10
        row.price    = 5
        row.vat      = 10
        row.discount = 30

        assert row.getTotalIncludingVAT() == 20
    }

    @Test
    void testGetPriceVAT() {

        row.amount = 2
        row.price = 100
        row.vat   = 25

        assert row.getPriceVAT() == 20
    }

    @Test
    void testIsNewReturnsTrue() {
        row.dateCreated = new DateTime()
        assert row.isNew()
    }

    @Test
    void testIsNewReturnsFalseIfOld() {
        row.dateCreated = new DateTime().minusMinutes(10)
        assert !row.isNew()
    }
}
