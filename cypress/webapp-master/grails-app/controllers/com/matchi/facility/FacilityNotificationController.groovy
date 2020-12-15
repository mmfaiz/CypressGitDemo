package com.matchi.facility

import com.matchi.GenericController
import com.matchi.messages.FacilityMessage
import com.matchi.messages.FacilityMessage.Channel
import grails.transaction.Transactional
import org.joda.time.LocalDate
import org.springframework.dao.DataIntegrityViolationException
import javax.servlet.http.HttpServletResponse

/**
 * @author Sergei Shushkevich
 */
class FacilityNotificationController extends GenericController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        Date now = LocalDate.now().toDate()
        def result = FacilityMessage.createCriteria().list(params) {
            eq("facility", facility)
            eq("channel", Channel.NOTIFICATION)
            ge("validTo", now)
            order("listPosition", "asc")
        }

        [facilityNotificationInstanceList: result,
                facilityNotificationInstanceTotal: result.totalCount, facility: facility]
    }

    def archived() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        Date now = LocalDate.now().toDate()
        def result = FacilityMessage.createCriteria().list(params) {
            eq("facility", getUserFacility())
            eq("channel", Channel.NOTIFICATION)
            lt("validTo", now)
            order("listPosition", "asc")
        }

        render(view: "index", model:
                [facilityNotificationInstanceList: result,
                 facilityNotificationInstanceTotal: result.totalCount, facility: facility])
    }

    def create() {
        def facility = getUserFacility()
        [facilityNotificationInstance: new FacilityMessage(params), facility: facility]
    }

    def save() {
        def facilityNotificationInstance = new FacilityMessage(params)
        facilityNotificationInstance.facility = getUserFacility()
        facilityNotificationInstance.channel = Channel.NOTIFICATION
        facilityNotificationInstance.html = true
        facilityNotificationInstance.active = true

        if (facilityNotificationInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message',
                    args: [message(code: 'facilityNotification.label'), facilityNotificationInstance.id])
            redirect(action: "index")
        } else {
            render(view: "create", model: [facilityNotificationInstance: facilityNotificationInstance])
        }
    }

    def edit() {
        def facilityNotificationInstance = FacilityMessage.findByIdAndFacilityAndChannel(
                params.id, getUserFacility(), Channel.NOTIFICATION)
        if (facilityNotificationInstance) {
            [facilityNotificationInstance: facilityNotificationInstance]
        } else {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'facilityNotification.label'), params.id])
            redirect(action: "index")
        }
    }

    def update(Long id, Long version) {
        def facilityNotificationInstance = FacilityMessage.findByIdAndFacilityAndChannel(
                id, getUserFacility(), Channel.NOTIFICATION)
        if (!facilityNotificationInstance) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'facilityNotification.label'), id])
            redirect(action: "index")
            return
        }

        if (version != null) {
            if (facilityNotificationInstance.version > version) {
                facilityNotificationInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'facilityNotification.label')] as Object[],
                        "Another user has updated this Notification while you were editing")
                render(view: "edit", model: [facilityNotificationInstance: facilityNotificationInstance])
                return
            }
        }

        facilityNotificationInstance.properties = params

        if (!facilityNotificationInstance.save(flush: true)) {
            render(view: "edit", model: [facilityNotificationInstance: facilityNotificationInstance])
            return
        }

        flash.message = message(code: 'default.updated.message',
                args: [message(code: 'facilityNotification.label'), facilityNotificationInstance.id])
        redirect(action: "index")
    }

    def delete() {
        def facilityNotificationInstance = FacilityMessage.findByIdAndFacilityAndChannel(
                params.id, getUserFacility(), Channel.NOTIFICATION)
        if (!facilityNotificationInstance) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'facilityNotification.label'), params.id])
            redirect(action: "index")
            return
        }

        try {
            facilityNotificationInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message',
                    args: [message(code: 'facilityNotification.label'), params.id])
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message',
                    args: [message(code: 'facilityNotification.label'), params.id])
        }

        redirect(action: "index")
    }

    @Transactional
    def swapListPosition(Long id1, Long id2) {
        FacilityMessage message1 = FacilityMessage.get(id1)
        FacilityMessage message2 = FacilityMessage.get(id2)
        if (message1 && message2) {
            Integer pos1 = message1.listPosition
            message1.listPosition = message2.listPosition
            message1.save()
            message2.listPosition = pos1
            message2.save()
            render ""
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

}
