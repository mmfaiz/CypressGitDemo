package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot
import org.joda.time.DateTime

class WeekDayPriceCondition extends BookingPriceCondition {

    String weekDaysData

    static transients = [ "weekDays" ]

    void setWeekDays(def weekDaysList) {
        if(weekDaysList)
            weekDaysData = weekDaysList.join(',')
    }

    def getWeekDays() {
        return weekDaysData?.split(',')
    }

    static constraints = {
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        DateTime slotTime = new DateTime(slot.startTime)
        def accept = false

        getWeekDays().each {
            if(it.equals(String.valueOf(slotTime.dayOfWeek))) {
                accept = true
            }
        }
        return accept
    }
}
