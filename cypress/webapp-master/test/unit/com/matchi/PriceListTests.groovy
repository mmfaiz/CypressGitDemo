package com.matchi

import com.matchi.price.*
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.joda.time.DateTime
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(PriceList)
@Mock([Court, Customer, Facility, Municipality, Price, PriceListConditionCategory, Region, Slot, Sport, User])
class PriceListTests extends Specification {

    def facility
    def customer
    def slot30
    def slot50
    def slot60
    def slot90
    def slot120

    def setup() {
        facility = createFacility()
        customer = createCustomer(facility)
        slot30 = createSlot(createCourt(facility), new DateTime().withTime(10, 0, 0, 0).toDate(),
                new DateTime().withTime(10, 30, 0, 0).toDate())
        slot50 = createSlot(createCourt(facility), new DateTime().withTime(10, 0, 0, 0).toDate(),
                new DateTime().withTime(10, 50, 0, 0).toDate())
        slot60 = createSlot(createCourt(facility), new DateTime().withTime(10, 0, 0, 0).toDate(),
                new DateTime().withTime(11, 00, 0, 0).toDate())
        slot90 = createSlot(createCourt(facility), new DateTime().withTime(10, 0, 0, 0).toDate(),
                new DateTime().withTime(11, 30, 0, 0).toDate())
        slot120 = createSlot(createCourt(facility), new DateTime().withTime(10, 0, 0, 0).toDate(),
                new DateTime().withTime(12, 0, 0, 0).toDate())
    }

    void "getBookingPrice returns the same price for any slot length if type is slot based"() {
        def pl = createPriceListWithType(PriceList.Type.SLOT_BASED)

        expect:
        pl.getBookingPrice(customer, slot30)?.price == 200L
        pl.getBookingPrice(customer, slot50)?.price == 200L
        pl.getBookingPrice(customer, slot60)?.price == 200L
        pl.getBookingPrice(customer, slot90)?.price == 200L
        pl.getBookingPrice(customer, slot120)?.price == 200L
    }

    void "getBookingPrice returns different prices for slots with different length if type is hour based"() {
        def pl = createPriceListWithType(PriceList.Type.HOUR_BASED)

        expect:
        pl.getBookingPrice(customer, slot30)?.price == 100L
        pl.getBookingPrice(customer, slot50)?.price == 167L
        pl.getBookingPrice(customer, slot60)?.price == 200L
        pl.getBookingPrice(customer, slot90)?.price == 300L
        pl.getBookingPrice(customer, slot120)?.price == 400L
    }

    private PriceList createPriceListWithType(PriceList.Type type) {
        def pl = createPriceList(facility)
        pl.type = type
        pl.priceListConditionCategories = [
                new PriceListConditionCategory(name: "n", defaultCategory: true, conditions: [])
                        .addToPrices(new Price(price: 200,
                                priceCategory: new PriceListConditionCategory(),
                                customerCategory: new PriceListCustomerCategory()))]
        pl.save(failOnError: true)
    }
}
