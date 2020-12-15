package com.matchi

import org.joda.time.DateTime

class UserActivityService {

    static transactional = false
    def final static YEARSPAN = 1
    def final static MONTHSPAN = 12
    def final static WEEKSPAN  = 5

    def groovySql

    def userActivityMonthly(User user) {
        def userActivity = getUserBookingsCountGroupedByMonth(user)
        def activity = []

        def yearBack  = new DateTime().minusYears(YEARSPAN).getMonthOfYear()

        (yearBack..(yearBack + MONTHSPAN)).each {
            def realMonth = it > 12 ? it - 12 : it
            def monthActivity = userActivity.findAll() { it.dateUnit == realMonth }

            activity << [date: "", dateUnit: realMonth, count: monthActivity[0]?.count]
        }


        return activity
    }

    def userActivityWeekly(User user) {
        def userActivity = getUserBookingsCountGroupedByWeek(user)
        def activity = []

        def monthBack  = new DateTime().minusWeeks(WEEKSPAN).getWeekOfWeekyear()

        (monthBack..(monthBack + WEEKSPAN)).each {
            def realWeek = it > 52 ? it - 52 : it
            def weekActivity = userActivity.findAll() { it.dateUnit == realWeek }

            def weekDate = weekActivity ? weekActivity[0]?.date : new DateTime().withWeekOfWeekyear(realWeek).toString("dd/MM")

            activity << [date: weekDate, dateUnit: realWeek, count: weekActivity[0]?.count]
        }


        return activity
    }

    def getUserBookingsCountGroupedByMonth(User user) {
        def queryParams = [
                userId: user.id,
                monthSpan: MONTHSPAN
        ]

        def query = """
            SELECT  DATE_FORMAT(s.start_time,'%d/%m') as date,
                    MONTH(s.start_time) as dateUnit,
                    count(s.id) as count
            FROM booking b
        	    LEFT JOIN slot s on b.slot_id = s.id
        	    LEFT JOIN customer c on c.id = b.customer_id
        	    LEFT JOIN user u on u.id = c.user_id
            WHERE s.start_time > DATE_SUB(curdate(), INTERVAL :monthSpan MONTH) and u.id = :userId
            GROUP BY MONTH(s.start_time) ORDER BY s.start_time asc
        """

        return groovySql.rows(query, queryParams)
    }

    def getUserBookingsCountGroupedByWeek(User user) {
        def queryParams = [
                userId: user.id,
                weekSpan: WEEKSPAN
        ]

        def query = """
                SELECT  DATE_FORMAT(s.start_time,'%d/%m') as date,
                        WEEK(s.start_time) as dateUnit,
                        count(s.id) as count
                FROM booking b
            	    LEFT JOIN slot s on b.slot_id = s.id
            	    LEFT JOIN customer c on c.id = b.customer_id
            	    LEFT JOIN user u on u.id = c.user_id
                WHERE s.start_time > DATE_SUB(curdate(), INTERVAL :weekSpan WEEK) and u.id = :userId
                GROUP BY WEEK(s.start_time) ORDER BY s.start_time asc
            """

        return groovySql.rows(query, queryParams)
    }
}
