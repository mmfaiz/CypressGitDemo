package com.matchi.facility

import com.matchi.GenericController
import com.matchi.messages.FacilityMessage
import grails.validation.Validateable
import org.apache.commons.collections.ListUtils
import org.apache.commons.collections.Factory
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class FacilityMessageController extends GenericController {

    def index() {
        def messages = FacilityMessage.facilityMessages(getUserFacility()).list(params)
        [messages: messages]
    }



    def form() {

        def cmd = new FacilityMessageCommand(active: Boolean.TRUE)

        if(params.id) {
            def message = FacilityMessage.get(params.id)
            if(message) {
                assertFacilityAccessTo(message)
                cmd.headline = message.headline
                cmd.content  = message.content
                cmd.active   = message.active
                cmd.validFromDate = message.validFrom?.toLocalDate()
                cmd.validFromTime = message.validFrom?.toLocalTime()
                cmd.validToDate = message.validTo?.toLocalDate()
                cmd.validToTime = message.validTo?.toLocalTime()
            }
        }


        [cmd: cmd]
    }

    def save(FacilityMessageCommand cmd) {

        def facilityMessage = new FacilityMessage()

        if(params.id) {
            facilityMessage = FacilityMessage.get(params.id)
        }

        facilityMessage.facility = getUserFacility()

        cmd.channels.each {
            facilityMessage.addToChannels(it)
        }

        facilityMessage.validFrom = cmd.validFromDate?.toDateTime(cmd.validFromTime)
        facilityMessage.validTo   = cmd.validToDate?.toDateTime(cmd.validToTime)

        facilityMessage.headline  = cmd.headline
        facilityMessage.content   = cmd.content
        facilityMessage.active    = cmd.active?.asBoolean()

        facilityMessage.save()

        flash.message = message(code: "facilityMessage.save.success")
        redirect(action: "index")
    }

    def delete() {

        if(params.id) {
            def facilityMessage = FacilityMessage.get(params.id)
            if(facilityMessage) {
                assertFacilityAccessTo(facilityMessage)
                facilityMessage.delete()
                flash.message = message(code: "facilityMessage.delete.success")
            }
        }

        redirect(action: "index")
    }
}

@Validateable(nullable = true)
class FacilityMessageCommand {
    List<String> channels = ListUtils.lazyList([], {new String()} as Factory)
    String headline
    String content
    LocalDate validFromDate
    LocalDate validToDate
    LocalTime validFromTime
    LocalTime validToTime
    Boolean active
}
