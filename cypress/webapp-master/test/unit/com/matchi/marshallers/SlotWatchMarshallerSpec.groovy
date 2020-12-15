package com.matchi.marshallers

import com.matchi.*
import com.matchi.watch.SlotWatch
import grails.test.mixin.Mock
import org.joda.time.DateTime
import org.springframework.context.MessageSource
import spock.lang.Specification

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@Mock([Court, Facility, Municipality, Region, SlotWatch, Sport, User])
class SlotWatchMarshallerSpec extends Specification {

    def marshaller
    def messageSource = Mock(MessageSource)

    def setup() {
        marshaller = new SlotWatchMarshaller()
        marshaller.messageSource = messageSource
    }

    void testMarshallSlotWatch() {
        def user = createUser()
        def facility = createFacility()
        def sport = createSport()
        def court = createCourt(facility, sport)
        def from = DateTime.now().toDate()
        def to = DateTime.now().plusHours(1).toDate()
        def sw1 = new SlotWatch(user: user, facility: facility, court: court, sport: sport,
                fromDate: from, toDate: to).save(failOnError: true)
        def sw2 = new SlotWatch(user: user, facility: facility,
                fromDate: from, toDate: to).save(failOnError: true)

        when:
        def result1 = marshaller.marshallSlotWatch(sw1)

        then:
        1 * messageSource.getMessage(_, _, _, _) >> "name from messageSource"
        result1.id == sw1.id
        result1.from == from.format("HH:mm")
        result1.to == to.format("HH:mm")
        result1.facility == facility
        result1.court == court
        result1.sport.id == sport.id
        result1.sport.name == "name from messageSource"

        when:
        def result2 = marshaller.marshallSlotWatch(sw2)

        then:
        result2.id == sw2.id
        result2.from == from.format("HH:mm")
        result2.to == to.format("HH:mm")
        result2.facility == facility
        !result2.court
        !result2.sport
    }
}