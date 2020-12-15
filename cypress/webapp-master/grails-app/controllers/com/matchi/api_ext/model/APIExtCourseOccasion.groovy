package com.matchi.api_ext.model

import com.matchi.Court
import com.matchi.activities.ActivityOccasion
import org.joda.time.DateTime

class APIExtCourseOccasion {
    DateTime start
    DateTime end
    APIExtCourt court
    List<String> trainers
    List<String> participants

    APIExtCourseOccasion(ActivityOccasion activityOccasion) {
        this.trainers = new ArrayList<>()
        this.participants = new ArrayList<>()

        this.start = activityOccasion.date.toDateTime(activityOccasion.startTime)
        this.end = activityOccasion.date.toDateTime(activityOccasion.endTime)

        Court c = activityOccasion.court?.court

        List<APIExtCourtCamera> cameras = new ArrayList<>()
        c.cameras?.each {cameras.add(new APIExtCourtCamera(it))}

        this.court = new APIExtCourt(
                c.id,
                c.name,
                c.listPosition,
                c.membersOnly,
                c.offlineOnly,
                c.indoor,
                c.surface?.name(),
                new APIExtSport(c.sport?.id, c.sport?.name),
                cameras)

        activityOccasion.trainers?.each {trainer ->
            this.trainers.add(trainer.firstName + " " + trainer.lastName)
        }

        activityOccasion.participants?.each {participant ->
            this.participants.add(participant.customer?.firstname + " " + participant.customer?.lastname)
        }
    }
}
