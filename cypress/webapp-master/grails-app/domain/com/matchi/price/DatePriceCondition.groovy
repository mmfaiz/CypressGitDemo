package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot
import org.joda.time.DateTime

class DatePriceCondition extends BookingPriceCondition {

    Date startDate
    Date endDate

    static constraints = {
        startDate(nullable: false)
        endDate(nullable: false)
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        DateTime slotTime = new DateTime(slot.startTime)
        DateTime start = new DateTime(startDate.clearTime())
        DateTime end = new DateTime(endDate.clearTime() + 1)

        return start.isBefore(slotTime) && end.isAfter(slotTime)
    }
}
