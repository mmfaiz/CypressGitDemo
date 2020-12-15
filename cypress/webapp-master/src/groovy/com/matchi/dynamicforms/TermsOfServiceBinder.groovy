package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class TermsOfServiceBinder extends FieldBinder {

    TermsOfServiceBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    void bind() {
        if (!params.boolean("$field.id")) {
            submission.errors.reject("submission.errors.termsOfService")
        }
    }
}