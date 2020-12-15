package com.matchi

import com.matchi.ParsedPersonalNumber
import org.junit.Test;

class ParsedPersonalNumberTests {

    @Test
    void ParsedPersonalNumberTest() {
        PersonalNumberSettings pnSetting1 = new PersonalNumberSettings("yyyyMMdd", "yyMMdd", "yymmdd", 4, /^(\d{6}|\d{8})(?:-(\d{4}))?$/, "??XXXXXX-XXXX", false)

        ParsedPersonalNumber pn1 = new ParsedPersonalNumber("930618-7635", pnSetting1)
        assert pn1.securityNumber == "7635"
        assert pn1.dateString == "930618"

        //invalid luhn, but shouldn't matter
        ParsedPersonalNumber pn2 = new ParsedPersonalNumber("930618-7636", pnSetting1)
        assert pn2.securityNumber == "7636"
        assert pn2.dateString == "930618"

        ParsedPersonalNumber pn3 = new ParsedPersonalNumber("19930618-7635", pnSetting1)
        assert pn3.securityNumber == "7635"
        assert pn3.dateString == "19930618"

        ParsedPersonalNumber pn4 = new ParsedPersonalNumber("30618-7635", pnSetting1)
        assert pn4.securityNumber == null
        assert pn4.dateString == null

        ParsedPersonalNumber pn5 = new ParsedPersonalNumber("30618-765", pnSetting1)
        assert pn5.securityNumber == null
        assert pn5.dateString == null

        ParsedPersonalNumber pn6 = new ParsedPersonalNumber("930618-765", pnSetting1)
        assert pn6.securityNumber == null
        assert pn6.dateString == null
    }
}
