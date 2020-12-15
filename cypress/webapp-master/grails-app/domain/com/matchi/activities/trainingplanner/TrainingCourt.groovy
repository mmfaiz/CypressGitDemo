package com.matchi.activities.trainingplanner

import com.matchi.Court
import com.matchi.Facility

/**
 * @author Michael Astreiko
 */
class TrainingCourt {
    static belongsTo = [facility: Facility, court: Court]
    String name

    static constraints = {
        court nullable: true
    }
}
