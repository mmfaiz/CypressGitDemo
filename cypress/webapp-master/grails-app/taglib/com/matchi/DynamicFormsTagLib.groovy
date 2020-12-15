package com.matchi

/**
 * @author Sergei Shushkevich
 */
class DynamicFormsTagLib {

    def renderSubmittedValue = { attrs, body ->
        def values = attrs.values
        if (!values) {
            throwTagError("Tag [renderSubmittedValue] is missing required attribute [values]")
        }

        values.sort{ i1, i2 ->
            return i1.valueIndex <=> i2.valueIndex
        }.groupBy {
            it.inputGroup
        }.eachWithIndex { label, items, i ->
            if (label) {
                if (i) {
                    out << "<br/>"
                }
                out << label.encodeAsHTML() << " "
            }

            items.sort { i1, i2 ->
                i1.valueIndex <=> i2.valueIndex ?: i1.input <=> i2.input
            }.eachWithIndex { item, j ->
                if (!label && j) {
                    out << "<br/>"
                }
                renderValue(item)
            }
        }
    }

    private void renderValue(submissionValue) {
        if (submissionValue.input) {
            out << message(code: "formField.type.${submissionValue.fieldType}.${submissionValue.input}") << ": "
        }
        out << submissionValue.value.encodeAsHTML() << " "
    }
}
