package com.matchi.statistic

import com.matchi.Facility
import com.matchi.FacilityContract
import com.matchi.FacilityContractItem
import com.matchi.Municipality
import com.matchi.Region
import grails.test.mixin.Mock
import org.joda.time.DateTime
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * Created by calle on 07/02/14.
 */
@Mock([Facility, FacilityContract, FacilityContractItem, Municipality, Region])
class FacilityPaymentSummaryTests {

    FacilityPaymentSummary summary
    def facility
    def contract

    @Before
    public void setUp() {
        contract = createFacilityContract()
        facility = contract.facility
        contract.fixedMonthlyFee = 1000
        contract.variableMediationFeePercentage = 3.15
        contract.variableMediationFee = 10
        contract.save()

        summary = new FacilityPaymentSummary(facility, [], new Interval(new DateTime().plusHours(1), new DateTime().plusDays(10)), [], [])

    }

    @Test
    public void testTotalRevenue() {
        addEntry("BOOKING", 100, 10)
        addPromoCode("promo_code", 10, 100)
        assert summary.getTotalRevenue() == 900
    }

    @Test
    public void testVariableFees() {
        addEntry("BOOKING", 100, 10)
        assert summary.getTotalVariableFees() == 100
    }

    @Test
    public void testTotalMonthlyProfit() {
        addEntry("BOOKING", 150, 10)
        addPromoCode("promo_code", 10, 100)

        assert summary.getTotalMonthlyProfit() == 300
    }

    void addEntry(type, price, num) {
        summary.entries.add new FacilityPaymentEntry(type: type, price: price, num: num, revenue: price)
    }

    void addPromoCode(type, count, total) {
        summary.promoCodeDiscounts.add new FacilityPromoDiscountEntry(type: type, count: count, total: total)
    }
}
