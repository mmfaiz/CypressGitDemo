package com.matchi.api.v2


import grails.converters.JSON

class BookingResourceController extends com.matchi.api.BookingResourceController {

    static namespace = "v2"

    def list() {
        int maxPerPage = params.int("max") ?: 0
        int offset = params.int("offset") ?: 0

        def bookings = bookingService.getUserBookings(getCurrentUser(), false, maxPerPage, offset)

        Map map = [:]
        map.put("bookings", bookings)
        map.put("totalCount", bookings.getTotalCount())
        render map as JSON
    }
}
