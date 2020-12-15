package com.matchi.price

import com.matchi.Customer
import com.matchi.Slot
import org.joda.time.DateTime
import org.joda.time.LocalTime

/**
 * In a Pricelist I want to be able to add a condition that is acceptable for "time left until booking starts"
 * (main reason: junior players should be able to book for free 0 min or 5 min or 1h before the time starts)
 *
 * The condition has 2 attributes, hours and minutes meaning how long before the actual slot start-time the Condition
 * should be accepted.
 *
 * @author Michael Astreiko
 */
class TimeBeforeBookingCondition extends BookingPriceCondition {
    Integer hours
    Integer minutes

    static constraints = {
        minutes(max: 60, nullable: true)
        hours(nullable: true, validator: { hours, obj ->
            if (!hours && !obj.minutes) {
                return ['timeBeforeBooking.condition.anythingShouldBeSpecified']
            }
        })
    }

    /**
     * To be accepted slot start time should be in future and current date should be in range
     * from (slot start time - condition time)
     * to slot start time.
     *
     * @param slot to check
     * @param customer
     * @return true if meets condition described above
     */
    @Override
    boolean accept(Slot slot, Customer customer) {
        new DateTime(slot.startTime).minusHours(hours ?: 0).minusMinutes(minutes ?: 0).beforeNow
    }
}
