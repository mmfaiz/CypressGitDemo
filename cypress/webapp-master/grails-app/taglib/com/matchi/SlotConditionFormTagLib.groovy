package com.matchi

import com.matchi.activities.ClassActivity

class SlotConditionFormTagLib {

    static String templatePrefix = "slot_condition_"

    def springSecurityService


    def slotConditionForm = { attrs, body ->
        def user = springSecurityService.currentUser
        def facility = user.facility
        def courts = Court.available(facility).list()
        def activities = ClassActivity.findAllByFacility(facility)

        def condition = attrs.condition
        def conditionName =  getTemplateNameFromCondition(condition)
        out << render(template:"/templates/conditions/slot/${conditionName}.form", model: [courts: courts.sort { it.facility }, activities: activities])
    }

    def slotConditionEntry = { attrs, body ->
        def condition = attrs.condition
        def conditionName =  getTemplateNameFromCondition(condition)
        out << render(template:"/templates/conditions/slot/${conditionName}", model: [condition: condition])
    }

    def slotConditionFormatted = { attrs, body ->
        def condition = attrs.condition
        def conditionName =  getTemplateNameFromCondition(condition)
        out << render(template:"/templates/conditions/slot/formatted/${conditionName}", model: [condition: condition])
    }

    private def getTemplateNameFromCondition(def condition) {
        return "${templatePrefix}${condition?.type?.toLowerCase()}"
    }
}
