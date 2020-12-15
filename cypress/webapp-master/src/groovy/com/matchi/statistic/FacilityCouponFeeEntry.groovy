package com.matchi.statistic

import com.matchi.Facility

/**
 * @author Sergei Shushkevich
 */
class FacilityCouponFeeEntry {

    Facility facility
    String type
    BigDecimal fee = 0
    String feeType
    Long count = 0

    BigDecimal getTotalFee() {
        fee * count
    }
}
