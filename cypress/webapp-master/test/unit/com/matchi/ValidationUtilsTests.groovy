package com.matchi

import org.junit.Test

import static com.matchi.ValidationUtils.*

/**
 * @author Sergei Shushkevich
 */
class ValidationUtilsTests {

    @Test
    void testIsPersonalNumberValid() {
        def orgPattern2 = /^(\d{9})$/
        def personalPatternFive = /^(\d{6}|\d{8})(?:-(\d{5}))?$/
        def securityNumberPatternFive = /^([0-9]{5})$/

        assert isPersonalNumberValid("811231", null, false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert isPersonalNumberValid("311281", null, false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", securityNumberLength: 4]))
        assert isPersonalNumberValid("19811231", null, false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert !isPersonalNumberValid("9811231", null, false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert !isPersonalNumberValid("abcdef", null, false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert !isPersonalNumberValid("811231", "1", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert !isPersonalNumberValid("811531", null, false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert !isPersonalNumberValid("811231", "abcd", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))

        //non-valid Luhn
        assert isPersonalNumberValid("160301", "1234", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, skipLuhnValidation: true]))
        assert isPersonalNumberValid("160301", "12345", false, PersonalNumberSettings.fromMap([totalStringParsePattern: personalPatternFive, securityNumberParsePattern: securityNumberPatternFive, longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5, skipLuhnValidation: true]))

        //Valid Luhn
        assert isPersonalNumberValid("160301", "1238", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
        assert isPersonalNumberValid("160301", "12342", false, PersonalNumberSettings.fromMap([totalStringParsePattern: personalPatternFive, securityNumberParsePattern: securityNumberPatternFive, longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5]))

        //valid luhn but wrong number of security digits
        assert !isPersonalNumberValid("160301", "1238", false, PersonalNumberSettings.fromMap([totalStringParsePattern: personalPatternFive, securityNumberParsePattern: securityNumberPatternFive, longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5]))
        assert !isPersonalNumberValid("160301", "12342", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4]))
    }

    @Test
    void testIsOrgNumberValid() {
        def orgPattern1 = /^(\d{6}|\d{8})(?:-(\d{4}))?$/
        def orgPattern2 = /^(\d{9})$/

        assert isOrgNumberValid("811231", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))
        assert !isOrgNumberValid("abcdef", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))
        assert isOrgNumberValid("811231-1234", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))
        assert !isOrgNumberValid("811231-1", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))
        assert !isOrgNumberValid("811231-abcd", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))
        assert isOrgNumberValid("811531", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 4, orgPattern: orgPattern1]))

        assert !isOrgNumberValid("2016abcde", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5, orgPattern: orgPattern2]))
        assert isOrgNumberValid("201601039", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5, orgPattern: orgPattern2]))
        assert !isOrgNumberValid("20160103", PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", securityNumberLength: 5, orgPattern: orgPattern2]))

        // Some Finnish
        assert !isOrgNumberValid("1234567-", PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", securityNumberLength: 4, orgPattern: /^(\d{7})-(\d{1})$/]))
        assert isOrgNumberValid("1234567-8", PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", securityNumberLength: 4, orgPattern: /^(\d{7})-(\d{1})$/]))
        assert !isOrgNumberValid("1234567", PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", securityNumberLength: 4, orgPattern: /^(\d{7})-(\d{1})$/]))
    }

    @Test
    void testIsDateOfBirthValid() {
        def orgPattern1 = /^(\d{6}|\d{8})(?:-(\d{4}))?$/

        assert isDateOfBirthValid("19930521", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", orgPattern: orgPattern1]))
        assert isDateOfBirthValid("12032010", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert isDateOfBirthValid("930521", false, PersonalNumberSettings.fromMap([longFormat: "yyyyMMdd", shortFormat: "yyMMdd", orgPattern: orgPattern1]))
        assert isDateOfBirthValid("120310", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))

        assert !isDateOfBirthValid("1234", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("123456890", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("111373", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("11131973", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("3111973", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("31173", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("19660703-0639", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("19660703-0639       ", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
        assert !isDateOfBirthValid("880422-1234", false, PersonalNumberSettings.fromMap([longFormat: "ddMMyyyy", shortFormat: "ddMMyy", orgPattern: orgPattern1]))
    }

    @Test
    void testRemoveInvalidEmailChars() {
        assert !removeInvalidEmailChars(null)
        assert "test@mail.com" == removeInvalidEmailChars("  test @ mail . com")
        assert "jon.wtte@gteborg.info" == removeInvalidEmailChars("jon.wätte@göteborg.info")
    }

    @Test
    void testValidateUserPassword() {
        assert validateUserPassword("a" * PASSWORD_MIN_LENGTH) == null
        assert validateUserPassword("a" * (PASSWORD_MIN_LENGTH - 1)) == PASSWORD_MIN_LENGTH_ERROR
        assert validateUserPassword("a" * (PASSWORD_MIN_LENGTH + 1)) == null
        assert validateUserPassword(null) == PASSWORD_MIN_LENGTH_ERROR
        assert validateUserPassword(generateLongField()) == FIELD_MAX_LENGTH_ERROR
    }

    @Test
    void testValidateUserNameLength() {
        assert validateUserNameLength(generateLongField()) == FIELD_MAX_LENGTH_ERROR
    }

    private String generateLongField() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= FIELD_MAX_LENGTH + 1; i++) {
            sb.append('a');
        }
        sb.toString()
    }
}
