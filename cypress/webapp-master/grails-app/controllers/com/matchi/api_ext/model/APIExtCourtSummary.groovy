package com.matchi.api_ext.model

import org.joda.time.DateTime

class APIExtCourtSummary {
    APIExtCourt court
    DateTime startTime
    DateTime endTime
    DateTime nextReservation
    APIExtBooking booking

    APIExtCourtSummary(def row) {
        APIExtSport sport = new APIExtSport(row.sport_id, row.sport_name)

        this.court = new APIExtCourt(
                row.court_id,
                row.court_name,
                row.court_position,
                row.members_only,
                row.offline_only,
                row.indoor,
                row.court_surface,
                sport,
                APIExtCourt.parseCameras(row.cameras))

        this.startTime = new DateTime(row.start_time)
        this.endTime = new DateTime(row.end_time)

        this.booking = new APIExtBooking(
                row.booking_id,
                row.booking_comments,
                row.booking_type,
                row.show_booking_holder,
                row.booking_name,
                row.booking_customer_id,
                row.activity_name,
                row.players)
    }
}
