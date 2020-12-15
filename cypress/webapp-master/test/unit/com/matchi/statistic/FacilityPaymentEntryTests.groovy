package com.matchi.statistic

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * Created by calle on 06/02/14.
 */
class FacilityPaymentEntryTests {

    FacilityPaymentEntry entry

    @Before
    public void setUp() {
        entry = new FacilityPaymentEntry(num: 10, revenue: 150, )
    }

    @Test
    public void testTotalRevenue() {
        assert entry.getTotalRevenue() == 1500
    }
}
