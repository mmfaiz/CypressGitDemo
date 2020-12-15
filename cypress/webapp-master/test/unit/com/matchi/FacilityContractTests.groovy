package com.matchi

import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(FacilityContract)
class FacilityContractTests {

    FacilityContract domain

    @Before
    void setUp() {
        domain = new FacilityContract()
    }

    void testUsePercentageWithNull() {
        assert !domain.useMinimumFee(null)
    }

    void testUseMinimumFee() {

        domain.variableMediationFeePercentage = 10.0
        domain.variableMediationFee           = 10

        assert domain.useMinimumFee(50)
    }

    void testUseMinimumFeeReturnsFalse() {

        domain.variableMediationFeePercentage = 10.0
        domain.variableMediationFee           = 10

        assert !domain.useMinimumFee(500)
    }

    void testUseMinimumFeeReturnsTrueIfSame() {

        domain.variableMediationFeePercentage = 10.0
        domain.variableMediationFee           = 10

        assert domain.useMinimumFee(100)
    }

    void testMinialFeeThreshold() {
        domain.variableMediationFee = 10
        domain.variableMediationFeePercentage = 3.15

        assert domain.getMinimalFeeThreshold() == 317
    }
}
