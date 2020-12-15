package com.matchi.conditions

import com.matchi.Slot
import com.matchi.coupon.NotValidCondition
import com.matchi.coupon.ValidCondition
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(SlotConditionSet)
@Mock([SlotConditionSet, SlotCondition])
class SlotConditionSetTests {

    SlotConditionSet conditionSet

    @Before
    public void setUp() {
        conditionSet = new SlotConditionSet()
    }

    void testReturnsTrueIfAllIsValid() {
        conditionSet.addToSlotConditions(new ValidCondition())
        conditionSet.addToSlotConditions(new ValidCondition())

        assert conditionSet.accept(new Slot())
    }

    void testReturnsFalseIfSomeAreInvalid() {
        conditionSet.addToSlotConditions(new ValidCondition())
        conditionSet.addToSlotConditions(new NotValidCondition())

        assert !conditionSet.accept(new Slot())
    }

    void testReturnsFalseIfEmpty() {
        assert !conditionSet.accept(new Slot())
    }
}
