package com.matchi.admin

import com.matchi.GlobalNotification
import org.springframework.dao.DataIntegrityViolationException

/**
 * @author Sergei Shushkevich
 */
class AdminGlobalNotificationController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "publishDate"
            params.order = "desc"
        }

        [globalNotificationInstanceList: GlobalNotification.list(params), globalNotificationInstanceTotal: GlobalNotification.count()]
    }

    def create() {
        [globalNotificationInstance: new GlobalNotification(params)]
    }

    def save() {
        def globalNotificationInstance = new GlobalNotification(params)
        if (globalNotificationInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'globalNotification.label'), globalNotificationInstance.id])
            redirect(action: "index")
        } else {
            render(view: "create", model: [globalNotificationInstance: globalNotificationInstance])
        }
    }

    def edit() {
        def globalNotificationInstance = GlobalNotification.get(params.id)
        if (globalNotificationInstance) {
            [globalNotificationInstance: globalNotificationInstance]
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'globalNotification.label'), params.id])
            redirect(action: "index")
        }
    }

    def update(Long id, Long version) {
        def globalNotificationInstance = GlobalNotification.get(id)
        if (!globalNotificationInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'globalNotification.label'), id])
            redirect(action: "index")
            return
        }

        if (version != null) {
            if (globalNotificationInstance.version > version) {
                globalNotificationInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'globalNotification.label')] as Object[],
                        "Another user has updated this GlobalNotification while you were editing")
                render(view: "edit", model: [globalNotificationInstance: globalNotificationInstance])
                return
            }
        }

        globalNotificationInstance.notificationText?.translations?.clear()
        globalNotificationInstance.properties = params

        GlobalNotification.withTransaction { status ->
            if (globalNotificationInstance.save(flush: true)) {
                flash.message = message(code: 'default.updated.message',
                        args: [message(code: 'globalNotification.label'), globalNotificationInstance.id])
                redirect(action: "index")
            } else {
                status.setRollbackOnly()
                render(view: "edit", model: [globalNotificationInstance: globalNotificationInstance])
            }
        }
    }

    def delete() {
        def globalNotificationInstance = GlobalNotification.get(params.id)
        if (!globalNotificationInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'globalNotification.label'), params.id])
            redirect(action: "index")
            return
        }

        try {
            globalNotificationInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'globalNotification.label'), params.id])
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'globalNotification.label'), params.id])
        }

        redirect(action: "index")
    }
}
