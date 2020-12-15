package com.matchi

import com.matchi.activities.ActivityOccasion
import org.joda.time.LocalDate

class ActivitiesHelper {
    static Collection<ActivityOccasion> filterOccasionsCollectionByCommand(Collection<ActivityOccasion> activityOccasions, GetActivitiesCommand cmd) {
        LocalDate startDate = new LocalDate(cmd.startDate)
        LocalDate endDate = new LocalDate(cmd.endDate)

        activityOccasions.findAll {
            if (!it.isUpcomingOnlineOccasion()) {
                return false
            }
            if (!((startDate.isBefore(it.date) || startDate.isEqual(it.date)) &&
                    (!endDate || (endDate.isAfter(it.date) || endDate.isEqual(it.date))))) {
                return false
            }
            if (cmd.bookingUser && cmd.hideBookedOccasions) {
                if (it.participations.find {
                    it.customer.user == cmd.bookingUser
                }) {
                    return false
                }
            }
            return true
        }
    }
}
