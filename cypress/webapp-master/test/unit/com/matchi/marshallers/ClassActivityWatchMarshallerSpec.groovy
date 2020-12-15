package com.matchi.marshallers

import com.matchi.Facility
import com.matchi.activities.ClassActivity
import com.matchi.watch.ClassActivityWatch
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * @author Sergei Shushkevich
 */
class ClassActivityWatchMarshallerSpec extends Specification {

    void testMarshallClassActivityWatch() {
        def facility = new Facility(name: "test facility")
        def activity = new ClassActivity(name: "test activity")
        activity.id = 100L
        def watch = new ClassActivityWatch(facility: facility, classActivity: activity,
                fromDate: new DateTime(2019, 9, 25, 14, 30).toDate(),
                toDate: new DateTime(2019, 9, 25, 15, 30).toDate())
        watch.id = 200L

        when:
        def result = new ClassActivityWatchMarshaller().marshallClassActivityWatch(watch)

        then:
        result.id == 200L
        result.activity.id == 100L
        result.activity.name == "test activity"
        result.from == "2019-09-25 14:30"
        result.to == "2019-09-25 15:30"
        result.facility == facility
    }
}