package com.matchi.events

import com.matchi.*
import com.matchi.activities.*
import com.matchi.watch.*
import grails.test.mixin.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(ActivityOccasion)
@Mock([ClassActivity, ClassActivityWatch, Facility, Municipality, Region, User])
class ActivityOccasionTests {

    ActivityOccasion occasion

    @Before
    public void setUp() {
        occasion = new ActivityOccasion()
    }

    void testIsNotInPastWhenOngoing() {
        def now = new DateTime()

        occasion.date = now.toLocalDate()
        occasion.startTime = now.toLocalTime().withMinuteOfHour(0)
        occasion.endTime   = now.toLocalTime().plusHours(1).withMinuteOfHour(0)

        assert !occasion.isPast()
    }

    void testIsNotInPastWhenInFuture() {
        def now = new DateTime().plusDays(1)

        occasion.date = now.toLocalDate()
        occasion.startTime = now.toLocalTime().plusHours(2).withMinuteOfHour(0)
        occasion.endTime   = now.toLocalTime().plusHours(3).withMinuteOfHour(0)

        assert !occasion.isPast()
    }

    void testIsInPast() {
        def dt = new DateTime().minusDays(1)

        occasion.date = dt.toLocalDate()
        occasion.startTime = dt.toLocalTime().minusHours(5).withMinuteOfHour(0)
        occasion.endTime   = dt.toLocalTime().minusHours(4).withMinuteOfHour(0)

        assert occasion.isPast()
    }

    void testLengthInMinutesOnHour() {
        occasion.date      = new DateTime().toLocalDate()
        occasion.startTime = new LocalTime("10:00")
        occasion.endTime   = new LocalTime("11:00")

        assert 60 == occasion.lengthInMinutes()
    }

    void testLengthInMinutesMultipleHoursAndMinutes() {
        occasion.date      = new DateTime().toLocalDate()
        occasion.startTime = new LocalTime("10:00")
        occasion.endTime   = new LocalTime("15:34")

        assert 334 == occasion.lengthInMinutes()
    }

    void testLengthInMinutesOneMinute() {
        occasion.date      = new DateTime().toLocalDate()
        occasion.startTime = new LocalTime("10:00")
        occasion.endTime   = new LocalTime("10:01")

        assert 1 == occasion.lengthInMinutes()
    }

    void testGetShortDescription() {
        assert new ActivityOccasion(date: new LocalDate(2019, 1, 15),
                startTime: new LocalTime(11, 55), activity: new EventActivity(name: "test1"))
                .getShortDescription() == "15/01 11:55, test1"
        assert new ActivityOccasion(date: new LocalDate(2019, 12, 25),
                startTime: new LocalTime(18, 0), activity: new EventActivity(name: "test2"))
                .getShortDescription() == "25/12 18:00, test2"
    }

    void testIsAutomaticCancellableNoMinParticipants() {
        occasion.minNumParticipants = null
        assert !occasion.hasToFewParticipations()

        occasion.participations = null
        assert !occasion.hasToFewParticipations()

        occasion.participations = []
        assert !occasion.hasToFewParticipations()

        occasion.participations = [new Participation(), new Participation()].toSet()
        assert !occasion.hasToFewParticipations()
        assert occasion.participations.size() == 2

        occasion.minNumParticipants = 0
        assert !occasion.hasToFewParticipations()
    }

    void testIsAutomaticCancellableWithMinParticipants() {
        occasion.minNumParticipants = 2
        assert occasion.hasToFewParticipations()

        occasion.participations = null
        assert occasion.hasToFewParticipations()

        occasion.participations = []
        assert occasion.hasToFewParticipations()

        occasion.participations = [new Participation(), new Participation()].toSet()
        assert !occasion.hasToFewParticipations()

        occasion.participations = [new Participation()].toSet()
        assert occasion.hasToFewParticipations()
    }

    void testSetGetCancellationDate() {
        occasion.automaticCancellationDateTime = null
        assert occasion.getCancelHoursInAdvance() == null

        occasion.date = new LocalDate()
        occasion.startTime = new LocalTime()
        occasion.setCancellationDateTime(0)
        assert occasion.automaticCancellationDateTime == occasion.date.toDateTime(occasion.startTime)
        assert occasion.cancelHoursInAdvance == 0

        shouldFail(IllegalArgumentException) {
            occasion.setCancellationDateTime(-1)
        }

        occasion.date = new LocalDate()
        occasion.startTime = new LocalTime()
        occasion.setCancellationDateTime(24)
        assert occasion.automaticCancellationDateTime == occasion.date.minusDays(1).toDateTime(occasion.startTime)
        assert occasion.automaticCancellationDateTime == occasion.date.toDateTime(occasion.startTime).minusHours(24)
        assert occasion.cancelHoursInAdvance == 24
    }

    void testGetWatchQueue() {
        def user = createUser()
        def facility = createFacility()
        def activity = new ClassActivity(name: "a", facility: facility).save(failOnError: true)
        def occasion1 = new ActivityOccasion(activity: activity, date: LocalDate.now(),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)
        def watch1 = new ClassActivityWatch(user: user, facility: facility, classActivity: activity,
                fromDate: occasion1.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion1.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)
        def occasion2 = new ActivityOccasion(activity: activity, date: LocalDate.now().plusDays(1),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)
        def watch2 = new ClassActivityWatch(user: user, facility: facility, classActivity: activity,
                fromDate: occasion2.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion2.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)

        def watchQueue = occasion1.getWatchQueue()

        assert watchQueue
        assert watchQueue.size() == 1
        assert watchQueue[0] == watch1

        watchQueue = occasion2.getWatchQueue()

        assert watchQueue
        assert watchQueue.size() == 1
        assert watchQueue[0] == watch2
    }

    void testGetWatchQueueSize() {
        def user = createUser()
        def user2 = createUser("user2@matchi.se")
        def facility = createFacility()
        def activity = new ClassActivity(name: "a", facility: facility).save(failOnError: true)
        def occasion1 = new ActivityOccasion(activity: activity, date: LocalDate.now(),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)
        new ClassActivityWatch(user: user, facility: facility, classActivity: activity,
                fromDate: occasion1.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion1.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)
        def occasion2 = new ActivityOccasion(activity: activity, date: LocalDate.now().plusDays(1),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)
        new ClassActivityWatch(user: user, facility: facility, classActivity: activity,
                fromDate: occasion2.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion2.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)
        new ClassActivityWatch(user: user2, facility: facility, classActivity: activity,
                fromDate: occasion2.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion2.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)

        assert occasion1.getWatchQueueSize() == 1
        assert occasion2.getWatchQueueSize() == 2
    }
}
