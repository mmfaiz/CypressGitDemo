package com.matchi

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class PhoneUtil {

    private static String DEFAULT_COUNTRY_CODE = "SE"

    static boolean isValid(String numberText, String countryCode = DEFAULT_COUNTRY_CODE) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(numberText, countryCode)
            return phoneUtil.isValidNumber(phoneNumber)
        } catch(NumberParseException npe) {
            return false
        }

    }

    static String convertToInternationalFormat(String numberText, String countryCode = DEFAULT_COUNTRY_CODE) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(numberText, countryCode)

        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

}
