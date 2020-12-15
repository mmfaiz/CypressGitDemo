package com.matchi.dynamicforms

/**
 * @author Michael Astreiko
 */
class FormField implements Serializable {
    private static final long serialVersionUID = 12L

    String label
    String helpText
    String fieldText        // e.g. "terms of service" text

    String type
    boolean isRequired = false
    boolean isEditable = true
    //To allow facility to control whether to show or not this field
    boolean isActive = true

    //FormField belongs whether to Form or to FormTemplate
    static belongsTo = [form: Form, template: FormTemplate]
    //predefinedValues for types: RADIO, CHECKBOX, SELECTBOX, TIMERANGE_CHECKBOX
    List predefinedValues
    static hasMany = [predefinedValues: FormFieldValue]

    static constraints = {
        helpText(nullable: true)
        fieldText(nullable: true)
        template(nullable: true)
        form(nullable: true)
        type(inList: Type.values()*.name())
        isActive(nullable:true)
    }

    static mapping = {
        fieldText type: 'text'
    }

    Type getTypeEnum() {
        Type.valueOf(this.type)
    }

    enum Type {
        TEXT(SingleValueBinder, false),
        TEXTAREA(SingleValueBinder, false),
        RADIO(SingleValueBinder, false),
        CHECKBOX(CheckboxBinder, false),
        SELECTBOX(SingleValueBinder, false),
        PERSONAL_INFORMATION(PersonalInformationBinder, false),
        ADDRESS(AddressBinder, false),
        PARENT_INFORMATION(ParentInformationBinder, false),
        TERMS_OF_SERVICE(TermsOfServiceBinder, true),
        TIMERANGE_CHECKBOX(TimeRangeBinder, true),
        TEXT_CHECKBOX(TextCheckboxBinder, false),
        NUMBER_OF_OCCASIONS(SingleValueBinder, true),
        TEXT_CHECKBOX_PICKUP(TextCheckboxBinder, false),
        TEXT_CHECKBOX_ALLERGIES(TextCheckboxBinder, false),
        PLAYER_STRENGTH(TextCheckboxBinder, false),
        CLUB(ClubValueBinder, false)

        final Class<? extends FieldBinder> binder
        final boolean customizable

        Type(Class<? extends FieldBinder> binder, boolean customizable) {
            this.binder = binder
            this.customizable = customizable
        }
    }
}
