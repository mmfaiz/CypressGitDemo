package com.matchi

import com.matchi.schedule.TimeSpan
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.IllegalFieldValueException
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import org.joda.time.ReadableDateTime
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

import java.text.ParseException
import java.text.SimpleDateFormat

class DateUtil {

	public static final String DEFAULT_DATE_AND_TIME_FORMAT = "yyyy-MM-dd HH:mm"
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
	public static final String DEFAULT_DATE_SHORT_FORMAT = "yyMMdd"
	public static final String DEFAULT_TIME_FORMAT = "HH:mm"
	public static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
	public static final String ISO8601_DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

	public static final int DAYS_PER_WEEK = 7
	public static final int SHORT_PERSONAL_NUMBER_LENGTH = 6
	public static final int LAST_CENTURY_PREFIX = 19

    public static final int MILLISECONDS_PER_HOUR = 60 * 60 * 1000

	def messageSource
	def grailsApplication

    static int getCurrentYear() {
		return new DateTime().getYear()
	}

    /**
	 * Evens out the hour on a Date
	 * 2011-05-01 15:23:23.234 => 2011-05-01 15:00:00 000 
	 * @param date
	 * @return A date object with minutes, seconds and millis set to zero
	 */
	public Date evenHour(Date date) {
		return new DateTime(date)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
            .withMillisOfSecond(0).toDate()
	}
	
	public Date evenHour(Calendar date) {
		return evenHour(date.getTime())
	}
	
	/**
	 * Formats a date to given standard
	 * @param date The date to be formatted
	 * @return A formatted string
	 */
	public String formatDate(Date date) {
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(date)
	}

	public String formatDateAndTime(Date date) {
		return new SimpleDateFormat(DEFAULT_DATE_AND_TIME_FORMAT).format(date)
	}

	public String formatDateTime(Date date) {
		return new SimpleDateFormat(DATE_AND_TIME_FORMAT).format(date)
	}

	public Date parseDate(String date) {
		return new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(date)
	}

	public Date parseDateAndTime(String date) {
		return new SimpleDateFormat(DATE_AND_TIME_FORMAT).parse(date)
	}
	
	/**
	 * Compiles a list of Dates between to dates:
	 * input 2011-01-01, 2011-01-04 will results in a list:
	 * [2011-01-01, 2011-01-02, 2011-01-03, 2011-01-04]
	 * 
	 * @param fromDate (inclusive)
	 * @param toDate (inclusive)
	 * @return A list of dates
	 */
	public List<DateTime> createDateIntervals(ReadableDateTime fromDate, ReadableDateTime toDate) {
		
		def results = []
		DateMidnight startDate = new DateMidnight(fromDate)
		DateMidnight endDate = new DateMidnight(toDate)
		
		for (DateMidnight date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1)) {
			results.add date
		}
		
