package com.matchi

import org.joda.time.DateTime

class BookingGroupFrequencyHandler {
    def WEEKLY     = [ val: 1, type: "WEEKLY" ]
    def MONTHLY    = [ val: 2, type: "MONTHLY" ]
    def YEARLY     = [ val: 3, type: "YEARLY" ]

    def getFrequency(int val) {
        return getFrequencies().collect { it.val == val}[0]
    }

    def getFrequencies() {
        return [ WEEKLY ]
    }

    def getNextOccurence( DateTime date, int frequency, int interval ) {
        switch (frequency) {
            case(1):
                return date.plusWeeks(interval)
            case(2):
                return date.plusMonths(interval)
            case(3):
                return date.plusYears(interval)
            default:
                return
        }
    }
}
