package com.matchi


import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.payment.PaymentFlow
import grails.validation.Validateable
import groovy.transform.ToString
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

class ActivityController extends GenericController {

    ActivityService activityService
    def facilityService

    def index(GetActivitiesCommand cmd) {
        PaymentFlow paymentFlow = PaymentFlow.getFinished(session, params.long("orderId"))
        [cmd: cmd, paymentFlow: paymentFlow, user: getCurrentUser()]
    }

    def findActivities(GetActivitiesCommand cmd) {
        LocalDate startDate = new LocalDate(cmd.startDate)
        LocalDate endDate = cmd?.endDate ? new LocalDate(cmd.endDate) : null

        Map<LocalDate, Map<ClassActivity, List<ActivityOccasion>>> occasionsByActivityByDate = activityService.searchForClassActivityOccasionsByDateAndActivity(cmd.sportIds, startDate, endDate, cmd.locationSearch, cmd.querySearch, cmd.level)
        render view: "activities", model: [occasionsByActivityByDate: occasionsByActivityByDate, cmd: cmd, user: currentUser]
    }
}

@ToString
@Validateable(nullable = true)
class GetActivitiesCommand {
    Collection<Long> sportIds = []
    Collection<Long> facilityIds = []
    Date startDate = new Date()
    Date endDate
    String locationSearch = ""
    String querySearch = ""

    List<DateTimeConstants> weekDays = []

    Boolean hideBookedOccasions = false
    Boolean hideFullyBooked = false
    Boolean exposeOccasions = false
    Long singleOccasionId = null

    Integer level

    //passed to marshaller
    User bookingUser = null
    ArrayList<ActivityOccasion> predefinedActivityOccasions = null


    String toString() {
        return properties.toMapString()
    }
}