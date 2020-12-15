package com.matchi.api_ext.model

import com.matchi.activities.ClassActivity

class APIExtActivity {
    Long id
    String headline
    String teaser
    Integer price
    String img
    List<APIExtActivityOccasion> occasions

    APIExtActivity(ClassActivity classActivity) {
        this.id = classActivity.id
        this.headline = classActivity.name
        this.teaser = classActivity.teaser
        this.price = classActivity.price
        this.img = classActivity.largeImage?.absoluteFileURL
        this.occasions = new ArrayList<>()

        classActivity.upcomingOnlineOccasions.each {activityOccasion ->
            this.occasions.add(new APIExtActivityOccasion(classActivity, activityOccasion))
        }
    }
}
