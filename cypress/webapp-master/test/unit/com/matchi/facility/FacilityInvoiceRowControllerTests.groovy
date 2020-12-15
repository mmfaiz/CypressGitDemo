package com.matchi.facility

import com.matchi.invoice.InvoiceRow
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test


@TestFor(FacilityInvoiceRowController)
@Mock([ InvoiceRow ])
class FacilityInvoiceRowControllerTests {

    void testRemoveNullRowsFromCommand() {

        InvoiceRowsCommand rows = new InvoiceRowsCommand()
        rows.rows << null
        rows.rows << new InvoiceRowCommand()
        rows.rows << null
        rows.rows << new InvoiceRowCommand()

        assert rows.rows.size() == 4

        rows.clearNullRows()

        assert rows.rows.size() == 2
    }

    @Test
    void testHasRowId() {
        InvoiceRowsCommand rows = new InvoiceRowsCommand()

        rows.rows << new InvoiceRowCommand(rowId: 1)
        rows.rows << new InvoiceRowCommand(rowId: 4)
        rows.rows << new InvoiceRowCommand(rowId: 66)

        assert rows.hasRowId(1)
        assert rows.hasRowId(4)
        assert rows.hasRowId(66)

        assert !rows.hasRowId(null)
        assert !rows.hasRowId(5)
        assert !rows.hasRowId(2000)
    }
}
