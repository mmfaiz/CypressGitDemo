package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils

@TestFor(MatchiConfigMethodAvailability)
class MatchiConfigMethodAvailabilityTests {
    void testMatchiConfigMethodAvailabilityTests() {
        MatchiConfigMethodAvailability matchiConfigMethodAvailability = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_DEVIATION)

        assert matchiConfigMethodAvailability.keyName == MatchiConfigKey.DISABLE_DEVIATION.key
        assert MatchiConfigMethodAvailability.simpleName == MatchiConfigKey.DISABLE_DEVIATION.clazz

        matchiConfigMethodAvailability.value = MatchiConfigMethodAvailability.NO
        assert !matchiConfigMethodAvailability.isBlocked()

        matchiConfigMethodAvailability.value = MatchiConfigMethodAvailability.ALWAYS
        assert matchiConfigMethodAvailability.isBlocked()

        DateTime today14 = new DateTime().withHourOfDay(14)
        DateTimeUtils.setCurrentMillisFixed(today14.millis)

        matchiConfigMethodAvailability.value = MatchiConfigMethodAvailability.OFFICE_HOURS
        assert matchiConfigMethodAvailability.isBlocked()

        DateTime today20 = new DateTime().withHourOfDay(19)
        DateTimeUtils.setCurrentMillisFixed(today20.millis)

        matchiConfigMethodAvailability.value = MatchiConfigMethodAvailability.OFFICE_HOURS
        assert !matchiConfigMethodAvailability.isBlocked()

        //reset back to the current time
        DateTimeUtils.setCurrentMillisSystem()
    }

}
