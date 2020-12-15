package com.matchi.coupon

import com.matchi.Slot
import com.matchi.conditions.SlotConditionSet
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CouponConditionGroup)
@Mock([CouponConditionGroup, SlotConditionSet])
class CouponConditionGroupTests {

    CouponConditionGroup group

    @Before
    void setUp() {
        group = new CouponConditionGroup()
    }

    void testReturnsTrueIfAnyIsValid() {
        addValidConditionSet()
        assert group.accept(new Slot())
    }

    void testReturnsTrueIfAnyValid() {
        addValidConditionSet()
        addInvalidConditionSet()
        addValidConditionSet()
        assert group.accept(new Slot())
    }

    void testReturnsTrueIfAnyValidInvalidFirst() {
        addInvalidConditionSet()
        addValidConditionSet()
        assert group.accept(new Slot())
    }

    void testReturnsTrueIfEmpty() {
        assert group.accept(new Slot())
    }

    private void addValidConditionSet() {
        group.addToSlotConditionSets(new TestSlotConditionSet(valid:  true))
    }

    private void addInvalidConditionSet() {
        group.addToSlotConditionSets(new TestSlotConditionSet(valid:  false))
    }
}

class TestSlotConditionSet extends SlotConditionSet {
    def valid

    @Override
    def accept(Slot slot) {
        return valid
    }
}