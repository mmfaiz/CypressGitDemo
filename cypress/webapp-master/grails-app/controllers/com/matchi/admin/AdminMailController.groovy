package com.matchi.admin


import org.joda.time.LocalDate

class AdminMailController {
    private static final String DATE_FORMAT = "yyyy-MM-dd"
    def groovySql

    def index() {
        LocalDate now = new LocalDate()
        String fromDate = now.minusDays(7).toString(DATE_FORMAT)
        String toDate = now.toString(DATE_FORMAT)

        log.info("Fetching sent and failed e-mails between ${fromDate} and ${toDate}")

        // Couldn't find a nice way to do the group by date only, so resorted to groovy sql.
        String sql = """select date(create_date) as created_date,
                               sum(status = 'CREATED') created,
                               sum(status = 'SENT') sent,
                               sum(status = 'ERROR') error
                        from async_mail_mess
                        where date(create_date) >= ?
                        and date(create_date) <= ?
                        group by date(create_date)
                        order by date(create_date) desc;"""

        def mailsByDate = []
        groovySql.rows(sql, [fromDate, toDate]).each {
            mailsByDate << [created_date: it.created_date, created: it.created, sent: it.sent, error: it.error]
        }
        groovySql.close()

        [ mailsByDate: mailsByDate ]
    }

    def resend() {
        String date = params.date
        if (date) {
            log.info("Resending e-mails for ${date}")
            LocalDate localDate = new LocalDate(date)

            String sql = """update async_mail_mess
                            set status = 'CREATED', attempts_count = 0
                            where status = 'ERROR'
                            and date(create_date) = ?;"""

            groovySql.executeUpdate(sql, [localDate.toString(DATE_FORMAT)])

            flash.message = message(code: "default.status.success")
        } else {
            flash.error = 'No date provided.'
        }
        groovySql.close()
        redirect(action: "index")
    }

}
