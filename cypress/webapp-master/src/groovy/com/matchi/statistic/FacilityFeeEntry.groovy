package com.matchi.statistic

class FacilityFeeEntry {

    String type
    Integer count
    BigDecimal price

    BigDecimal getTotal() {
        count * price
    }

}
