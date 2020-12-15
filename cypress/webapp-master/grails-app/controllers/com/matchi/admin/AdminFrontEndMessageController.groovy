package com.matchi.admin

import com.matchi.FrontEndMessage
import com.matchi.MFile
import com.matchi.activities.ClassActivity
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

/**
 * @author Sergei Shushkevich
 */
class AdminFrontEndMessageController {

    def fileArchiveService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "publishDate"
            params.order = "desc"
        }

        [frontEndMessageList: FrontEndMessage.list(params), frontEndMessageInstanceTotal: FrontEndMessage.count()]
    }

    def create() {
        [frontEndMessageInstance: new FrontEndMessage(params)]
    }

    def save() {
        FrontEndMessage frontEndMessageInstance = new FrontEndMessage(params)
        if (frontEndMessageInstance .save(flush: true)) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'frontEndMessage.label'), frontEndMessageInstance.id])
            redirect(action: "index")
        } else {
            render(view: "create", model: [frontEndMessageInstance: frontEndMessageInstance])
        }
    }

    def edit() {
        FrontEndMessage frontEndMessageInstance = FrontEndMessage.get(params.id)

        if (frontEndMessageInstance) {
            [frontEndMessageInstance: frontEndMessageInstance]
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'frontEndMessage.label'), params.id])
            redirect(action: "index")
        }
    }

    def update(Long id, Long version) {
        FrontEndMessage frontEndMessageInstance = FrontEndMessage.get(params.id)
        if (!frontEndMessageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'frontEndMessage.label'), id])
            redirect(action: "index")
            return
        }

        if (version != null) {
            if (frontEndMessageInstance.version > version) {
                frontEndMessageInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'frontEndMessage.label')] as Object[],
                        "Another user has updated this Front End Message while you were editing")
                render(view: "edit", model: [frontEndMessageInstance: frontEndMessageInstance])
                return
            }
        }

        frontEndMessageInstance.properties = params

        FrontEndMessage.withTransaction { status ->
            if (frontEndMessageInstance.save(flush: true)) {
                flash.message = message(code: 'default.updated.message',
                        args: [message(code: 'frontEndMessage.label'), frontEndMessageInstance.id])
                render(view: "edit", model: [frontEndMessageInstance: frontEndMessageInstance])
            } else {
                status.setRollbackOnly()
                render(view: "edit", model: [frontEndMessageInstance: frontEndMessageInstance])
            }
        }
    }

    def delete() {
        FrontEndMessage frontEndMessageInstance = FrontEndMessage.get(params.id)
        if (!frontEndMessageInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'frontEndMessage.label'), params.id])
            redirect(action: "index")
            return
        }

        try {
            frontEndMessageInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'frontEndMessage.label'), params.id])
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'frontEndMessage.label'), params.id])
        }

        redirect(action: "index")
    }

    def uploadImage() {
        FrontEndMessage frontEndMessageInstance = FrontEndMessage.get(params.id)

        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile("file")

        image.originalFilename

        if (!frontEndMessageInstance.images.any { it.originalFileName == image.originalFilename} && image && image.size > 0) {
            def mfile = fileArchiveService.storeFile(image)

            frontEndMessageInstance.images.add(mfile)
            frontEndMessageInstance.save()

            flash.message = message(code: "facilityActivity.upload.success")
        } else {
            flash.error = message(code: "facilityActivity.upload.error")
        }

        redirect(action: "edit", id: frontEndMessageInstance.id)
    }

    def removeImage() {
        FrontEndMessage frontEndMessageInstance = FrontEndMessage.get(params.id)
        MFile image = frontEndMessageInstance.images.find {
            return it.id == params.long("imageId")
        }

        flash.message = message(code: "facilityActivity.removeImage.success")

        if(image) {
            frontEndMessageInstance.images.remove(image)
            fileArchiveService.removeFile(image)
            frontEndMessageInstance.save()
        }

        redirect(action: "edit", id: frontEndMessageInstance.id)
    }
}
