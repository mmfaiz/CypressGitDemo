package com.matchi.payment

import org.joda.time.DateTime


class BookingRestrictionException extends InvalidPriceException {

    final DateTime validFrom
    final String profiles

    private BookingRestrictionException() {

    }

    BookingRestrictionException(DateTime validFrom, String profiles) {
        this.validFrom = validFrom
        this.profiles = profiles
    }

}