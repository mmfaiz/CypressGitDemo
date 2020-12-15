package com.matchi.api_ext.model

import com.matchi.Booking
import com.matchi.Court
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import org.joda.time.DateTime

class APIExtActivityOccasion {
    DateTime start
    Long lengthInMinutes
    Integer maxParticipants
    Integer registeredParticipants
    List<APIExtSlot> slots
    List<APIExtParticipant> participants

    APIExtActivityOccasion(ClassActivity classActivity, ActivityOccasion activityOccasion) {
        this.start = activityOccasion.date.toDateTime(activityOccasion.startTime)
        this.lengthInMinutes = activityOccasion.lengthInMinutes()
        this.maxParticipants = activityOccasion.maxNumParticipants
        this.registeredParticipants = activityOccasion.numParticipations()
        this.slots = new ArrayList<>()
        this.participants = new ArrayList<>()

        activityOccasion.bookings.each {
            this.slots.add(slot(classActivity, it))
        }

        activityOccasion.participations.each {
            this.participants.add(new APIExtParticipant(it.customer))
        }
    }

    private APIExtSlot slot(ClassActivity classActivity, Booking booking) {
        Court court = booking.slot?.court

        List<APIExtCourtCamera> cameras = new ArrayList<>()
        court?.cameras?.each {cameras.add(new APIExtCourtCamera(it))}

        APIExtParticipant apiExtParticipant = new APIExtParticipant(booking.customer)

        APIExtBooking apiExtBooking = new APIExtBooking(
                booking.id,
                booking.comments,
                booking.group?.type?.toString(),
                booking.slot.court.facility.showBookingHolder,
                apiExtParticipant.name,
                booking.customer.id,
                classActivity.name,
                null)

        APIExtCourt apiExtCourt = new APIExtCourt(
                court.id,
                court.name,
                court.listPosition,
                court.membersOnly,
                court.offlineOnly,
                court.indoor,
                court.surface?.name(),
                new APIExtSport(court.sport?.id, court.sport?.name),
                cameras)

        return new APIExtSlot(
                booking.slot.id,
                new DateTime(booking.slot.startTime),
                new DateTime(booking.slot.endTime),
                apiExtCourt,
                apiExtBooking)
    }
}
