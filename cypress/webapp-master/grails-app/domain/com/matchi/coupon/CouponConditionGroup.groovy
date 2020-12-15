package com.matchi.coupon

import com.matchi.conditions.SlotConditionSet
import com.matchi.Slot

class CouponConditionGroup implements Serializable {
    static hasMany = [slotConditionSets: SlotConditionSet]
    static belongsTo = [ coupon: Offer]

    String name

    static constraints = {
    }

    def accept(Slot slot) {

        if(!slotConditionSets || slotConditionSets.isEmpty()) {
            return true
        }
        if(slotConditionSets) {
            def validConditionSets = slotConditionSets.any() { it.accept(slot) }
            return validConditionSets
        }

    }

    static mapping = {
        slotConditionSets joinTable: [name: 'coupon_condition_groups_slot_conditions_sets',
                                   column: 'slot_condition_set_id',
                                   key: 'coupon_condition_group_id']
    }
}
