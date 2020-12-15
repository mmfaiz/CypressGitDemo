package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class CheckboxBinder extends FieldBinder {

    CheckboxBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    void bind() {
        def values = params.list(field.id.toString())
        if (values) {
            values.eachWithIndex { val, idx ->
                submission.addToValues(new SubmissionValue(value: val, valueIndex: idx,
                        label: field.label, fieldId: field.id, fieldType: field.type))
            }
        } else if (field.isRequired) {
            submission.errors.reject("submission.errors.required", [field.label] as String[], "")
        }
    }
}