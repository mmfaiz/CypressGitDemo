package com.matchi

import com.matchi.async.ScheduledTask
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(Season)
@Mock([Facility, Region, Municipality, Season, ScheduledTask])
class SeasonTests extends Specification {

    void testNameIsRequired() {
        when:
        def season = createSeason()
        season.name = null

        then:
        !season.validate()
    }

    void testStartTimeIsRequired() {
        when:
        def season = createSeason()
        season.startTime = null

        then:
        !season.validate()
    }

    void testEndTimeIsRequired() {
        when:
        def season = createSeason()
        season.endTime = null

        then:
        !season.validate()
    }

    void testIsInitializing() {
        when:
        def facility = createFacility()
        def season1 = createSeason(facility)
        def season2 = createSeason(facility)
        new ScheduledTask(facility: facility, name: "task1",
                relatedDomainClass: Season.class.simpleName, domainIdentifier: season1.id,
                isTaskFinished: true).save(failOnError: true)
        new ScheduledTask(facility: facility, name: "task2",
                relatedDomainClass: Season.class.simpleName, domainIdentifier: season2.id,
                isTaskFinished: false).save(failOnError: true)

        then:
        !season1.isInitializing()
        season2.isInitializing()
    }
}
