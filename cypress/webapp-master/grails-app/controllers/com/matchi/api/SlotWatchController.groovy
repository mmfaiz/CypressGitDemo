package com.matchi.api

import com.matchi.Court
import com.matchi.Facility
import com.matchi.Sport
import com.matchi.User
import com.matchi.watch.SlotWatch
import grails.converters.JSON
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.joda.time.DateTime

class SlotWatchController extends GenericAPIController {

    final String dateFormat = "yyyy-MM-dd"
    
    def objectWatchNotificationService
    def userService
    def dateUtil

    def list() {
        def slotwatches = SlotWatch.createCriteria().list {
            if (params.myDate) {
                ge("fromDate", dateUtil.beginningOfDay(params.date("myDate", dateFormat))?.toDate())
                le("toDate", dateUtil.endOfDay(params.date("myDate", dateFormat))?.toDate())
            }

            if (params.facility) {
                eq("facility", Facility.get(params.facility))
            }

            eq("user", getCurrentUser())
            order("fromDate","asc")
        }

        render slotwatches as JSON
    }

    def add(AddSlotWatchCommand cmd) {
        log.debug("Adding slot watch")
        User user = userService.getLoggedInUser()

        if (!user) {
            error(403, Code.ACCESS_DENIED, "User not logged in")
            return
        }

        if(!cmd.validate()) {
            error(400, Code.INPUT_ERROR, "Invalid input")
            return
        }
        
        Facility facility = Facility.get(cmd.facilityId)
        Court court = Court.get(cmd.courtId)
        def sport = !court && cmd.sportIds?.size() == 1 ? Sport.get(cmd.sportIds[0]) : null

        def watch = objectWatchNotificationService.addNotificationFor(
                user, facility, court, sport, cmd.fromAsDateTime(), cmd.toAsDateTime(), cmd.smsNotify
        )

        if (watch) {
            render watch as JSON
        } else {
            error(400, Code.INPUT_ERROR, "Invalid input")
        }
    }

    def remove() {
        def slotWatch = SlotWatch.get(params.id instanceof String ? Long.parseLong(params.id) : params.id)

        log.debug("Removing ${slotWatch?.id} SlotWatch")

        if (!slotWatch) {
            error(400, Code.INPUT_ERROR, "Invalid input")
            return
        }
        
        slotWatch?.delete()
        render status: HttpStatus.SC_OK
    }
}

@Validateable(nullable = true)
class AddSlotWatchCommand {

    Long facilityId
    Long courtId
    String fromDate
    String fromTime
    boolean smsNotify = false
    List<Long> sportIds

    DateTime fromAsDateTime() {
        def timeOfDay = fromTime.tokenize(':')

        return new DateTime(fromDate).withHourOfDay(timeOfDay[0]?.toInteger()).withMinuteOfHour(timeOfDay[1]?.toInteger())
    }

    DateTime toAsDateTime() {
        return fromAsDateTime().plusHours(1)
    }

    @Override
    public String toString() {
        return "AddSlotWatchCommand{" +
                "facilityId=" + facilityId +
                ", courtId=" + courtId +
                ", fromDate=" + fromDate +
                ", fromTime=" + fromTime +
                ", smsNotify=" + smsNotify +
                //", to=" + to +
                '}';
    }
}
