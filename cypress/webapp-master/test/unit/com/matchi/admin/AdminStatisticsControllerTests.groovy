package com.matchi.admin

import com.matchi.FacilityContract
import grails.test.mixin.TestFor
import org.junit.Before

@TestFor(AdminStatisticsController)
class AdminStatisticsControllerTests {
    private FacilityContract contract

    @Before
    public void setUp() {
        contract = new FacilityContract(variableMediationFee: 10, variableMediationFeePercentage: 3.15)
    }

    void test() {

    }
}
