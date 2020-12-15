package com.matchi

/**
 * Class to represent different formats for personal numbers
 */
class PersonalNumberSettings {

    /**
     *  Default settings
     */
    final static String LONG_FORMAT_DEFAULT = "yyyyMMdd"
    final static String SHORT_FORMAT_DEFAULT = "yyMMdd"
    final static String READABLE_FORMAT_DEFAULT = "yymmdd"
    final static int SECURITY_NUMBER_LENGTH_DEFAULT = 0
    final static String ORG_PATTERN_DEFAULT = /^.*$/
    final static String ORG_FORMAT_DEFAULT = "X"
    final static boolean REQUIRE_SECURITY_NUMBER_DEFAULT = false
    final static boolean SKIP_LUHN_VALDIATION_DEFAULT = false
    final static String DEFAULT_TOTAL_STRING_PARSE_PATTERN = /^(\d{6}|\d{8})(?:-(\d{4}))?$/
    final static String DEFAULT_SECURITY_NUMBER_PARSE_PATTERN = /^([0-9]{4})$/

    /**
     * These are fetched from settings and should be immutable
     */
    final String longFormat
    final String shortFormat
    final String readableFormat
    final int securityNumberLength
    final String orgPattern
    final String orgFormat
    final String totalStringParsePattern
    final String securityNumberParsePattern

    /**
     * Case specific variables.
     */
    boolean requireSecurityNumber = REQUIRE_SECURITY_NUMBER_DEFAULT
    boolean skipLuhnValidation = SKIP_LUHN_VALDIATION_DEFAULT

    PersonalNumberSettings(String longFormat = LONG_FORMAT_DEFAULT,
                           String shortFormat = SHORT_FORMAT_DEFAULT,
                           String readableFormat = READABLE_FORMAT_DEFAULT,
                           int securityNumberLength = SECURITY_NUMBER_LENGTH_DEFAULT,
                           String orgPattern = ORG_PATTERN_DEFAULT,
                           String orgFormat= ORG_FORMAT_DEFAULT,
                           boolean skipLuhnValidation = SKIP_LUHN_VALDIATION_DEFAULT,
                           String totalStringParsePattern = DEFAULT_TOTAL_STRING_PARSE_PATTERN,
                           String securityNumberParsePattern = DEFAULT_SECURITY_NUMBER_PARSE_PATTERN) {

        this.longFormat = longFormat
        this.shortFormat = shortFormat
        this.readableFormat = readableFormat
        this.securityNumberLength = securityNumberLength
        this.orgPattern = orgPattern
        this.orgFormat = orgFormat
        this.skipLuhnValidation = skipLuhnValidation
        this.totalStringParsePattern = totalStringParsePattern
        this.securityNumberParsePattern = securityNumberParsePattern
    }

    static PersonalNumberSettings fromMap(Map map) {
        if(!map) {
            return new PersonalNumberSettings()
        }

        return new PersonalNumberSettings(
                map.longFormat as String ?: LONG_FORMAT_DEFAULT,
                map.shortFormat as String ?: SHORT_FORMAT_DEFAULT,
                map.readableFormat as String ?: READABLE_FORMAT_DEFAULT,
                map.securityNumberLength as Integer ?: SECURITY_NUMBER_LENGTH_DEFAULT,
                map.orgPattern as String ?: ORG_PATTERN_DEFAULT,
                map.orgFormat as String ?: ORG_FORMAT_DEFAULT,
                map.skipLuhnValidation as Boolean ?: SKIP_LUHN_VALDIATION_DEFAULT,
                map.totalStringParsePattern as String ?: DEFAULT_TOTAL_STRING_PARSE_PATTERN,
                map.securityNumberParsePattern as String ?: DEFAULT_SECURITY_NUMBER_PARSE_PATTERN
        )
    }
}
