package com.matchi

import org.joda.time.LocalDate
import org.joda.time.DateTime

class ObjectWatchTagLib {

    def slotService
    def springSecurityService
    def courtService

    def addSlotWatch = { attr, body ->

        Facility facility = attr.facility
        DateTime now = new DateTime()
        DateTime date = attr.date ? new DateTime(formatDate(date: new LocalDate(attr.date).toDate(), format: 'yyyy-MM-dd')) : now

        def user = springSecurityService.currentUser

        List<Long> sports = null

        if (attr.sports instanceof String[]) {
            sports = attr.sports.findAll { String it ->
                try {
                    Long.parseLong(it)
                    return true
                } catch (NumberFormatException e) {
                    return false
                }
            }.toList()
        } else if (attr.sports instanceof String) {
            try {
                sports = [Long.parseLong(attr.sports as String)].toList()
            } catch (NumberFormatException e) {
            }
        } else {
            sports = attr.sports
        }
        // Only show this feature if the date within facilities bookable setting
        if (!facility?.isBookableForUser(date, user) || !sports) {
            return
        }

        def slotHours = slotService.getDaySlotHours(date, facility.id, sports)
        def watchSports = sports ? Sport.findAllByIdInList(sports) : null
        def courts = courtService.findUsersCourts([facility], watchSports, null, null, user)

        out << render(template: "/templates/slotWatch/queueForm",
                model: [date     : date, facility: facility, courts: courts,
                        slotHours: slotHours, user: user, watchSports: watchSports])
    }
}
