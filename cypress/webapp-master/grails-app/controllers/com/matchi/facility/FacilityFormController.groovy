package com.matchi.facility

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.activities.Participant
import com.matchi.dynamicforms.FieldBinder
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField.Type
import com.matchi.dynamicforms.FormTemplate
import com.matchi.dynamicforms.Submission
import com.matchi.dynamicforms.SubmissionValue
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * @author Sergei Shushkevich
 */
class FacilityFormController extends GenericController {

    def index() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        [forms: Form.findAllByFacility(facility, params), formsCount: Form.countByFacility(facility)]
    }

    def create() {
        Facility facility = getUserFacility()
        return [formTemplates: FormTemplate.shared(facility).listDistinct(),
         facilityHasRequirementProfiles: facility.hasRequirementProfiles()]
    }

    def save() {
        def formInstance = new Form(params)
        formInstance.facility = getUserFacility()

        if (formInstance.save(flush: true)) {
            flash.message = message(code: "facilityForm.save.success")
            redirect(action: "index")
        } else {
            render(view: "create", model: [formInstance: formInstance,
                    formTemplates: FormTemplate.shared(getUserFacility()).listDistinct()])
        }
    }

    def edit(Long id) {
        Facility facility = getUserFacility()
        def formInstance = Form.findByIdAndFacility(id, facility)
        if (formInstance) {
            return [formInstance: formInstance, facilityHasRequirementProfiles: facility.hasRequirementProfiles()]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def update(Long id, Long version) {
        def formInstance = Form.findByIdAndFacility(id, getUserFacility())
        if (formInstance) {
            if (version != null) {
                if (formInstance.version > version) {
                    formInstance.errors.rejectValue("version", "form.optimistic.locking.failure")
                    render(view: "edit", model: [formInstance: formInstance])
                    return
                }
            }

            if (formInstance.fields) {
                ([] + formInstance.fields).each {
                    formInstance.removeFromFields(it)
                    it.delete()
                }
            }
            formInstance.properties = params

            if (formInstance.save(flush: true)) {
                flash.message = message(code: "facilityForm.update.success")
                redirect(action: "index")
            } else {
                render(view: "edit", model: [formInstance: formInstance])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def delete(Long id) {
        def formInstance = Form.findByIdAndFacility(id, getUserFacility())
        if (formInstance) {
            try {
                formInstance.delete(flush: true)
                flash.message = message(code: "facilityForm.delete.success")
            } catch (DataIntegrityViolationException e) {
                log.error(e.message, e)
                flash.error = message(code: "facilityForm.delete.failure")
            }
            redirect(action: "index")
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def loadTemplate(Long id) {
        def formTemplate = FormTemplate.shared(getUserFacility()).get(id)
        if (formTemplate) {
            render(template: "formTemplate", model: [formTemplate: formTemplate, facilityEditing: true])
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def submissions(Long id) {
        def formInstance = Form.findByIdAndFacility(id, getUserFacility())
        if (formInstance) {
            params.max = Math.min(params.max ? params.int('max') : 50, 100)
            if (!params.sort) {
                params.sort = "dateCreated"
                params.order = "desc"
            }

            return [formInstance: formInstance, submissions: Submission.findAllByForm(formInstance, params),
                    submissionsCount: Submission.countByForm(formInstance)]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def showSubmission(Long id) {
        def submission = Submission.byFacility(getUserFacility()).get(id)
        if (submission) {
            def values = SubmissionValue.findAllBySubmission(submission).groupBy {
                it.label
            }.sort {
                it.value[0].fieldId
            }
            return [submission: submission, submissionValues: values]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def editSubmission(Long id) {
        def submission = Submission.byFacility(getUserFacility()).get(id)
        if (submission) {
            def redirectParams = [customerId: submission.customer.id, hash: submission.form.hash,
                    returnUrl: createLink(controller: "facilityCustomer", action: "show", id: submission.customer.id)]

            SubmissionValue.findAllBySubmission(submission).each {
                if (it.fieldType == Type.CHECKBOX.name()) {
                    if (!redirectParams."${it.fieldId}") {
                        redirectParams."${it.fieldId}" = []
                    }
                    redirectParams."${it.fieldId}" << it.value
                } else if (it.fieldType in [Type.TEXT_CHECKBOX, Type.TEXT_CHECKBOX_PICKUP,
                        Type.TEXT_CHECKBOX_ALLERGIES, Type.PLAYER_STRENGTH]*.name()) {
                    redirectParams."${it.fieldId}.${FieldBinder.CHECKMARK_INPUT}" = true
                    redirectParams."${it.fieldId}.${it.input}" = it.value
                } else if (it.fieldType == Type.TIMERANGE_CHECKBOX.name()) {
                    redirectParams."${it.fieldId}.${it.valueIndex}.${FieldBinder.CHECKMARK_INPUT}" = true
                    redirectParams."${it.fieldId}.${it.valueIndex}.${it.input}" = it.value
                } else if (it.input) {
                    redirectParams."${it.fieldId}.${it.input}" = it.value
                } else {
                    redirectParams."${it.fieldId}" = it.value
                }
            }

            redirect(controller: "form", action: "show", params: redirectParams)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}
