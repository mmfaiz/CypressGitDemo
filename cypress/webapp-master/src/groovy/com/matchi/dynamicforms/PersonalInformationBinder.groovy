package com.matchi.dynamicforms

import com.matchi.SecurityService
import com.matchi.Facility
import com.matchi.PersonalNumberSettings
import com.matchi.ValidationUtils
import grails.util.Holders
import org.apache.commons.validator.EmailValidator
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class PersonalInformationBinder extends FieldBinder {
    PersonalInformationBinder(FormField field, Submission submission, GrailsParameterMap params,
                              MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    static List getInputs() {
        ["firstname", "lastname", "gender", "security_number", "email", "cellphone", "telephone"]
    }

    static List getOptionalInputs() {
        ["telephone"]
    }

    static List getOverrideableOptionalInputs() {
        ["security_number"]
    }

    static Boolean canIgnoreValidation(String input, Boolean canValidationOverrideRights) {
        FieldBinder.canIgnoreValidation(input, canValidationOverrideRights, overrideableOptionalInputs)
    }

    protected void validateValue(String input, String value) {
        if (input == "security_number") {
            Facility facility = field.form.facility
            Boolean requireSecurityNumber = field.form.facility.requireSecurityNumber
            PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()
            personalNumberSettings.requireSecurityNumber = requireSecurityNumber

            def matcher = (value =~ /^(\d{6}|\d{8})(?:-(\d{4,5}))?$/)

            if (canIgnoreValidation(input, grails.util.Holders.applicationContext.securityService.hasFacilityAccessTo(field.form.facility)) &&
                (
                    (ValidationUtils.isDateOfBirthValid(matcher[0][1], false, personalNumberSettings))
                        || value.length() == 0
                )
            ) {
                //Do nothing except stop other validation
            }
            else if (requireSecurityNumber && !ValidationUtils.isPersonalNumberValid(matcher[0][1], matcher[0][2], false, personalNumberSettings)) {
                submission.errors.reject("submission.errors.input.invalid",
                        [messageSource.getMessage("formField.type.${field.type}.${input}", null, locale),
                                field.label] as String[], "")
            } else if(!requireSecurityNumber && !ValidationUtils.isDateOfBirthValid(value, false, personalNumberSettings)) {
                submission.errors.reject("submission.errors.input.invalid",
                        [messageSource.getMessage("formField.type.${field.type}.birthdate", null, locale),
                         field.label] as String[], "")
            }
        } else if (input == "email" && !EmailValidator.getInstance().isValid(value)) {
            submission.errors.reject("submission.errors.input.invalid",
                    [messageSource.getMessage("formField.type.${field.type}.${input}", null, locale),
                            field.label] as String[], "")
        }
    }

    protected void doBind() {
        inputs.eachWithIndex { it, idx ->
            def value = params."${field.id}.${it}"?.trim()
            if (value) {
                submission.addToValues(new SubmissionValue(value: value, input: it, valueIndex: idx,
                        label: field.label, fieldId: field.id, fieldType: field.type))
                try {
                    validateValue(it, value)
                } catch (e) {
                    submission.errors.reject("submission.errors.input.invalid",
                            [messageSource.getMessage("formField.type.${field.type}.${it}", null, locale),
                                    field.label] as String[], "")
                }
            } else if (field.isRequired && !optionalInputs.contains(it) &&
                    !canIgnoreValidation(it, grails.util.Holders.applicationContext.securityService.hasFacilityAccessTo(field.form.facility))) {
                submission.errors.reject("submission.errors.input.required",
                        [messageSource.getMessage("formField.type.${field.type}.${it}", null, locale),
                                field.label] as String[], "")
            }
        }
    }
}