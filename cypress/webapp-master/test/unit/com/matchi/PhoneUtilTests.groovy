package com.matchi

import org.junit.Test

class PhoneUtilTests  {

    @Test
    public void testValid() {

        assert true
    }

    @Test
    public void testConvertNumberWithoutCountryCode() {
        assert PhoneUtil.convertToInternationalFormat("0703121212") == "+46703121212"
        assert PhoneUtil.isValid("0703121212")
    }

    @Test
    public void testConvertNumberWithoutCountryCodeWithDelimiter() {
        assert PhoneUtil.convertToInternationalFormat("0703-121212") == "+46703121212"
    }

    @Test
    public void testConvertNumberWithoutCountryCodeWithSpaces() {
        assert PhoneUtil.convertToInternationalFormat("70 312 12 12") == "+46703121212"
        assert PhoneUtil.convertToInternationalFormat("  70 312 12 12 ") == "+46703121212"
    }

    @Test
    public void testConvertNumberWithCountryCode() {
        assert PhoneUtil.convertToInternationalFormat("0046703121212") == "+46703121212"
        assert PhoneUtil.convertToInternationalFormat("0046 0703121212") == "+46703121212"
        assert PhoneUtil.convertToInternationalFormat("+46 70 312 12 12") == "+46703121212"
    }

    @Test
    public void testIsValid() {
        assert PhoneUtil.isValid("0703040404")
    }

    @Test
    public void testNotValid() {
        assert !PhoneUtil.isValid("")
    }

}
