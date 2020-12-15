package com.matchi.api_ext

import com.matchi.Facility
import com.matchi.api_ext.model.APIExtActivity
import com.matchi.api_ext.model.APIExtCourse
import com.matchi.api_ext.model.APIExtCourseOccasion
import com.matchi.api_ext.model.APIExtEvent
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import org.joda.time.DateTime
import org.joda.time.Interval

class APIExtFacilityController extends APIExtGenericController {
    def slotService
    def activityService
    def mlcsService

    def facility(Long facilityId) {
        render getFacility(facilityId) as JSON
    }

    @Cacheable(value = "apiExtFacilities")
    def facilities() {
        render Facility.bookableAndActiveFacilities.listDistinct() as JSON
    }

    def sports(Long facilityId) {
        Facility facility = getFacility(facilityId)
        render facility.sports as JSON
    }

    /**
     * Return a list of courts with current summary details.
     * Intended use for external API integration to list the current status for one or several courts.
     * param.court = A list of court id's to filter on. Takes precedence over param.sport if available.
     * param.sport = A list of sport id's to filter on. Ignored if param.court is available.
     * @return
     */
    def courts(Long facilityId) {
        Facility facility = getFacility(facilityId)

        DateTime dateTime = DateTime.now()
        Date from = dateTime.toDate()
        Date to = dateTime.plusDays(1).withTimeAtStartOfDay().toDate()

        def courts = params.list("court")
        def sports = params.list("sport")

        def result = courtService.getCourtSummary(facility, from, to, getCourtIds(facility, courts, sports))
        render result as JSON
    }

    /**
     * Return a list of slots with summary details.
     * Intended use for external API integration to list current and upcoming slots.
     * param.date = A date as yyyy-MM-dd. If provided all slots for the date are returned.
     *     If omitted all slots from the current date and time are returned.
     * param.court = A list of court id's to filter on. Takes precedence over param.sport if available.
     * param.sport = A list of sport id's to filter on. Ignored if param.court is available.
     * @return
     */
    def slots(Long facilityId) {
        Facility facility = getFacility(facilityId)

        Date from
        Date to
        if (params.date) {
            DateTime dateTime = new DateTime(params.date)
            from = dateTime.withTimeAtStartOfDay().toDate()
            to = dateTime.plusDays(1).withTimeAtStartOfDay().toDate()
        } else {
            DateTime dateTime = DateTime.now()
            from = dateTime.toDate()
            to = dateTime.plusDays(1).withTimeAtStartOfDay().toDate()
        }

        def courts = params.list("court")
        def sports = params.list("sport")

        def result = slotService.getSlotSummary(facility, from, to, getCourtIds(facility, courts, sports))
        render result as JSON
    }

    /**
     * Get courses from Training planner with occasions, courts and participants.
     * Occasions are included based on from- and to-params.
     * @return
     */
    def courses(Long facilityId, APIExtFilterCommand cmd) {
        Facility facility = getFacility(facilityId)

        def result = []
        activityService.getCoursesWithPublishedForm(facility).each {courseActivity ->
            APIExtCourse apiExtCourse = new APIExtCourse(courseActivity)

            courseActivity.occasions?.each {activityOccasion ->
                if (activityOccasion.isUpcoming()) {
                    apiExtCourse.occasions.add(new APIExtCourseOccasion(activityOccasion))
                }
            }

            result << apiExtCourse
        }

        render result as JSON
    }

    /**
     * Get events
     * @return
     */
    def events(Long facilityId, APIExtFilterCommand cmd) {
        Facility facility = getFacility(facilityId)

        def result = []
        activityService.getOnlineEvents(facility).each {eventActivity ->
            APIExtEvent apiExtEvent = new APIExtEvent(eventActivity)
            result << apiExtEvent
        }

        render result as JSON
    }

    /**
     * Get activities with upcoming occasions
     * @param facilityId
     * @return
     */
    def activities(Long facilityId) {
        Facility facility = getFacility(facilityId)

        def result = []
        activityService.getActiveActivitiesWithOccasions(facility).each {activity ->
            result << new APIExtActivity(activity)
        }

        render result as JSON
    }

    /**
     * Retrieve MCLS schedule (same as MLCSController#schedule
     * @return
     */
    def lights(Long facilityId, APIExtFilterCommand cmd) {
        Facility facility = getFacility(facilityId)

        def result = mlcsService.buildScheduleResponse(
                facility,
                new Interval(cmd.from.toDateTimeAtStartOfDay(), cmd.to.toDateTimeAtStartOfDay()))

        mlcsService.updateFacilityMLCSHeartBeat(facility)
        render result as JSON
    }

}
