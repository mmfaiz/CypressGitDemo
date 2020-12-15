package com.matchi.price

import com.matchi.Court
import com.matchi.Customer
import com.matchi.Slot
import grails.test.mixin.TestFor
import org.junit.Before

@TestFor(CourtPriceCondition)
class CourtPriceConditionTests {

    CourtPriceCondition condition

    @Before
    public void setUp() {

        def court1 = new Court() ; court1.id = 1
        def court2 = new Court() ; court2.id = 2

        condition = new CourtPriceCondition()
        condition.courts = []
        condition.courts << court1
        condition.courts << court2
    }

    void testAcceptOnSameId() {
        def court = new Court() ; court.id = 1
        assert condition.accept(new Slot(court: court), new Customer())
    }

    void testDoNotAcceptOnDifferentId() {
        def court = new Court() ; court.id = 3
        assert !condition.accept(new Slot(court: court), new Customer())
    }

    void testDoNotAcceptOnNoCourts() {
        condition.courts = []
        def court = new Court() ; court.id = 3
        assert !condition.accept(new Slot(court: court), new Customer())
    }
}
