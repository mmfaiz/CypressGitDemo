package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class TextCheckboxBinder extends FieldBinder {

    TextCheckboxBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    static List getInputs() {
        [CHECKMARK_INPUT, "value"]
    }
}