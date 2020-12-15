package com.matchi.dynamicforms

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

class TimeRangeBinder extends FieldBinder {

    TimeRangeBinder(FormField field, Submission submission, GrailsParameterMap params,
            MessageSource messageSource, Locale locale) {
        super(field, submission, params, messageSource, locale)
    }

    static List getInputs() {
        [CHECKMARK_INPUT, "from", "to"]
    }

    protected void validateValues() {
        if (submission.values) {
            submission.values.groupBy {
                it.inputGroup
            }.each { key, items ->
                if (items.size() == 2) {
                    def from = Date.parse("HH:mm", items.find { it.input == "from" }.value)
                    def to = Date.parse("HH:mm", items.find { it.input == "to" }.value)
                    if (from >= to) {
                        submission.errors.reject("submission.errors.invalid", [field.label] as String[], "")
                    }
                }
            }
        } else if (field.isRequired) {
            submission.errors.reject("submission.errors.required", [field.label] as String[], "")
        }
    }

    void bind() {
        field.predefinedValues.eachWithIndex { pv, i ->
            def checked = params.boolean(field.id + "." + i + "." + CHECKMARK_INPUT)
            if (checked) {
                (inputs - CHECKMARK_INPUT).eachWithIndex { it, idx ->
                    def value = params."${field.id}.${i}.${it}"?.trim()
                    if (value) {
                        submission.addToValues(new SubmissionValue(value: value, input: it, inputGroup: pv.value,
                                valueIndex: i, label: field.label, fieldId: field.id, fieldType: field.type))
                    } else if (field.isRequired) {
                        submission.errors.reject("submission.errors.input.required",
                                [messageSource.getMessage("formField.type.${field.type}.${it}", null, locale),
                                        field.label] as String[], "")
                    }
                }
            }
        }

        validateValues()
    }
}