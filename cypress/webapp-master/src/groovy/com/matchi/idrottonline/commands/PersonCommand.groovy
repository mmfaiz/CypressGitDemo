package com.matchi.idrottonline.commands

import com.fasterxml.jackson.annotation.JsonIgnore
import com.matchi.Customer
import com.matchi.LuhnValidator
import com.matchi.StringHelper
import grails.validation.Validateable

@Validateable()
class PersonCommand {

    public static final int BIRTH_YEAR_MINIMUM_ALLOWED_VALUE = 1800
    public static final int BIRTH_YEAR_MAXIMUM_ALLOWED_VALUE = 2100

    String createDate
    String changeDate
    String optionType
    String birthDate
    String personalIdentityNumber
    String firstName
    String lastName
    String gender
    List<OrganisationsCommand> organisations

    // The identifier of person in external application. We have no requirement about the format of the string.
    // But you have to make sure it is unique in scope of you application id and identify same person between batches.
    // NOTE! Customer id will not be unique between facilities/batches because personalidentitynumber must reference the same externalIdentification.
    String externalIdentification

    @JsonIgnore
    String customerId

    static constraints = {
        createDate blank: false
        changeDate blank: false
        optionType blank: false, matches: OptionType.UPDATE.toString()
        personalIdentityNumber blank: false
        firstName blank: false
        lastName blank: false
        gender blank: false, validator: { val, obj ->

            // Must contain exact 13 characters for IdrottOnline to accept in format: YYYYMMDD-XXXX
            if(obj.personalIdentityNumber?.length() != 13)
                return ['personCommand.personalIdentityNumber.invalidFormat']

            String securityNumber = null
            obj.personalIdentityNumber?.find(~/^(\d{6}|\d{8})(?:-(\d{4}))?$/) { match, dob, sn -> securityNumber = sn }

            // Validate year of birth to be between 1800-2100 because IdrottOnline has that requirement.
            String yearString = obj.personalIdentityNumber?.take(4)
            if(yearString?.isInteger()){
                int year = yearString.toInteger()
                if(year < BIRTH_YEAR_MINIMUM_ALLOWED_VALUE || year > BIRTH_YEAR_MAXIMUM_ALLOWED_VALUE)
                    return false
            }

            // Spaces are not allowed as a suffix because it makes the batch corrupt in IdrottOnline.
            if(StringHelper.endsWithWhiteSpace(obj.personalIdentityNumber))
                return false

            // Awkward Luhn validation to avoid security numbers such as 0000.
            boolean validLuhn = false
            try {
                String tmpPersonalIdentityNumber = obj.personalIdentityNumber.replace("-", "")
                validLuhn = LuhnValidator.validate(tmpPersonalIdentityNumber.substring(2, tmpPersonalIdentityNumber.length()))
            } catch (NumberFormatException e) {
                // Ignore...
            }

            if (!validLuhn) {
                return ['personCommand.personalIdentityNumber.incorrect']
            }

            if (securityNumber) {
                def controlNr = Integer.parseInt(securityNumber[2])
                return LuhnValidator.validateType(controlNr, Customer.CustomerType."${val.toUpperCase()}") ? true : ['invalid.gender']
            }
            return true
        }
        organisations minSize: 1, validator: { val -> val.every { it.validate() } }
        birthDate blank: false
        externalIdentification blank: false
        customerId nullable: true
    }


    enum OptionType {
        UPDATE("Update")

        OptionType(String value) { this.value = value }
        private final String value
        public String toString() { return value }
    }
}
