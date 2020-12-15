package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class AddressBinder extends FieldBinder {

    AddressBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    static List getInputs() {
        ["address1", "address2", "postal_code", "city"]
    }

    static List getOptionalInputs() {
        ["address2"]
    }
}