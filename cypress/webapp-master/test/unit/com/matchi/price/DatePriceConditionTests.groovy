package com.matchi.price
import com.matchi.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(DatePriceCondition)
@Mock([Slot,Court, Sport,Region,Municipality,Facility])
class DatePriceConditionTests {

    void setUp() {
        Region region = new Region(name: '1', lat: 12, lng: 12).save(flush: true, failOnError: true)
        Municipality municipality = new Municipality(name: "1", lat: 12, lng: 12, region: region).save(flush: true, failOnError: true)
        Facility facility = new Facility(name: "1", shortname: "1", lat: 123, lng: 123, vat: 12, municipality: municipality, country: "sv", email: "facility@matchi.se").save(flush: true, failOnError: true)
        Sport sport = TestUtils.createSport()
        Court court = new Court(name: "123", sport: sport, facility: facility, listPosition: 1).save(flush: true, failOnError: true)
        new Slot(startTime: new Date(), endTime: new Date() + 1, court: court).save(flush: true, failOnError: true)
        new Slot(startTime: new Date()-2, endTime: new Date() - 1, court: court).save(flush: true, failOnError: true)
        new Slot(startTime: new Date()+2, endTime: new Date() + 3, court: court).save(flush: true, failOnError: true)
    }

    void testDatePriceConditionAccept() {
        DatePriceCondition condition = new DatePriceCondition(startDate: new Date(), endDate: new Date() + 1)
        Slot slot = Slot.get(1)
        Slot slotBefore = Slot.get(2)
        Slot slotAfter = Slot.get(3)
        assert condition.accept(slot, new Customer())
        assert !condition.accept(slotBefore, new Customer())
        assert !condition.accept(slotAfter, new Customer())
    }

}
