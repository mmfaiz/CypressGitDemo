package com.matchi.dynamicforms

/**
 * User to store predefined Field values (selectbox, radio buttons, etc) as well as for custom User values.
 * If submission defined - it is User defined field
 *
 * @author Michael Astreiko
 */
class FormFieldValue implements Serializable {
    private static final long serialVersionUID = 12L

    static belongsTo = [field: FormField]

    String value
    String minValue
    String maxValue
    boolean isActive = true

    static constraints = {
        minValue(nullable: true, maxSize: 255)
        maxValue(nullable: true, maxSize: 255)
    }
}
