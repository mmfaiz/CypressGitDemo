package com.matchi.api_ext

import com.matchi.Court
import com.matchi.Facility
import com.matchi.Sport
import com.matchi.api.Code
import com.matchi.api.GenericAPIController
import grails.validation.Validateable
import org.joda.time.LocalDate

class APIExtGenericController extends GenericAPIController {
    def courtService

    /**
     * Get a Facility based on it's id.
     * @param facilityId
     * @return
     */
    Facility getFacility(Long facilityId) {
        Facility facility = Facility.get(facilityId)

        if(facility) {
            return facility
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Facility not found")
        }
    }

    /**
     * Get a list of Sports based on a list of id's.
     * @param sportIds
     * @return
     */
    def getSports(def sportIds) {
        def sports = []
        sportIds?.each {sports << Sport.get(it)}
        return sports
    }

    /**
     * Get courts based on a list of id's and verify that they belong to the facility
     * and are not archived. If a list of sport id's is used then get the courts
     * for the facility based on sports.
     * @param facility
     * @param courtIds
     * @param sportIds
     * @return
     */
    def getCourtIds(Facility facility, def courtIds, def sportIds) {
        def courts = []

        if (!courtIds) {
            courtService.findCourts([facility], getSports(sportIds)).each { Court court -> courts << court.id}
        } else {
            courtIds.each {
                Court court = Court.get(it)
                if (court && court.facilityId == facility.id && !court.archived) {
                    courts << court.id
                }
            }
        }

        return courts
    }

}

@Validateable(nullable = true)
class APIExtFilterCommand {
    Integer max
    Integer offset
    LocalDate from
    LocalDate to
    String order

    static constraints = {
        max nullable: true
        offset nullable: true
        from nullable: true
        to nullable: true
        order nullable: true
    }

    Integer getMax() {
        this.max = max ?: 50
        return max
    }

    Integer getOffset() {
        this.offset = offset ?: 0
        return offset
    }

    LocalDate getFrom() {
        this.from = from ?: LocalDate.now()
        return from
    }

    LocalDate getTo() {
        this.to = to ?: from.plusDays(1)
        return to
    }

    String getOrder() {
        this.order = order ?: "asc"
        return order
    }
}