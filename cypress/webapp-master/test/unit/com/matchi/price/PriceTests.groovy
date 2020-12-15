package com.matchi.price

import com.matchi.Facility
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(Price)
@Mock(Facility)
class PriceTests {

    Price price = null
    Facility facility

    @Before
    void setUp() {
        price = new Price()
        price.price = 100L

        price.customerCategory = new PriceListCustomerCategory()
        facility = new Facility(vat: 0)
        price.customerCategory.facility = facility
    }

    @Test
    void testPriceExVatEqualsPriceIfVatIsZero() {
        assert price.priceExVAT == 100
    }

    @Test
    void testPriceExVatWithVat() {
        facility.vat = 10
        assert price.priceExVAT == 90.91
    }

    @Test
    void testPriceExVatWithVatRounding() {
        price.price = 99
        facility.vat = 7
        println price.priceExVAT
        assert price.priceExVAT == 92.52
        assert price.VATAmount == 6.48
    }

    @Test
    void testVatAmount() {
        facility.vat = 25
        println price.priceExVAT
        assert Price.calculateVATAmount(price.price, new Double(facility.vat)) == 20
    }
}
