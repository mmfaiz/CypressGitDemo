package com.matchi.admin

import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormTemplate
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * @author Sergei Shushkevich
 */
class AdminFormTemplateController {

    def index() {
        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        [templates: FormTemplate.list(params), templatesCount: FormTemplate.count()]
    }

    def create() {
    }

    def save() {
        def formTemplate = new FormTemplate(params)
        if (formTemplate.save(flush: true)) {
            flash.message = message(code: "adminFormTemplate.save.success")
            redirect(action: "index")
        } else {
            render(view: "create", model: [formTemplate: formTemplate])
        }
    }

    def edit(Long id) {
        def formTemplate = FormTemplate.get(id)
        if (formTemplate) {
            return [formTemplate: formTemplate]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def update(Long id, Long version) {
        def formTemplate = FormTemplate.get(id)
        if (formTemplate) {
            if (version != null) {
                if (formTemplate.version > version) {
                    formTemplate.errors.rejectValue("version", "formTemplate.optimistic.locking.failure")
                    render(view: "edit", model: [formTemplate: formTemplate])
                    return
                }
            }

            formTemplate.facilities?.clear()
            if (formTemplate.templateFields) {
                ([] + formTemplate.templateFields).each {
                    formTemplate.removeFromTemplateFields(it)
                    it.delete()
                }
            }
            formTemplate.properties = params

            if (formTemplate.save(flush: true)) {
                flash.message = message(code: "adminFormTemplate.update.success")
                redirect(action: "index")
            } else {
                render(view: "edit", model: [formTemplate: formTemplate])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def delete(Long id) {
        def formTemplate = FormTemplate.get(id)
        if (formTemplate) {
            try {
                FormTemplate.withTransaction {
                    Form.findAllByRelatedFormTemplate(formTemplate).each { form ->
                        form.relatedFormTemplate = null
                        form.save()
                    }
                    formTemplate.delete(flush: true)
                }
                flash.message = message(code: "adminFormTemplate.delete.success")
            } catch (DataIntegrityViolationException e) {
                log.error(e.message, e)
                flash.error = message(code: "adminFormTemplate.delete.failure")
            }
            redirect(action: "index")
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}
