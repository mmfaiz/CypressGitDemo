package com.matchi.protocol

import com.matchi.Facility
import grails.validation.Validateable

class ApiBookingsController extends AbstractApiController  {

    def apiBookingsService

    def index() { }

    def bookings(ApiBookingsCommand cmd) {
        ApiRequestedBookings requestedBookings = null

        ApiBookingsRequest bookingsRequest = createBookingsRequest(cmd, request)
        try {
            log.debug("" + bookingsRequest.toString())

            requestedBookings = apiBookingsService.processBookingsRequest(bookingsRequest)
            requestedBookings.success = true

        } catch(ApiBookingsRequestException be) {
            log.error(be)
            requestedBookings = new ApiRequestedBookings()
            requestedBookings.addError(be.errorCode, "Could not load bookings!")
            requestedBookings.success = false
        }

        renderResponse(requestedBookings)
    }

    private ApiBookingsRequest createBookingsRequest(def cmd, def request) {
        ApiBookingsRequest bookingsRequest = new ApiBookingsRequest()

        try {
            def facility = Facility.findByApikey(cmd.token)

            if(facility)
                bookingsRequest.facilityId = facility.id
            else
                bookingsRequest.facilityId = null

            bookingsRequest.startDate = cmd.startDate
            bookingsRequest.endDate = cmd.endDate

            bookingsRequest.start = cmd.start

            if(cmd.max != 0) {
                bookingsRequest.max = cmd.max
            }

        } catch(NumberFormatException n) {
            log.debug("Unable to find facility for api request.")
        }

        bookingsRequest.remoteIP = request.getRemoteAddr()

        return bookingsRequest
    }

    @Override
    protected AbstractRequest.RequestType getRequestType() {
        return AbstractRequest.RequestType.BOOKINGS
    }
}

@Validateable(nullable = true)
class ApiBookingsCommand {
    String token
    String startDate
    String endDate

    int start
    int max

    static constraints = {

    }
}
