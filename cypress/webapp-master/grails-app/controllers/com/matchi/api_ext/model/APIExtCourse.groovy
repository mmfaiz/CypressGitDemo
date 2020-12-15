package com.matchi.api_ext.model

import com.matchi.activities.trainingplanner.CourseActivity
import org.joda.time.LocalDate

class APIExtCourse {
    Long id
    String name
    String description
    LocalDate startDate
    LocalDate endDate
    List<APIExtCourseOccasion> occasions

    APIExtCourse(CourseActivity courseActivity) {
        this.id = courseActivity.id
        this.name = courseActivity.name
        this.description = courseActivity.description
        this.startDate = new LocalDate(courseActivity.startDate)
        this.endDate = new LocalDate(courseActivity.endDate)
        this.occasions = new ArrayList<>()
    }
}
