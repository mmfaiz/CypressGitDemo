package com.matchi.facility

import com.matchi.BookingRestriction
import com.matchi.GenericController
import com.matchi.RecurringSlotsContainer
import com.matchi.Slot
import com.matchi.requirements.RequirementProfile
import grails.validation.Validateable
import org.joda.time.DateTime

class FacilityBookingRestrictionsController extends GenericController {

    def slotService
    def groovySql

    enum ValidUntilUnit {
        MINUTES(1), HOURS(60), DAYS(1440), WEEKS(10080)

        private int mins

        ValidUntilUnit(int mins) {
            this.mins = mins
        }

        static List toList() {
            return [MINUTES, HOURS, DAYS, WEEKS]
        }

        int convertToMinutes(int value) {
            return value * this.mins
        }
    }

    def create() {
        def currentFacility = getUserFacility()

        List<RequirementProfile> requirementProfiles = RequirementProfile.findAllByFacility(currentFacility)

        List<Slot> slots = Slot.findAllById(params.slotIds.split(','))

        [ requirementProfiles: requirementProfiles, restrictionSlotData: params.slotIds, date: params.date, slots: slots ]
    }

    def save(SaveBookingRestrictionCommand cmd) {
        if (cmd.hasErrors()) {
            flash.error = message(code: "facilityBookingRestriction.modal.flash.error")
            redirect(controller: "facilityBooking", action: "index", params: [date: params.date != "null" ? params.date : ""])
            return
        }

        List<Slot> slots = Slot.findAllByIdInList(cmd.slotIds())
        if(cmd.useRecurrence) {
            RecurringSlotsContainer recurrenceSlots = slotService.getRecurrenceSlots(new DateTime(cmd.recurrenceStart), new DateTime(cmd.recurrenceEnd), cmd.weekDays*.toString(), cmd.frequency, cmd.interval, slots)

            // Add restrictions to all the slots, despite booking or not. But we cannot replace current booking restriction
            slots = (recurrenceSlots.freeSlots + recurrenceSlots.unavailableSlots).toList().findAll { Slot slot ->
                slot.bookingRestriction == null
            }
        }

        List<RequirementProfile> requirementProfiles = RequirementProfile.findAllByIdInList(cmd.requirementProfiles)
        ValidUntilUnit validUntilUnit = cmd.validUntilUnit as ValidUntilUnit

        BookingRestriction restriction = new BookingRestriction()
        restriction.validUntilMinBeforeStart = validUntilUnit.convertToMinutes(cmd.validUntilBeforeStart)
        restriction.slots = slots
        restriction.requirementProfiles = requirementProfiles

        requirementProfiles.each {
            it.addToBookingRestrictions(restriction)
        }

        slots.each {
            it.bookingRestriction = restriction
        }

        if (restriction.hasErrors() || !restriction.save()) {
            flash.error = message(code: "facilityBookingRestriction.modal.flash.error")
        } else {
            flash.message = message(code: "facilityBookingRestriction.modal.created.flash")
        }

        redirect(controller: "facilityBooking", action: "index", params: [date: params.date != "null" ? params.date : ""])
    }

    def delete(DeleteBookingRestrictionCommand cmd) {
        List<Slot> slots = Slot.findAllByIdInList(cmd.slotIds(), [bookingRestrictions: "join"])
        List<BookingRestriction> restrictions =  slots.collect { it.bookingRestriction }?.unique()

        restrictions.each { BookingRestriction restriction ->
            slots?.each { Slot s ->
                if (restriction?.slots?.contains(s)) {
                    restriction?.removeFromSlots(s)
                    s.bookingRestriction = null
                }
            }

            if (restriction?.slots?.isEmpty()) {
                List<RequirementProfile> toCleanUp = []
                toCleanUp.addAll(restriction.requirementProfiles)

                toCleanUp.each {
                    String query = """
                            delete from booking_restriction_requirement_profiles where
                            restriction_id = ? and profile_id = ?;
                            """
                    groovySql.executeUpdate(query, [restriction.id, it.id])
                }
                restriction.delete()
            }
        }

        flash.message = "Slot restriction(s) deleted"
        redirect(controller: "facilityBooking", action: "index", params: [date: params.date != "null" ? params.date : ""])
    }
}

@Validateable(nullable = true)
class SaveBookingRestrictionCommand {
    String restrictionSlots
    List<Long> requirementProfiles

    Boolean useRecurrence
    String recurrenceStart
    String recurrenceEnd
    List<Integer> weekDays
    int frequency
    int interval

    int validUntilBeforeStart = 0
    String validUntilUnit = FacilityBookingRestrictionsController.ValidUntilUnit.MINUTES.name()

    static constraints = {
        restrictionSlots nullable: false, blank: false
        requirementProfiles nullable: false, blank: false
        validUntilBeforeStart nullable: true, blank: true
        recurrenceStart(nullable: true)
        recurrenceEnd(nullable: true)
        weekDays(nullable: true)
        frequency(nullable: true)
        interval(nullable: true)
        interval(nullable: false)
    }

    def slotIds() {
        return Slot.parseAll(restrictionSlots)
    }
}

@Validateable(nullable = true)
class DeleteBookingRestrictionCommand {
    String delRestrictionSlotData
    List<Long> restrictionIds

    static constraints = {
        delRestrictionSlotData nullable: false
        restrictionIds nullable: false
    }

    def slotIds() {
        return Slot.parseAll(delRestrictionSlotData)
    }
}