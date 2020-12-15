package com.matchi.api
import com.matchi.Booking
import com.matchi.FacilityProperty
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString
import org.apache.http.HttpStatus
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDateTime

class BackhandSmashController extends GenericAPIController {

    def bookingService

    def facility() {
        render facilityService.getFacility(request.facilityId) as JSON
    }

    def cancelBooking(ApiCancelBookingCommand cmd) {
        log.debug("Cancel booking")
        log.debug(params)
        def facility = facilityService.getFacility(request.facilityId)
        def acceptedCustomerId = facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID.name())?.tokenize(",")

        List<Long> ids = []
        acceptedCustomerId.each {
            ids << Long.parseLong(it)
        }

        if(cmd.hasErrors()) {
            response.status = HttpStatus.SC_BAD_REQUEST
            render cmd.errors as JSON
            return false
        } else if (!acceptedCustomerId) {
            render status: HttpStatus.SC_NOT_FOUND, text: "Missing setting"
            return false
        }

        Booking booking = Booking.createCriteria().get {
            createAlias("customer", "cu", CriteriaSpecification.LEFT_JOIN)
            createAlias("slot", "s", CriteriaSpecification.LEFT_JOIN)
            createAlias("s.court", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("c.facility", "f", CriteriaSpecification.LEFT_JOIN)

            eq("f.id", facility.id)
            eq("c.id", cmd.courtId)
            eq("s.startTime", new LocalDateTime(cmd.startTime).toDate())
        }

        if(!booking) {
            render status: HttpStatus.SC_NOT_FOUND, text: "No booking found"
            return false
        } else if (!ids.contains(booking.customerId)) {
            render status: HttpStatus.SC_FORBIDDEN, text: "Customer of booking is not approved for cancellations"
            return false
        }

        try {
            bookingService.cancelBooking(booking, "")
        } catch (Exception e) {
            log.error("Cancel booking failed ${cmd}", e)
            render status: HttpStatus.SC_INTERNAL_SERVER_ERROR, text: "Error"
            return false
        }

        render status: HttpStatus.SC_OK
    }
}

@ToString
@Validateable(nullable = true)
class ApiCancelBookingCommand {
    Long courtId
    String startTime

    static constraints = {
        courtId(nullable: false)
        startTime(nullable: false)
    }
}
