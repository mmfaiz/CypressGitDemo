package com.matchi.marshallers

import com.matchi.ActivityService
import com.matchi.activities.ActivityOccasion
import grails.converters.JSON

import javax.annotation.PostConstruct

class ActivityOccasionMarshaller {

    static ActivityService activityService

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(ActivityOccasion, { ActivityOccasion occasion ->
            marshallActivityOccasion(occasion)
        })
    }

    def marshallActivityOccasion(ActivityOccasion occasion) {

        def result = [
                id                           : occasion.id,
                activityId                   : occasion.activityId ?: occasion.activity.id,
                message                      : occasion.message,
                basePrice                    : occasion.price,
                currency                     : occasion.activity.facility.currency,
                courtId                      : occasion.courtId,
                startTime                    : occasion.startDateTime,
                endTime                      : occasion.endDateTime,
                participants                 : occasion.participations.size(),
                maxNumParticipants           : occasion.maxNumParticipants,
                minNumParticipants           : occasion.minNumParticipants,
                membersOnly                  : occasion.membersOnly,
                automaticCancellationDateTime: occasion.automaticCancellationDateTime,
                isBookable                   : activityService.isActivityOccasionBookableForUser(occasion),
                isParticipating              : activityService.isUserParticipating(occasion),
                canBeCancelledByUser         : occasion.activity.cancelByUser,
                hasNotClosedRegistration     : occasion.hasNotClosedRegistration(),
                hasOpenedRegistration        : occasion.hasOpenedRegistration(),
                refundPolicy                 : [
                        code: "cancellation.up.to.hours.before",
                        args: [occasion.activity.cancelLimitWithFallback]
                ],
                refundableServiceFee         : activityService.getServiceFeeWithCurrency(occasion),
        ]

        return result
    }
}
