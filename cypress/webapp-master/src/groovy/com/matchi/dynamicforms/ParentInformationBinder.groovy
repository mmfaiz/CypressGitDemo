package com.matchi.dynamicforms

import org.apache.commons.validator.EmailValidator
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class ParentInformationBinder extends FieldBinder {

    ParentInformationBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    static List getInputs() {
        ["firstname", "lastname", "email", "cellphone"]
    }

    protected void validateValue(String input, String value) {
        if (input == "email" && !EmailValidator.getInstance().isValid(value)) {
            submission.errors.reject("submission.errors.input.invalid",
                    [messageSource.getMessage("formField.type.${field.type}.${input}", null, locale),
                            field.label] as String[], "")
        }
    }
}