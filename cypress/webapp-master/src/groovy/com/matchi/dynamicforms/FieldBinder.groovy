package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class FieldBinder {
    public static final String CHECKMARK_INPUT = "checkmark"

    FormField field
    Submission submission
    GrailsParameterMap params
    MessageSource messageSource
    Locale locale

    FieldBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        this.field = field
        this.submission = submission
        this.params = params
        this.messageSource = messageSource
        this.locale = locale
    }

    static List getInputs() {
        []
    }

    static List getOptionalInputs() {
        []
    }

    static List getOverrideableOptionalInputs() {
        []
    }

    static Boolean canIgnoreValidation(String input, Boolean canOverrideValidationRights, List overrideableOptionalInputs = null) {
        overrideableOptionalInputs = overrideableOptionalInputs ? overrideableOptionalInputs : this.overrideableOptionalInputs
        if (canOverrideValidationRights && overrideableOptionalInputs.contains(input)) {
            return true
        }
        return false
    }

    void bind() {
        if (inputs.contains(CHECKMARK_INPUT)) {
            def checked = params.boolean(field.id + "." + CHECKMARK_INPUT)
            if (checked) {
                doBind()
                validateValues()
            } else if (field.isRequired) {
                submission.errors.reject("submission.errors.required", [field.label] as String[], "")
            }
        } else {
            doBind()
            validateValues()
        }
    }

    protected void validateValue(String input, String value) {
    }

    protected void validateValues() {
    }

    protected void doBind() {
        (inputs - CHECKMARK_INPUT).eachWithIndex { it, idx ->
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