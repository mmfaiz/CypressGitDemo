package com.matchi

import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test

@TestMixin(DomainClassUnitTestMixin)
class BankGiroUtilTests {
    def bankGiroUtil
    def file2
    def file3
    def file4
    def file5

    @Before
    void setup() {
        bankGiroUtil = new BankGiroUtil()
        file2 = new File("web-app/example/BgMaxfil2.txt")
        file3 = new File("web-app/example/BgMaxfil3.txt")
        file4 = new File("web-app/example/BBSNorwayfile.txt")
        file5 = new File("web-app/example/BBSNorwayfile2.txt")
    }

    @Test
    void testThatRecordsAreBeingParsed() {
        def records = bankGiroUtil.getBgMaxRecords(file2)

        // TODO: depending on server locale returns 4 or 5 records
        assert records.size() >= 4
    }

    @Test
    void testThatBBSRecordsAreBeingParsed() {
        def records = bankGiroUtil.getBBSRecords(file5)

        assert records.size() == 20
        records.each {
            assert it.value.amount > 0
            assert it.value.date.toDate().after(new LocalDate(1990, 1, 1).toDate())
        }
    }

    @Test
    void testBBSRecordDetails() {
        def records = bankGiroUtil.getBBSRecords(file4)

        assert records.size() == 1

        def recordDetails = records.entrySet().toList()[0]
        assert recordDetails.key == '2227775'
        assert recordDetails.value.amount == 850
        assert recordDetails.value.date.equals(new LocalDate(2015, 11, 12))
    }
}