		return results
	}
	
	/**
	 * Compiles a list of hourly TimeSpans 
	 * 
	 * @param theDate The date of which to compile
	 * @param startHour Start hour of day
	 * @param endHour End hour of day
	 * @param spanStartMinute Start minute
	 * @param spanEndMinute End minute
	 * @return A list of TimeSpan
	 */
	public List<TimeSpan> createTimeSpans(DateTime theDate, int startHour, int endHour) {
		def results = []
		
		DateTime startTime = new DateTime(theDate).withTime(startHour, 0,0,0)
		DateTime endTime = new DateTime(theDate).withTime(endHour, 0,0,0)

        if(endHour == 0) {
            // allow timespans to midnight ("0")
            endTime = endTime.plusDays(1)
        }

		
		for (DateTime date = startTime; (date.isBefore(endTime) || date.equals(endTime)); date = date.plusHours(1)) {
			DateTime spanStart = new DateTime(date).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)

            DateTime spanEnd = spanStart.plusHours(1)
			def span =  new TimeSpan(spanStart, spanEnd)
			results.add span
		}
		
		return results
	}
	
	/**
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public List<DateMidnight> composeMonthlyCalendar(int year, int month) {
		List<Calendar> results = new ArrayList<Calendar>()
		
		DateMidnight startDate = new DateMidnight()
			.withYear(year)
			.withMonthOfYear(month)
			.withDayOfMonth(1)
			.withDayOfWeek(1)
			
		DateMidnight endDate = new DateMidnight()
			.withYear(year)
			.withMonthOfYear(month)
			.dayOfMonth()
			.withMaximumValue()
			.dayOfWeek()
			.withMaximumValue()
			
		for (DateMidnight date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1)) {
			results.add date
		}
		
		return results;
	}

    /**
     *
     * @param year
     * @param week
     * @return
     */
    public List<DateMidnight> composeWeeklyCalendar(int year, int week) {
        List<Calendar> results = new ArrayList<Calendar>()

        DateMidnight startDate = new DateMidnight()
                .withWeekyear(year)
                .withWeekOfWeekyear(week)
                .withDayOfWeek(DateTimeConstants.MONDAY)

        DateMidnight endDate = startDate.plusDays(6)


        for (DateMidnight date = startDate; date.isBefore(endDate) || date.isEqual(endDate); date = date.plusDays(1)) {
            results.add date
        }

        return results;
    }

    /**
     * Composes user birthdate from form parameters
     * @param year Year, yyyy
     * @param month Month, MM
     * @param day Day, dd
     * @return Date instance
     */
    public Date composeBirthDate(int year, String month, int day) {
        def cal = Calendar.getInstance()

        Date date = new SimpleDateFormat("MMMM").parse(month);
        cal.setTime(date)

        def calMonth = cal.get(Calendar.MONTH)

        return parseDate(year + "-" + (calMonth + 1) + "-" + day)
    }

	public DateTime beginningOfYear(Date date) {
		return beginningOfYear(new DateTime(date))
	}

	public DateTime beginningOfYear(DateTime dateTime) {
		return beginningOfDay(dateTime.withMonthOfYear(1).withDayOfMonth(1))
	}

    public DateTime beginningOfDay(Date date) {
        return beginningOfDay(new DateTime(date))
    }

    public DateTime endOfDay(Date date) {
        return endOfDay(new DateTime(date))
    }

    public DateTime beginningOfDay(DateTime date) {
        return date.withTimeAtStartOfDay()
    }

    public DateTime endOfDay(DateTime date) {
        return date.withTime(23, 59, 59, 999)
    }

	public LocalTime getLocalTimeMidnight() {
		return new LocalTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
	}

    public String format(Period period) {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroIfSupported()
                .appendHours()
                .appendSeparator(":")
                .minimumPrintedDigits(2)
                .printZeroIfSupported()
                .appendMinutes()
                .toFormatter()
        return formatter.print(period)
    }

    /**
     * Returns new DateTime object with the day set to the first
     * @param date
     * @return
     */
    public DateTime toBeginningOfMonth(DateTime date) {
        return date.dayOfMonth().withMinimumValue()
    }

	/**
	 * Returns new DateTime object with the day set to the last
	 * @param date
	 * @return
	 */
	public DateTime toEndOfMonth(DateTime date) {
		return date.dayOfMonth().withMaximumValue()
	}

    /**
     * Method that flattens (unions) all intervals in a list.
     *
     * Ex:
     *  [1-6, 8-10] == flatten([1-5, 2-6, 8-10])
     *
     * @param List of intervals
     * @return A flatten list of intervals
     */
    public List<Interval> flatten(List<Interval> intervals) {

        // we need sorted interval before we start
        // interval are sorted by interval.start (asc), interval.duration (asc)
        intervals = sort(intervals.unique())

        for(int i = 0 ; i < intervals.size() ; i++) {
            def current = intervals.get(i)

            for(int j = i+1 ; j < intervals.size() ; j++) {

                def next = intervals.get(j)

                // If next interval is after, break and move to next
                if (next.start.isAfter(current.end)) {
                    break;
                }

                if (next.overlap(current) || next.abuts(current)) {

                    // If the next interval overlaps or abuts the current interval
                    // merge the intervals.
                    if (next.end.isAfter(current.end)) {
                        current = new Interval(current.start, next.end)
                        intervals.set(i, current)
                    }
                    // remove the old interval and back one
                    intervals.remove(j)
                    j--;

                }
            }
        }

        return intervals
    }

    /**
     * Sorts a list of intervals by:
     *  1) Start time
     *  2) Duration (ascending)
     * @param intervals
     * @return
     */
    public List<Interval> sort(List<Interval> intervals) {
        def comparator = [
                compare: { Interval a, Interval b ->
                    int start = a.start.compareTo(b.start)
                    if (start != 0) return start
                    a.toDuration().compareTo(b.toDuration())
                }
        ] as Comparator
        return intervals.sort(false, comparator)
    }

    static Integer parse2DigitBirthYear(String value) {
        parse2DigitYearDate(value)[Calendar.YEAR]
    }

    static Date parse2DigitYearDate(String value) {
        def cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -100)
        def sdf = new SimpleDateFormat("yyMMdd")
        sdf.set2DigitYearStart(cal.getTime())
        sdf.parse(value)
    }

	Date parseDateOfBirth(Facility facility, String dob) {
		PersonalNumberSettings personalNumberSettings = getPersonalNumberSettings(facility.country)
		String dateFormat = dob.size() == SHORT_PERSONAL_NUMBER_LENGTH ? personalNumberSettings.shortFormat : personalNumberSettings.longFormat

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat)
		simpleDateFormat.lenient = false

		try {
			LocalDate date = new LocalDate(simpleDateFormat.parse(dob))
			if(date.isAfter(new LocalDate())) return date.withCenturyOfEra(LAST_CENTURY_PREFIX).toDate()

			return date.toDate()
		} catch (ParseException e) {
			return null
		}
	}

	/**
	 * Method to return different useable formats for personal numbers
	 * @param countryCode
	 * @return
	 */
	PersonalNumberSettings getPersonalNumberSettings(String countryCode) {
		Map settings = grailsApplication.config.customer.personalNumber.settings
		return PersonalNumberSettings.fromMap(settings[countryCode] as Map)
	}

	/**
	 * Returns unique dates based on date printed out as YYYY-MM-DD
	 * @param dateObjects
	 * @return
	 */
	static List<Date> getUniqueDates(List<Date> dateObjects) {
		return dateObjects.unique { Date date ->
			return date.format(DEFAULT_DATE_FORMAT)
		}
	}

	static Date getDateOfBirth(String date, String shortFormat, String format){
		Date dateOfBirth
		SimpleDateFormat dateFormat = new SimpleDateFormat(date.size() == 6 ? shortFormat : format)
		dateFormat.lenient = false
		try {
			dateOfBirth = dateFormat.parse(date)
		} catch (ParseException e) {
			dateOfBirth = null
		}
		return dateOfBirth
	}

	/**
	 * Checks if LocalTime object is midnight, exactly 00:00:00.000
	 * @param localTime
	 * @return
	 */
	boolean isMidnight(LocalTime localTime) {
		if(localTime == null) {
			return false
		}

		return localTime.equals(getLocalTimeMidnight())
	}

	static boolean hourOfDayExist(Date date, int hour) {
		try {
			new DateTime(date).withHourOfDay(hour)
			return true
		} catch(IllegalFieldValueException ifve) {
			return false
		}
	}
}