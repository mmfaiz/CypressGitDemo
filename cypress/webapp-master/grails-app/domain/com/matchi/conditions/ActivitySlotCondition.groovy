package com.matchi.conditions


import com.matchi.Slot
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.enums.BookingGroupType
import grails.util.Holders

class ActivitySlotCondition extends SlotCondition {

    static hasMany = [activities: ClassActivity]
    static mapping = {
        activities joinTable: [name: 'activity_slot_condition_class_activity', key: 'activity_slot_condition_activities_id', column: 'activity_id']
    }
    boolean notValidForActivities

    @Override
    boolean accept(Slot slot) {
        def accept = false

        if(slot && slot.booking) {
            if (slot.booking?.group?.type?.equals(BookingGroupType.ACTIVITY)) {
                if (notValidForActivities) {
                    return false
                }

                def activityService = Holders.grailsApplication.mainContext.getBean('activityService')

                Map<Slot,ActivityOccasion> occasionsBySlot = activityService.getOccasionsBySlots([slot]) as Map<Slot, ActivityOccasion>
                ActivityOccasion occasion = occasionsBySlot.get(slot)

                if(occasion) {
                    accept = activities?.any { activity -> activity.id == occasion.activity.id }
                }
            } else {
                accept = activities?.size() <= 0
            }
        } else if (slot && notValidForActivities) {
            accept = true
        }

        return accept
    }

    @Override
    void populate(def params) {
        params.list("activities").each {
            addToActivities(ClassActivity.get(it))
        }

        if(params.boolean("noActivities")) {
            this.notValidForActivities = params.boolean("noActivities")
        }
    }

    String getType() {
        return "ACTIVITIES"
    }

    static transients = ['type']

    static constraints = {
        activities(validator: { activities, obj ->

            if(!obj.properties['notValidForActivities']) {
                return ((activities && activities.size() > 0)?true:['invalid'])
            }

            return true
        })
    }
}
