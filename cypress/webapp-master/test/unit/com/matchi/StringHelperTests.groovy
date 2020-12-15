package com.matchi

class StringHelperTests extends GroovyTestCase {

    void testSafeSubstringZipCode() {
        String zipCode = "43137"
        String result = StringHelper.safeSubstring(zipCode, 0, 10)

        assert result.equals(zipCode)
    }

    void testSafeSubstringEmpty() {
        String zipCode = ""
        String result = StringHelper.safeSubstring(zipCode, 0, 10)

        assert result.equals(zipCode)
    }

    void testSafeSubstringNull() {
        String zipCode = null
        String result = StringHelper.safeSubstring(zipCode, 0, 10)

        assert result == null
    }

    void testSafeSubstring10chars() {
        String str = "0123456789"
        String result = StringHelper.safeSubstring(str, 0, 10)

        assert result.equals(str)
    }

    void testSafeSubstring11chars() {
        String str = "01234567891"
        String result = StringHelper.safeSubstring(str, 0, 10)

        assert result.equals("0123456789")
    }

    void testExtendedTrimForHiddenChars() {
        String str1 = "1@3.5​​ " //includes hidden space characters
        assert str1.length() == 8
        assert StringHelper.extendedTrim(str1) == "1@3.5"
        assert StringHelper.extendedTrim(str1).length() == 5
    }

    void testExtendedTrimForWhiteSpaceChars() {
        String str2 = " åäö \n"
        assert StringHelper.extendedTrim(str2) == "åäö"
        assert StringHelper.extendedTrim(str2).length() == 3
    }

    void testExtendedTrimForNormalString() {
        String str2 = "The quick brown fox jumps over the lazy dog"
        assert StringHelper.extendedTrim(str2) == str2
    }

    void testExtendedTrimForEmptyString() {
        String str2 = ""
        assert str2.length() == 0
        assert StringHelper.extendedTrim(str2) == ""
        assert StringHelper.extendedTrim(str2).length() == 0
    }

    void testExtendedTrimForNullString() {
        String str1 = null
        shouldFail(NullPointerException) {
            assert str1.length() == 0
        }

        assert StringHelper.extendedTrim(str1) == null
        shouldFail(NullPointerException) {
            assert StringHelper.extendedTrim(str1).length() == 0
        }
    }
}
