package com.matchi.membership

import org.joda.time.LocalDate

/**
 * @author Sergei Shushkevich
 */
enum TimeUnit {

    DAY({LocalDate date, Integer amount -> date.plusDays(amount)}),
    WEEK({LocalDate date, Integer amount -> date.plusWeeks(amount)}),
    MONTH({LocalDate date, Integer amount -> date.plusMonths(amount)}),
    YEAR({LocalDate date, Integer amount -> date.plusYears(amount)})

    final Closure addTime

    TimeUnit(Closure addTime) {
        this.addTime = addTime
    }

    static List<TimeUnit> availableUnits() {
        [YEAR, MONTH]
    }

    static List<TimeUnit> list() {
        [YEAR, MONTH, WEEK, DAY]
    }
}
