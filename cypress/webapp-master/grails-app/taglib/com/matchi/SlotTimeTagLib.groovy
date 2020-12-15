package com.matchi

import org.joda.time.DateTime
import org.joda.time.LocalDate
import com.matchi.activities.ActivityOccasion

class SlotTimeTagLib {

    def slotFullTime = { attrs, body ->
		if(attrs.slot != null) {
            StringBuilder sb = new StringBuilder()
            sb.append(new DateTime(attrs.slot.startTime).toString("yyyy-MM-dd"))
            sb.append(" ")
            sb.append(new DateTime(attrs.slot.startTime).toString("HH:mm"))
            sb.append("-")
            sb.append(new DateTime(attrs.slot.endTime).toString("HH:mm"))
			out << sb.toString()
		}
	}

    def slotCourtAndTime = { attrs, body ->
		if(attrs.slot != null) {
            StringBuilder sb = new StringBuilder()

            def startTime = new DateTime(attrs.slot.startTime)
            def endTime = new DateTime(attrs.slot.endTime)

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
            sb.append(startTime.toString("HH:mm"))
            sb.append("-")
            sb.append(endTime.toString("HH:mm"))
            sb.append(" ")
            sb.append(attrs.slot.court.name.encodeAsHTML())
			out << sb.toString()
		}
	}

    def occasionTime = { attrs, body ->
        if(attrs.occasion != null) {
            StringBuilder sb = new StringBuilder()
            ActivityOccasion occasion = attrs.occasion

            def dateTime = occasion.date.toDateTime(occasion.startTime)

            def code = "time.weekDay." + dateTime.getDayOfWeek()

            if (dateTime.toLocalDate().equals(new LocalDate())) {
                sb.append(message(code: "default.dateRangePicker.today"))
            } else if (dateTime.toLocalDate().equals(new LocalDate().plusDays(1))) {
                sb.append(message(code: "default.dateRangePicker.tomorrow"))
            } else {
                sb.append(g.message(code: code))
            }

            sb.append(" (")
            sb.append(dateTime.toString("d/M"))
            sb.append(") ")
            sb.append(dateTime.toString("HH:mm"))

            out << sb.toString()
        }
    }
}
