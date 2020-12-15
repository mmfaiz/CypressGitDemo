package com.matchi

import org.joda.time.DateTime
import org.joda.time.LocalDate

class JodaDateFormatTagLib {

    def dateUtil

	def shortWeekDay = { attrs, body ->
		if(attrs.date != null) {
			out << message(code:"time.shortWeekDay."+ attrs.date.getDayOfWeek())
		} 
	}
	
	def weekDay = { attrs, body ->
		if(attrs.date != null) {
			out << message(code:"time.weekDay."+ attrs.date.getDayOfWeek())
		}
	}

    def period = {  attrs, body ->
        if (attrs.period) {
            out << dateUtil.format(attrs.period)
        } else {
            if(attrs.default) {
                out << attrs.default
            }
        }

    }

    def humanDateFormat = { attrs, body ->

        StringBuilder sb = new StringBuilder()

        def startTime = attrs.date

        def code = "time.weekDay." + startTime.getDayOfWeek()

        if (startTime.toLocalDate().equals(new LocalDate())) {
            sb.append(message(code: "default.dateRangePicker.today"))
        } else if (startTime.toLocalDate().equals(new LocalDate().plusDays(1))) {
            sb.append(message(code: "default.dateRangePicker.tomorrow"))
        } else {
            sb.append(g.message(code: code))
        }


        sb.append(" (")
        sb.append(startTime.toString("d/M"))
        sb.append(") ")

        out << sb.toString()
    }

    def thisYear = { attrs, body ->
        out << new LocalDate().getYear()
    }
}
