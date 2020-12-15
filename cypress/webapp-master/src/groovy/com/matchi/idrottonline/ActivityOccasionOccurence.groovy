package com.matchi.idrottonline

import com.matchi.DateUtil
import com.matchi.activities.ActivityOccasion
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

class ActivityOccasionOccurence {
    LocalDate date
    ActivityOccasion activityOccasion

    ActivityOccasionOccurence(LocalDate date, ActivityOccasion activityOccasion){
        this.activityOccasion = activityOccasion
        this.date = date
    }

    // The activity occasions in MATCHi have the same 'id' every week which means they are not globally unique.
    // This calculated unique identifier ensures that the same activity occasion will have a unique id that
    // can be identified if sent multiple times to IdrottOnline. Uses the date of the monday in the week
    // and the id of the activity occasion (Id is guaranteed to be unique within the week). The identifier will stay
    // the same even if the activity occasion is moved between days in the week.
    // Format: YYYY-MM-DD-ID
    String getUniqueIdentifier(){
        LocalDate mondayOfOccurenceWeek = date.withDayOfWeek(DateTimeConstants.MONDAY)
        return String.format("${mondayOfOccurenceWeek.toString(DateUtil.DEFAULT_DATE_FORMAT)}-${activityOccasion.id}")
    }

    // IdrottOnline will automatically adjust to local datetime if time zone is not
    // specified explicitly and therefore we convert all datetimes to UTC.
    DateTime getStartDateTime(){
        activityOccasion.activity.facility.getDateTimeAsUTC(date.toDateTime(activityOccasion.startTime))
    }

    // IdrottOnline will automatically adjust to local datetime if time zone is not
    // specified explicitly and therefore we convert all datetimes to UTC.
    DateTime getEndDateTime(){
        activityOccasion.activity.facility.getDateTimeAsUTC(date.toDateTime(activityOccasion.endTime))
    }

    @Override
    String toString() {
        String.format("${activityOccasion.startTime.toString(DateUtil.DEFAULT_TIME_FORMAT)}-${activityOccasion.endTime.toString(DateUtil.DEFAULT_TIME_FORMAT)} ${activityOccasion.court?.name}")
    }
}
