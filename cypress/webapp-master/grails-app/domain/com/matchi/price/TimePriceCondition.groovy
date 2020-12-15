package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot
import org.joda.time.DateTime
import org.joda.time.LocalTime

class TimePriceCondition extends BookingPriceCondition {

    int fromHour
    int fromMinute
    int toHour
    int toMinute

    public def formattedFrom() {
        return  new LocalTime(fromHour, fromMinute).toString("HH:mm")
    }

    public def formattedTo() {
        return new LocalTime(toHour, toMinute).toString("HH:mm")
    }

    static constraints = {
        fromHour(validator: { fromHour, obj ->
			new LocalTime(fromHour, obj.properties['fromMinute'])
            .isBefore(new LocalTime(obj.properties['toHour'], obj.properties['toMinute'])) ? true : ['invalid.timemismatch']
		})
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        DateTime from = new LocalTime(fromHour, fromMinute).toDateTimeToday()
        DateTime to = new LocalTime(toHour, toMinute).toDateTimeToday()

        LocalTime slotLocalTime = new DateTime(slot.startTime).toLocalTime()
        DateTime slotTime = slotLocalTime.toDateTimeToday()

        def result = (from.isBefore(slotTime) || from.isEqual(slotTime)) && (to.isAfter(slotTime))

        return result
    }
}
