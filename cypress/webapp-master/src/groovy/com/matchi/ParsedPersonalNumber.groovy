package com.matchi

/**
 * Represents a personal number parsed from String to String and String.
 * Does not validate, but ready for validation!
 */
class ParsedPersonalNumber {

    final String dateString
    final String securityNumber

    ParsedPersonalNumber(final String value, final PersonalNumberSettings settings) {
        def matcher = (value =~ settings.totalStringParsePattern)

        if(matcher.matches()) {
            dateString =  matcher[0][1].toString()

            final String unverifiedSecurityNumber = matcher[0][2]
            if(unverifiedSecurityNumber && unverifiedSecurityNumber.size() == settings.securityNumberLength) {
                securityNumber = unverifiedSecurityNumber
            }
        }
    }

}
