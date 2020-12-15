package com.matchi

/**
 * @author Sergei Shushkevich
 */
class ValidationUtils {

    public final static EMAIL_REGEX = /^[a-zA-Z0-9\.\_\%\+\-]+\@[a-zA-Z0-9\.\-]+\.[a-zA-Z]{2,63}$/
    public final static int PASSWORD_MIN_LENGTH = 7
    public final static String PASSWORD_MIN_LENGTH_ERROR = "user.password.minLength.error"
    public final static int FIELD_MAX_LENGTH = 255
    public final static String FIELD_MAX_LENGTH_ERROR = "user.field.maxLength.error"

    static boolean isPersonalNumberValid(String dateString, String securityNumber, boolean isCompany, PersonalNumberSettings settings) {
        if (!dateString) {
            return false
        }

        if (isCompany) {
            return isOrgNumberValid(dateString + (securityNumber ? "-" + securityNumber : ""), settings)
        }

        // Generates a date from the personal number string. Returns null if the format is wrong.
        Date dateOfBirth = DateUtil.getDateOfBirth(dateString, settings.shortFormat, settings.longFormat)

        // if dateOfBirth couldn't be parsed then it's null and not accepted
        if (!dateOfBirth) {
            return false
        }

        // If the security number isn't set then we should accept it and not validate anymore
        // if security number is required by facility then it should not be accepted

        if (!securityNumber) {
            if (settings.requireSecurityNumber) {
                return false
            }
            return true
        }

        // If security number is set then we should validate that it is correct even if it is not required
        // The security number must match in size with facilitys country
        if (securityNumber && securityNumber.size() != settings.securityNumberLength) {
            return false
        }

        // Security Number must only contain number
        if (!securityNumber.matches(settings.securityNumberParsePattern)) {
            return false
        }

        // Luhn validation sums the n-1 first numbers and make the last digit a control digit which checks if the
        // total personal number is correct.
        // Luhn validation is not applicable for all countries personal numbers
        if (!settings.skipLuhnValidation) {
            // if we should do Luhn validation, do it on the concatenated number, note the numbers are strings
            String totalPersonalNumber = dateOfBirth.format(settings.shortFormat) + securityNumber
            if (!LuhnValidator.validate(totalPersonalNumber)) {
                return false
            }
        }

        //If all validations are run and accepted then the personal number is correct
        return true
    }

    static boolean isOrgNumberValid(String value, PersonalNumberSettings settings) {
        def matcher = (value =~ settings.orgPattern)
        if (matcher.matches()) {
            return true
        } else {
            return false
        }
    }

    static boolean isDateOfBirthValid(String date, boolean isCompany, PersonalNumberSettings settings) {
        def matcher = isCompany ? (date =~ settings.orgPattern) : (date =~ /^(\d{6}|\d{8})$/)
        if (matcher.matches()) {
            if (!isCompany) {
                Date dateOfBirth = DateUtil.getDateOfBirth(date, settings.shortFormat, settings.longFormat)
                if (!dateOfBirth) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    static String removeInvalidEmailChars(String email) {
        email?.replaceAll(/[^a-zA-Z0-9.!@#$%&'*+\/=?^_`{|}~-]+/, "")
    }

    static boolean isEmailAddress(String str) {
        return str.matches(EMAIL_REGEX)
    }

    static List getEmailsFromString(String str, String separator) {
        return str.split(separator).collect { String s -> s.trim() }.findAll { String s -> isEmailAddress(s) }
    }

    static String validateUserPassword(String password) {
        if (password?.size() < PASSWORD_MIN_LENGTH) {
            return PASSWORD_MIN_LENGTH_ERROR
        }
        if (password?.size() > FIELD_MAX_LENGTH) {
            return FIELD_MAX_LENGTH_ERROR;
        }

        return null
    }

    static String validateUserNameLength(String name) {
        if (name?.size() > FIELD_MAX_LENGTH) {
            return FIELD_MAX_LENGTH_ERROR;
        }

 return null
    }
}
