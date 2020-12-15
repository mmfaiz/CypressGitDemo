package com.matchi

import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityContractItem)
class FacilityContractItemTests {

    void testConstraints() {
        mockForConstraintsTests(FacilityContractItem)

        def obj = new FacilityContractItem()
        assert !obj.validate()
        assert 4 == obj.errors.errorCount
        assert "nullable" == obj.errors.description
        assert "nullable" == obj.errors.price
        assert "nullable" == obj.errors.contract
        assert "validator" == obj.errors.chargeMonths

        obj = new FacilityContractItem(description: "a" * 256)
        assert !obj.validate()
        assert "maxSize" == obj.errors.description

        obj = new FacilityContractItem(description: "a" * 255, price: 99.99,
                contract: new FacilityContract(), chargeMonths: [1])
        assert obj.validate()
    }
}
