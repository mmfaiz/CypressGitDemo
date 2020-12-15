package com.matchi.dynamicforms

/**
 * @author Sergei Shushkevich
 */
class SubmissionValue implements Serializable {
    private static final long serialVersionUID = 12L

    static belongsTo = [submission: Submission]

    String label    // field label
    String input    // to distinguish submitted values for FormFields with multiple input fields
    String inputGroup
    String value
    Integer valueIndex = 0      // submitted values for complex fields should be displayed in proper order

    // field meta data (might be removed in future)
    Long fieldId
    String fieldType

    static constraints = {
        label(maxSize: 255)
        input(nullable: true, maxSize: 255)
        inputGroup(nullable: true, maxSize: 255)
        value(maxSize: 255, markup: true)
        fieldId(nullable: true)
        fieldType(nullable: true, maxSize: 255)
    }

    static mapping = {
        version false
        label index: "submission_value_label_idx"
        fieldType index: "submission_value_type_value_idx"
        value index: "submission_value_type_value_idx"
    }
}
