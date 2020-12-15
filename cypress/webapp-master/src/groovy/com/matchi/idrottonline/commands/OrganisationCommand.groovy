package com.matchi.idrottonline.commands

import grails.validation.Validateable

@Validateable()
class OrganisationCommand {

    String optionType
    String externalOrganisationIdentification
    String organisationIdentityNumber
    List<SportsCommand> sports

    static constraints = {
        optionType matches: OptionType.UPDATE.toString()
        externalOrganisationIdentification blank: false
        organisationIdentityNumber blank: false
        sports minSize: 1, validator: { val -> val.every { it.validate() } }
    }

    enum OptionType {
        UPDATE("Update")

        OptionType(String value) { this.value = value }
        private final String value
        public String toString() { return value }
    }
}
