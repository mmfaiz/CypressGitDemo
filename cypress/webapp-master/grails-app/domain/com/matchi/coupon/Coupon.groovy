package com.matchi.coupon

class Coupon extends Offer {

    public final static String MAPPING_VALUE = "coupon"

    static mapping = {
        discriminator MAPPING_VALUE
    }

    Integer nrOfPeriods
    Integer nrOfBookingsInPeriod
    // Nr of bookings per given period
    ConditionPeriod conditionPeriod
    boolean totalBookingsInPeriod = false


    static constraints = {
        nrOfPeriods(nullable: true)
        nrOfBookingsInPeriod(nullable: true)
        conditionPeriod(nullable: true)
        totalBookingsInPeriod(nullable: true)
    }

    public static enum ConditionPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY

        static list() {
            return [DAILY, WEEKLY, MONTHLY, YEARLY]
        }
    }

    String getOfferTypeString() {
        return 'coupon'
    }
}
