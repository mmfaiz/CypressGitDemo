package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.MessageSource

import static org.junit.Assert.assertEquals

@TestMixin(GrailsUnitTestMixin)
@Mock([Facility])
class DateUtilTests {

    def dateUtil
    def mockMessageSource

	@Before
	void setUp() {
        dateUtil = new DateUtil()

        mockMessageSource = mockFor(MessageSource)
        dateUtil.messageSource = mockMessageSource.createMock()
        dateUtil.grailsApplication = [config: [customer: [personalNumber: [settings: [:]]]]]
	}

    @After
	void tearDown() {
	}

    @Test
	void testEventHours() {
		Calendar calendar = Calendar.getInstance()
		calendar.set(Calendar.MINUTE, 30)
        calendar.set(Calendar.SECOND, 30)
        calendar.set(Calendar.MILLISECOND, 333)
		Date evenDate = dateUtil.evenHour(calendar.getTime())
		assertEquals 0, evenDate.getMinutes()
        assertEquals 0, evenDate.getSeconds()
	}

    @Test
	void testFormatDate() {
		Calendar calendar = Calendar.getInstance()
		calendar.set(Calendar.YEAR, 2011)
		calendar.set(Calendar.MONTH, 1)
		calendar.set(Calendar.DAY_OF_MONTH, 15)
		String result = dateUtil.formatDate(calendar.getTime())
		
		assertEquals "2011-02-15", result
	}

    @Test
	void testDateIntervalRightNumberOfDays() {
		DateTime fromDate = new DateTime(2010,12,29,12,0,0,0);
		DateTime toDate = new DateTime(2011,1,2,12,0,0,0);
		
		def results = dateUtil.createDateIntervals(fromDate, toDate)
		assertEquals 5, results.size()
	}

    @Test
	void testDateIntervalCorrectFirstDate() {
		DateTime fromDate = new DateTime(2011,1,1,12,0,0,0);
		DateTime toDate = new DateTime(2011,1,10,12,0,0,0);
		
		def results = dateUtil.createDateIntervals(fromDate, toDate)
		def firstDate = results.get(0)
		assertEquals 1, firstDate.dayOfMonth().get()
		assertEquals 2011, firstDate.getYear()
	}

    @Test
	void testTimeSpanRightNumberOfSpans() {
		DateTime date = new DateTime(2011,1,1,12,0,0,0);
		
		def results = dateUtil.createTimeSpans(date, 9, 20)
		
		assertEquals 12, results.size()
	}

    @Test
	void testTimeSpanRightWithOneHour() {
		DateTime date = new DateTime(2011,1,1,12,0,0,0);
		
		def results = dateUtil.createTimeSpans(date, 10, 10)
		
		assertEquals 1, results.size()
	}

    @Test
	void testTimeSpanRightEndingMinuteAlwaysZero() {
		DateTime date = new DateTime(2011,1,1,12,0,0,0);
		
		def results = dateUtil.createTimeSpans(date, 10, 11)
		DateTime first = results.get(0).end
		
		assertEquals 0, first.getMinuteOfHour()
		
	}

    @Test
    void testTimeEndWithZero() {
        DateTime date = new DateTime(2011,1,1,12,0,0,0);

        def results = dateUtil.createTimeSpans(date, 10, 0)

        assertEquals 15, results.size()
    }

    @Test
    void testComposeBirthDate() {
        Date date = new DateTime(2012,1,1,0,0,0,0).toDate();

        def composedDate = dateUtil.composeBirthDate(2012, date.format("MMMM"), 1)

        assertEquals composedDate, date
    }

    @Test
	void testFirstAndLastDay() {
		def days = dateUtil.composeMonthlyCalendar(2011, 1);
		
		DateMidnight firstDate = days.get(0)
		
		assertEquals 2010, firstDate.getYear()
		assertEquals DateTimeConstants.DECEMBER, firstDate.monthOfYear().get()
		assertEquals 27, firstDate.dayOfMonth().get()
	}

    @Test
	void testRightNumberOfDaysInComposeMonthlyCalendar() {
		def days = dateUtil.composeMonthlyCalendar(2011, 1);
		assertEquals 42, days.size()
	}

    @Test
    void testRightNumberOfDaysInComposeWeeklyCalendar() {
        def days = dateUtil.composeWeeklyCalendar(2012, 1);
        assertEquals 7, days.size()

        days = dateUtil.composeWeeklyCalendar(2012, 15);
        assertEquals 7, days.size()

        days = dateUtil.composeWeeklyCalendar(2012, 52);
        assertEquals 7, days.size()

        days = dateUtil.composeWeeklyCalendar(2014, 1);
        println(days)
    }

