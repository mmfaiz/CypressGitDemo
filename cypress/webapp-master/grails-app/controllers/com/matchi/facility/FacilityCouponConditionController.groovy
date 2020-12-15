package com.matchi.facility
import com.matchi.GenericController
import com.matchi.conditions.*
import com.matchi.coupon.Coupon
import com.matchi.coupon.CouponConditionGroup
import com.matchi.coupon.Offer
import com.matchi.coupon.PromoCode
import grails.validation.Validateable

class FacilityCouponConditionController extends GenericController {

    static def CONDITIONS_KEY = "com.matchi.coupons.slots.conditions_key"
    def couponService
    /**
     * Lists all condition groups from a given coupon id
     * @return
     */
    def list(String type) {
        Offer coupon = couponService.findCouponByTypeAndId(type, params.id)

        if(!coupon) {
            flash.error = message(code: "facilityCouponCondition.list.error")
            redirect(action: "index")
            return
        } else {
            // assert that user has access to this coupon
            assertFacilityAccessTo(coupon)
        }

        clearUnsaved()

        render(view: "list", model: [coupon: coupon])
    }

    /**
     * Displays a form for a coupon condition group
     *
     * @return
     */

    def form(String type) {
        def coupon = couponService.findCouponByTypeAndId(type, params.id)
        def group = new CouponConditionGroup()
        def conditionSets = []
        if(params.groupId) {
            group = CouponConditionGroup.get(params.groupId)
        }
        def availableConditions = getAvailableConditions(type)

        CouponConditionGroup unsaved = getUnsaved()

        if(group?.slotConditionSets) {
            conditionSets.addAll(group.slotConditionSets)
        }
        conditionSets.addAll(unsaved.slotConditionSets)

        [coupon: coupon , facility: getUserFacility(), availableConditions: availableConditions,
                conditionSets: conditionSets, group: group]
    }

    /**
     * Called when user is saving a condition group.
     * The added conditions in session is added to a persistent CouponConditionGroup.
     * @param cmd
     * @return view to render
     */
    def saveConditionGroup(SaveRuleCommand cmd) {
        def coupon = couponService.findCouponByTypeAndId(cmd.name, params.id)
        CouponConditionGroup group

        def unsaved = getUnsaved()

        if (params.groupId) {
            group = CouponConditionGroup.get(params.groupId)
        } else {
            group = new CouponConditionGroup()
        }

        group.name = cmd.name

        unsaved.slotConditionSets.each {
            group.addToSlotConditionSets(it)
        }

        coupon.addToCouponConditionGroups(group)
        if(coupon.save()) {
            clearUnsaved()
            flash.message = message(code: "facilityCouponCondition.saveConditionGroup.success")
        }

        redirect(action:  "list", id:  params.id, mapping: params.couponType + "Conditions")
    }

    /**
     * Removed a condition group
     * @return view to render
     */
    def removeConditionGroup() {
        CouponConditionGroup group = CouponConditionGroup.get(params.groupId)

        if (group) {
            group.delete()
            flash.message = message(code: "facilityCouponCondition.removeConditionGroup.success", args: [group.name])
        }

        redirect(action:  "list", id:  params.id, mapping: params.type + "Conditions")
    }

    /**
     * Called when user adds a new condition set to condition group. The set is saved in session storage and
     * added to the persistent object once the user saves the condition group.
     * @return the view to render
     */
    def addConditionSet() {
        SlotConditionSet conditionSet = new SlotConditionSet()
        addConditionsToSet(conditionSet)
        addUnsaved(conditionSet)

        if(conditionSet.slotConditions && !conditionSet.slotConditions.isEmpty()) {
            flash.message = message(code: "facilityCouponCondition.addConditionSet.success")
        } else {
            flash.error = message(code: "facilityCouponCondition.addConditionSet.error")
        }

        redirect(action: "form", id: params.id, params: [name: params.name, groupId: params.groupId], mapping: params.type + "Conditions")
    }

    def removeConditionSet() {
        def ruleId = params.ruleId

        log.info("Removing rule id ${ruleId}")

        // If rule id starts with 'H' the set is stored in session
        if (ruleId.startsWith("H")) {
            // remove from session storage
            def unsaved = getUnsaved();

            def rule = unsaved.slotConditionSets.find {
                def d = "H" + it.unsavedIdentifier
                d.equals(ruleId)
            }

            rule.each {
                unsaved.slotConditionSets.remove(rule)
            }
            session[CONDITIONS_KEY] = unsaved
        } else {
            // remove from persistant storage
            if(params.groupId) {
                CouponConditionGroup group = CouponConditionGroup.get(params.groupId)
                group.removeFromSlotConditionSets(SlotConditionSet.get(ruleId))
            }
        }

        flash.message = message(code: "facilityCouponCondition.removeConditionSet.success")

        redirect(action: "form", id: params.id, params: [name: params.name, groupId: params.groupId], mapping: params.type + "Conditions")
    }

    private void addConditionsToSet(SlotConditionSet conditionSet) {

        def availableConditions = getAvailableConditions()

        availableConditions.each { def condition ->
            condition.populate(params)

            if (condition.validate()) {
                conditionSet.addToSlotConditions(condition)
            } else {
                log.debug("Could not add condition of type ${condition.getType()} (${condition.errors})")
            }

        }
    }

    private void addUnsaved(SlotConditionSet conditionSet) {
        def unsaved = getUnsaved()
        if(conditionSet.slotConditions && !conditionSet.slotConditions.isEmpty()) {
            conditionSet.unsavedIdentifier = new Date().toString().encodeAsMD5()
            unsaved.slotConditionSets << conditionSet
        }
        session[CONDITIONS_KEY] = unsaved
    }

    private def getUnsaved() {
        def unsavedGroup = session[CONDITIONS_KEY]
        if (!unsavedGroup) {
            unsavedGroup = new CouponConditionGroup()
            unsavedGroup.slotConditionSets = []
            session[CONDITIONS_KEY] = unsavedGroup
        }
        return unsavedGroup
    }

    private void clearUnsaved() {
        session.removeAttribute(CONDITIONS_KEY)
    }

    private def getAvailableConditions(String type) {
        if (specifyOfferClass(type) == PromoCode.class)
            [ new DateSlotCondition(), new TimeSlotCondition(), new HoursInAdvanceBookableSlotCondition(),
              new WeekdaySlotCondition(), new CourtSlotCondition() ]
        else
            [ new DateSlotCondition(), new TimeSlotCondition(), new HoursInAdvanceBookableSlotCondition(),
                new WeekdaySlotCondition(), new CourtSlotCondition(), new ActivitySlotCondition() ]
    }

}

@Validateable(nullable = true)
class SaveRuleCommand {
    Long id
    String name
}
