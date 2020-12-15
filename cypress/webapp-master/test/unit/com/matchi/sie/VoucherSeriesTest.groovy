package com.matchi.sie

import com.matchi.sie.Document.VoucherSeries
import org.junit.Before
import org.junit.Test

class VoucherSeriesTest extends GroovyTestCase {

    @Before
    void setUp() {
    }

    protected series(Boolean creditor, String type)
    {
        return (new VoucherSeries()).selfFor(creditor, type)
    }

    @Test
    void testSelfForTrueInvoiceExpectLF()
    {
        String result = this.series(true, "invoice")
        assert result.equals("LF")
    }

    @Test
    void testSelfForTruePaymentExpectKB()
    {
        String result = this.series(true, "payment")
        assert result.equals("KB")
    }

    @Test
    void testSelfForFalseInvoiceExpectKF()
    {
        String result = this.series(false, "invoice")
        assert result.equals("KF")
    }

    @Test
    void testSelfForFalsePaymentExpectKI()
    {
        String result = this.series(false, "payment")
        assert result.equals("KI")
    }

    @Test
    void testSelfForTrueManualBookableExpectLV()
    {
        String result = this.series(true, "manual_bookable")
        assert result.equals("LV")
    }
}
