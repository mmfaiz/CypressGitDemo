package com.matchi.marshallers

import com.matchi.ActivitiesHelper
import com.matchi.activities.ClassActivity
import grails.converters.JSON

import javax.annotation.PostConstruct

class ClassActivityMarshaller {
    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(ClassActivity) { ClassActivity activity ->
            marshallClassActivity(activity)
        }
    }

    def marshallClassActivity(ClassActivity activity) {
        def result = [
                id            : activity.id,
                facilityId    : activity.facilityId,
                name          : activity.name,
                teaser        : activity.teaser,
                imageUrl      : activity.largeImage?.optimizedImageFullURL,
                messageTitle  : activity.userMessageLabel,
                level         : [min: activity.levelMin, max: activity.levelMax],
                terms         : activity.terms,
                numOfOccasions: activity.occasions.size()
        ]

        if (activity.JSONoptions?.exposeOccasions) {
            if (activity.JSONoptions?.predefinedActivityOccasions != null) {
                result.occasions = ActivitiesHelper.filterOccasionsCollectionByCommand(activity.JSONoptions?.predefinedActivityOccasions, activity.JSONoptions).sort { it.startDateTime }
            } else {

                result.occasions = ActivitiesHelper.filterOccasionsCollectionByCommand(activity.upcomingOnlineOccasions, activity.JSONoptions).sort {
                    it.startDateTime
                }
            }
        }

        return result
    }
}
