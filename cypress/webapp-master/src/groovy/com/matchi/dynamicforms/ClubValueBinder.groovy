package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * Created by victorlindhe on 2017-08-21.
 */
class ClubValueBinder extends FieldBinder {

    final static String CLUB_INPUT_STRING = 'club'

    ClubValueBinder(FormField field, Submission submission, GrailsParameterMap params,
        MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    void bind() {
        def value = params."$field.id"?.trim()

        if (value) {
            submission.addToValues(new SubmissionValue(value: value, label: field.label,
                    fieldId: field.id, fieldType: field.type, input: CLUB_INPUT_STRING))
        } else if (field.isRequired) {
            submission.errors.reject("submission.errors.required", [field.label] as String[], "")
        }
    }

}