    @Test
    void testFormatPeriod() {
        def period = new Period(2, 1, 0,0)
        assert dateUtil.format(period) == "02:01"
    }

    @Test
    void testFormatPeriodWithZeroes() {
        def period = new Period(2, 0, 0,0)
        assert dateUtil.format(period) == "02:00"
    }

    @Test
    void testFormatPeriodWithOnlyZeroes() {
        def period = new Period(0, 0, 0,0)
        assert dateUtil.format(period) == "00:00"
    }

    /*
     * Test of flatten intervals
     */
    @Test
    void testSortStart() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,10,0), new DateTime(2012,1,1,11,0))
        intervals << new Interval(new DateTime(2012,1,1,14,0), new DateTime(2012,1,1,15,0))
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))

        assert dateUtil.sort(intervals).get(0).start.getHourOfDay() == 10
        assert dateUtil.sort(intervals).get(1).start.getHourOfDay() == 12
        assert dateUtil.sort(intervals).get(2).start.getHourOfDay() == 14
    }

    @Test
    void testSortStartAndDuration() {
        def intervals = []
        intervals << new Interval(createByHour(10), createByHour(15))
        intervals << new Interval(createByHour(10), createByHour(11))
        intervals << new Interval(createByHour(10), createByHour(13))
        intervals << new Interval(createByHour(10), createByHour(16))

        def result = dateUtil.sort(intervals)

        def last
        result.each { Interval it ->
            if(last) {
                assert it.toDurationMillis() >= last.toDurationMillis()
            }
            last = it
        }

        assert result.get(0).end.getHourOfDay() == 11
        assert result.get(1).end.getHourOfDay() == 13
        assert result.get(2).end.getHourOfDay() == 15
        assert result.get(3).end.getHourOfDay() == 16
    }

    @Test
    void testNoMerge() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))
        intervals << new Interval(new DateTime(2012,1,1,14,0), new DateTime(2012,1,1,15,0))

        assert dateUtil.flatten(intervals).size() == 2
    }

    @Test
    void testFlattenMergeWhenOverlaps() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))
        intervals << new Interval(new DateTime(2012,1,1,12,30), new DateTime(2012,1,1,15,0))

        def result = dateUtil.flatten(intervals)
        assert result.size() == 1
    }

    @Test
    void testFlattenMergeWhenAbuts() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))
        intervals << new Interval(new DateTime(2012,1,1,13,0), new DateTime(2012,1,1,14,0))

        def result = dateUtil.flatten(intervals)
        assert result.size() == 1
        assert result.get(0).start.getHourOfDay() == 12
        assert result.get(0).end.getHourOfDay() == 14
    }

    @Test
    void testNoFlattenMergeWhenNotAbuts() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))
        intervals << new Interval(new DateTime(2012,1,1,13,1), new DateTime(2012,1,1,14,0))

        def result = dateUtil.flatten(intervals)
        assert result.size() == 2
        assert result.get(0).start.getHourOfDay() == 12
        assert result.get(0).end.getHourOfDay() == 13
    }

    @Test
    void testFlattenMergeWhenWithin() {
        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,10), new DateTime(2012,1,1,12,20))
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))

        def result = dateUtil.flatten(intervals)
        assert result.size() == 1
        assert result.get(0).start.getHourOfDay() == 12
        assert result.get(0).end.getHourOfDay() == 13
    }

    @Test
    void testMultipleMergeWhenOverlaps() {

        def intervals = []
        intervals << new Interval(new DateTime(2012,1,1,12,0), new DateTime(2012,1,1,13,0))
        intervals << new Interval(new DateTime(2012,1,1,12,30), new DateTime(2012,1,1,15,0))
        intervals << new Interval(new DateTime(2012,1,1,15,0), new DateTime(2012,1,1,16,0))
        intervals << new Interval(new DateTime(2012,1,1,15,20), new DateTime(2012,1,1,15,25))
        intervals << new Interval(new DateTime(2012,1,1,19,0), new DateTime(2012,1,1,20,0))

        def result = dateUtil.flatten(intervals)

        assert result.size() == 2
        assert result.get(0).start.getHourOfDay() == 12
        assert result.get(0).end.getHourOfDay() == 16

        assert result.get(1).start.getHourOfDay() == 19
        assert result.get(1).end.getHourOfDay() == 20

    }

    @Test
    void testParse2DigitBirthYear() {
        1974 == DateUtil.parse2DigitBirthYear("740102")
        2005 == DateUtil.parse2DigitBirthYear("051231")
    }

    @Test
    void testBeginningMonthJanuary() {
        DateTime dt = new DateTime("2017-01-12")
        DateTime result = dateUtil.toBeginningOfMonth(dt)

        assert result.dayOfMonth == 1
        assert result.monthOfYear == 1
        assert result.year == 2017

        assert dt.dayOfMonth == 12
        assert dt.monthOfYear == 1
        assert dt.year == 2017
    }

    @Test
    void testEndOfMonthJanuary() {
        DateTime dt = new DateTime("2017-01-12")
        DateTime result = dateUtil.toEndOfMonth(dt)

        assert result.dayOfMonth == 31
        assert result.monthOfYear == 1
        assert result.year == 2017

        assert dt.dayOfMonth == 12
        assert dt.monthOfYear == 1
        assert dt.year == 2017
    }

    @Test
    void testEndOfMonthFebruary() {
        DateTime dt = new DateTime("2017-02-12")
        DateTime result = dateUtil.toEndOfMonth(dt)

        assert result.dayOfMonth == 28
        assert result.monthOfYear == 2
        assert result.year == 2017

        assert dt.dayOfMonth == 12
        assert dt.monthOfYear == 2
        assert dt.year == 2017
    }

    @Test
    void testEndOfMonthFebruaryLeapYear() {
        DateTime dt = new DateTime("2020-02-12")
        DateTime result = dateUtil.toEndOfMonth(dt)

        assert result.dayOfMonth == 29
        assert result.monthOfYear == 2
        assert result.year == 2020

        assert dt.dayOfMonth == 12
        assert dt.monthOfYear == 2
        assert dt.year == 2020
    }

    @Test
    void testEndOfMonthDecember() {
        DateTime dt = new DateTime("2017-12-09")
        DateTime result = dateUtil.toEndOfMonth(dt)

        assert result.dayOfMonth == 31
        assert result.monthOfYear == 12
        assert result.year == 2017

        assert dt.dayOfMonth == 9
        assert dt.monthOfYear == 12
        assert dt.year == 2017
    }

    @Test
    void testParseDateOfBirthWithFullDateFormatAndLowAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def dob = "20051210"

        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyyyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)

        assert dateTime != null
        assert dateTime.dayOfMonth == 10
        assert dateTime.monthOfYear == 12
        assert dateTime.year == 2005
    }

    @Test
    void testParseDateOfBirthWithShortDateFormatAndLowAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def dob = "051210"

        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)

        assert dateTime != null
        assert dateTime.dayOfMonth == 10
        assert dateTime.monthOfYear == 12
        assert dateTime.year == 2005
    }

    @Test
    void testParseDateOfBirthWithFullDateFormatAndHighAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def dob = "19201210"

        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyyyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)

        assert dateTime != null
        assert dateTime.dayOfMonth == 10
        assert dateTime.monthOfYear == 12
        assert dateTime.year == 1920
    }

    @Test
    void testParseDateOfBirthWithShortDateFormatAndWithLowAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def initDate = LocalDate.now().plusDays(1)
        def dob = initDate.toString(DateTimeFormat.forPattern("yyMMdd"))

        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)

        assert dateTime != null
        assert dateTime.dayOfMonth == initDate.dayOfMonth
        assert dateTime.monthOfYear == initDate.monthOfYear
        assert dateTime.year == initDate.year - 100
    }

    @Test
    void testParseDateOfBirthWithShortDateFormatAndWithMediumAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def dob = "281210"

        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)
        assert dateTime != null
        assert dateTime.dayOfMonth == 10
        assert dateTime.monthOfYear == 12
        assert dateTime.year == 1928
    }

    @Test
    void testParseDateOfBirthWithFullDateFormatAndWithHighAge() {
        Facility facility = new Facility(language: "sv").save(validate: false)
        def dob = "951210"
        mockMessageSource.demand.getMessage(1) { propRef, na, locale ->
            return "yyMMdd"
        }

        Date date = dateUtil.parseDateOfBirth(facility, dob)
        DateTime dateTime = new DateTime(date)

        assert dateTime != null
        assert dateTime.dayOfMonth == 10
        assert dateTime.monthOfYear == 12
        assert dateTime.year == 1995
    }

    @Test
    void testGetUniqueDates() {
        Date date1 = new Date(year: 2018, month: 6, date: 1, hours: 21, minutes: 55, seconds: 0)
        Date date2 = new Date(year: 2018, month: 6, date: 1, hours: 21, minutes: 45, seconds: 0)
        Date date3 = new Date(year: 2018, month: 6, date: 2, hours: 21, minutes: 45, seconds: 0)

        List<Date> results = DateUtil.getUniqueDates([date1, date2, date3])

        assert results.size() == 2
        assert results.contains(date3)
        assert (results.contains(date1) || results.contains(date2))
    }

    private DateTime createByHour(def hour) {
        new DateTime(2012, 1, 1, hour, 0)

    }

    void testBeginningOfYear() {
        DateTime dt = new DateTime("2017-08-31")
        DateTime result = dateUtil.beginningOfYear(dt)

        assert result.dayOfMonth == 1
        assert result.dayOfYear == 1
        assert result.year == 2017
        assert result.toString(DateUtil.DEFAULT_DATE_FORMAT) == "2017-01-01"
    }

    @Test
    void testGetDateOfBirth() {
        Date date = new DateTime("1993-05-21").toDate()

        assert date == DateUtil.getDateOfBirth("19930521", "yyMMdd", "yyyyMMdd")
        assert date == DateUtil.getDateOfBirth("930521", "yyMMdd", "yyyyMMdd")

        assert date == DateUtil.getDateOfBirth("21051993", "ddMMyy", "ddMMyyyy")
        assert date == DateUtil.getDateOfBirth("210593", "ddMMyy", "ddMMyyyy")

        assert null == DateUtil.getDateOfBirth("932021", "yyMMdd", "yyyyMMdd")
        assert null == DateUtil.getDateOfBirth("19932021", "yyMMdd", "yyyyMMdd")
    }

    @Test
    void testNullCountryCodePersonalNumberSettings() {
        dateUtil.grailsApplication.config = [
                customer: [
                        personalNumber: [
                                settings: [:]
                        ]
                ]
        ]

        PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings(null)

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == PersonalNumberSettings.LONG_FORMAT_DEFAULT
        assert personalNumberSettings.shortFormat == PersonalNumberSettings.SHORT_FORMAT_DEFAULT
        assert personalNumberSettings.readableFormat == PersonalNumberSettings.READABLE_FORMAT_DEFAULT
        assert personalNumberSettings.securityNumberLength == PersonalNumberSettings.SECURITY_NUMBER_LENGTH_DEFAULT
        assert personalNumberSettings.orgPattern == PersonalNumberSettings.ORG_PATTERN_DEFAULT
    }

    @Test
    void testEmptyCountryCodePersonalNumberSettings() {
        dateUtil.grailsApplication.config = [
                customer: [
                        personalNumber: [
                                settings: [:]
                        ]
                ]
        ]

        PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings("")

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == PersonalNumberSettings.LONG_FORMAT_DEFAULT
        assert personalNumberSettings.shortFormat == PersonalNumberSettings.SHORT_FORMAT_DEFAULT
        assert personalNumberSettings.readableFormat == PersonalNumberSettings.READABLE_FORMAT_DEFAULT
        assert personalNumberSettings.securityNumberLength == PersonalNumberSettings.SECURITY_NUMBER_LENGTH_DEFAULT
        assert personalNumberSettings.orgPattern == PersonalNumberSettings.ORG_PATTERN_DEFAULT

        // Accept whatever
        assert ("1234korv" =~ personalNumberSettings.orgPattern).matches()
    }

    @Test
    void testPersonalNumberSettingsComplete() {
        dateUtil.grailsApplication.config = [
                customer: [
                        personalNumber: [
                                settings: [
                                        SE: [
                                                securityNumberLength: 4,
                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                longFormat: "yyyyMMdd",
                                                shortFormat: "yyMMdd",
                                                readableFormat: "yymmdd"
                                        ],
                                        PL: [
                                                securityNumberLength: 0,
                                                orgPattern: /^.*$/,
                                                longFormat: "ddMMyyyy",
                                                shortFormat: "ddMMyy",
                                                readableFormat: "yymmdd"
                                        ]
                                ]
                        ]
                ]
        ]

        PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings("SE")

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == "yyyyMMdd"
        assert personalNumberSettings.shortFormat == "yyMMdd"
        assert personalNumberSettings.readableFormat == "yymmdd"
        assert personalNumberSettings.securityNumberLength == 4
        assert personalNumberSettings.orgPattern == /^(\d{6}|\d{8})(?:-(\d{4}))?$/

        personalNumberSettings = dateUtil.getPersonalNumberSettings("PL")

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == "ddMMyyyy"
        assert personalNumberSettings.shortFormat == "ddMMyy"
        assert personalNumberSettings.readableFormat == "yymmdd"
        assert personalNumberSettings.securityNumberLength == 0
        assert personalNumberSettings.orgPattern == /^.*$/
    }

    @Test
    void testPersonalNumberSettingsInComplete() {
        dateUtil.grailsApplication.config = [
                customer: [
                        personalNumber: [
                                settings: [
                                        SE: [
                                                securityNumberLength: 4,
                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                        ],
                                        PL: [
                                                longFormat: "ddMMyyyy",
                                                shortFormat: "ddMMyy",
                                                readableFormat: "yymmdd"
                                        ]
                                ]
                        ]
                ]
        ]

        PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings("SE")

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == PersonalNumberSettings.LONG_FORMAT_DEFAULT
        assert personalNumberSettings.shortFormat == PersonalNumberSettings.SHORT_FORMAT_DEFAULT
        assert personalNumberSettings.readableFormat == PersonalNumberSettings.READABLE_FORMAT_DEFAULT
        assert personalNumberSettings.securityNumberLength == 4
        assert personalNumberSettings.orgPattern == /^(\d{6}|\d{8})(?:-(\d{4}))?$/

        personalNumberSettings = dateUtil.getPersonalNumberSettings("PL")

        assert personalNumberSettings
        assert personalNumberSettings.longFormat == "ddMMyyyy"
        assert personalNumberSettings.shortFormat == "ddMMyy"
        assert personalNumberSettings.readableFormat == "yymmdd"
        assert personalNumberSettings.securityNumberLength == PersonalNumberSettings.SECURITY_NUMBER_LENGTH_DEFAULT
        assert personalNumberSettings.orgPattern == PersonalNumberSettings.ORG_PATTERN_DEFAULT
    }

    @Test
    void testPersonalNumberSettingsLuhnValidation() {
        dateUtil.grailsApplication.config = [
                customer: [
                        personalNumber: [
                                settings: [
                                        SE: [
                                                securityNumberLength: 4,
                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                longFormat: "yyyyMMdd",
                                                shortFormat: "yyMMdd",
                                                readableFormat: "yymmdd"
                                        ],
                                        NO: [
                                                securityNumberLength: 4,
                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                longFormat: "yyyyMMdd",
                                                shortFormat: "yyMMdd",
                                                readableFormat: "yymmdd",
                                                skipLuhnValidation: false
                                        ],
                                        DK: [
                                                securityNumberLength: 4,
                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                longFormat: "ddMMyyyy",
                                                shortFormat: "ddMMyy",
                                                readableFormat: "yymmdd",
                                                skipLuhnValidation: true
                                        ]
                                ]
                        ]
                ]
        ]

        PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings("SE")
        assert personalNumberSettings
        assert !personalNumberSettings.skipLuhnValidation

        personalNumberSettings = dateUtil.getPersonalNumberSettings("NO")
        assert personalNumberSettings
        assert !personalNumberSettings.skipLuhnValidation

        personalNumberSettings = dateUtil.getPersonalNumberSettings("DK")
        assert personalNumberSettings
        assert personalNumberSettings.skipLuhnValidation
    }

    @Test
    void testGetLocalTimeMidnight() {
        LocalTime midnight = dateUtil.getLocalTimeMidnight()

        assert midnight
        assert midnight.getMillisOfDay() == 0
        assert midnight.getMillisOfSecond() == 0
        assert midnight.getMinuteOfHour() == 0
        assert midnight.getHourOfDay() == 0
    }

    @Test
    void testIsMidnight() {
        assert !dateUtil.isMidnight(null)
        assert dateUtil.isMidnight(dateUtil.getLocalTimeMidnight())

        LocalTime anotherMidnight = new LocalTime().withMillisOfSecond(0).withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0)

        assert dateUtil.isMidnight(anotherMidnight)
        assert anotherMidnight.equals(dateUtil.getLocalTimeMidnight())
        assert dateUtil.getLocalTimeMidnight().equals(anotherMidnight)
        assert anotherMidnight.getMillisOfDay() == 0

        assert !dateUtil.isMidnight(anotherMidnight.withHourOfDay(1))
        assert !dateUtil.isMidnight(anotherMidnight.withMinuteOfHour(1))
        assert !dateUtil.isMidnight(anotherMidnight.withSecondOfMinute(1))
        assert !dateUtil.isMidnight(anotherMidnight.withMillisOfSecond(1))
    }

    void testHourOfDayExist() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
        Date date = formatter.parseDateTime("2020-03-29 02:00:00 +0200").toDate()

        int oneHourSkipped;
        int hourCounted;
        for (int i=0; i< 24; i++){
            if (DateUtil.hourOfDayExist(date, i)) {
                hourCounted++
            } else {
                oneHourSkipped++
            }
        }

        assert hourCounted >= 23
        assert oneHourSkipped <= 1
    }
}
