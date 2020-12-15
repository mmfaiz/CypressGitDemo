import org.joda.time.LocalDate

databaseChangeLog = {
    changeSet(author: "sergei (generated)", id: "1537882284811-2") {
        addColumn(tableName: "membership") {
            column(name: "cancel", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-3") {
        addColumn(tableName: "membership") {
            column(name: "end_date", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-4") {
        addColumn(tableName: "membership") {
            column(name: "start_date", type: "date")
        }
    }

    changeSet(author: "sergei", id: "1537882284811-5") {
        sql("update membership set start_date = date(date_created)")
    }

    changeSet(author: "sergei", id: "1537882284811-6") {
        addNotNullConstraint(columnDataType: "date", columnName: "start_date", tableName: "membership")
    }

    changeSet(author: "sergei", id: "1537882284811-7") {
        sql("update membership set cancel = true where status = 'CANCEL'")
    }

    changeSet(author: "sergei", id: "1537882284811-8") {
        grailsChange {
            change {
                sql.eachRow("select * from facility where membership_end_date is not null") { f ->
                    def endDate = new LocalDate(f.membership_end_date).withYear(new LocalDate().getYear())
                    if (endDate < new LocalDate()) {
                        endDate = endDate.plusYears(1)
                    }
                    sql.executeUpdate("update membership m join customer c on m.customer_id = c.id join facility f on c.facility_id = f.id set m.end_date = ? where f.id = ?",
                            [endDate.toDate(), f.id])
                }
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-9") {
        addColumn(tableName: "membership_type") {
            column(name: "valid_time_amount", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-10") {
        addColumn(tableName: "membership_type") {
            column(name: "valid_time_unit", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1537882284811-11") {
        sql("update membership set end_date = date_format(now(),'%Y-12-31') where end_date is null")
    }

    changeSet(author: "sergei", id: "1537882284811-12") {
        addNotNullConstraint(columnDataType: "date", columnName: "end_date", tableName: "membership")
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-13") {
        addColumn(tableName: "facility") {
            column(name: "membership_valid_time_amount", type: "integer", defaultValueNumeric: 1) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-14") {
        addColumn(tableName: "facility") {
            column(name: "membership_valid_time_unit", type: "varchar(255)", defaultValue: "YEAR") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-15") {
        addColumn(tableName: "facility") {
            column(name: "membership_grace_nr_of_days", type: "integer", defaultValueNumeric: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-16") {
        dropUniqueConstraint(constraintName: "customer_id_uniq_1407840036982", tableName: "membership")
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-17") {
        addColumn(tableName: "membership") {
            column(name: "grace_period_end_date", type: "date")
        }
    }

    changeSet(author: "sergei", id: "1537882284811-18") {
        sql("update membership set grace_period_end_date = end_date")
    }

    changeSet(author: "sergei", id: "1537882284811-19") {
        addNotNullConstraint(columnDataType: "date", columnName: "grace_period_end_date", tableName: "membership")
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-21") {
        addColumn(tableName: "facility") {
            column(name: "yearly_membership_purchase_days_in_advance", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-22") {
        addColumn(tableName: "facility") {
            column(name: "yearly_membership_start_date", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-23") {
        addColumn(tableName: "membership_type") {
            column(name: "purchase_days_in_advance_yearly", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-24") {
        addColumn(tableName: "membership_type") {
            column(name: "start_date_yearly", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-25") {
        createIndex(indexName: "end_date_idx", tableName: "membership") {
            column(name: "end_date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-26") {
        addColumn(tableName: "membership") {
            column(name: "activated", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1537882284811-27") {
        sql("update membership set activated = false where status = 'PENDING' or status = 'NOT_ACTIVE' or status = 'CANCELLED'")
    }

    changeSet(author: "sergei", id: "1537882284811-20") {
        grailsChange {
            change {
                println "Updating invoiced memberships..."
                sql.eachRow("select * from membership where invoice_row_id is not null and order_id is not null") { mrow ->
                    sql.eachRow("""select * from order_payment op
                            join order_order_payments oop on oop.payment_id = op.id
                            where oop.order_id = ? and op.status = 'CAPTURED'""", [mrow.order_id]) { oprow ->
                        sql.executeUpdate("update order_payment set status = 'NEW' where id = ?", [oprow.id])
                    }
                    sql.executeUpdate("update `order` set status = 'NEW' where id = ?", [mrow.order_id])

                    def currentEndDate = new LocalDate(mrow.end_date)
                    def start = currentEndDate.plusDays(1).toDate()
                    def end = currentEndDate.plusYears(1).toDate()
                    def newMembershipId = sql.executeInsert("""insert into membership
                            (version, customer_id, date_created, last_updated, status, type_id, created_by_id,
                                    order_id, cancel, activated, start_date, end_date, grace_period_end_date)
                            values (0, ?, now(), now(), 'ACTIVE', ?, ?, ?, ?, ?, ?, ?, ?)""",
                            [mrow.customer_id, mrow.type_id, mrow.created_by_id, mrow.order_id,
                                    false, true, start, end, end])[0][0]

                    sql.executeUpdate("update membership set invoice_row_id = null, order_id = null where id = ?", [mrow.id])

                    if (mrow.family_id) {
                        def contactId = sql.firstRow("select contact_id from membership_family where id = ?",
                                [mrow.family_id]).contact_id
                        if (contactId == mrow.customer_id) {
                            def familtyId = sql.executeInsert("""insert into membership_family
                                    (version, contact_id) values (0, ?)""", [mrow.customer_id])[0][0]
                            sql.executeUpdate("update membership set family_id = ? where id = ?",
                                    [familtyId, newMembershipId])

                            sql.eachRow("""select m.id, m.type_id, m.customer_id, m.end_date, c.facility_id, f.name
                                    from membership m left join customer c on c.id = m.customer_id
                                    left join facility f on f.id = c.facility_id
                                    where m.id != ? and m.family_id = ? and m.activated = ? and m.cancel = ?""",
                                    [mrow.id, mrow.family_id, true, false]) { fmrow ->

                                def issuerId = sql.firstRow("""select u.id from `user` u
                                        join user_role ur on ur.user_id = u.id
                                        join role r on r.id = ur.role_id
                                        where r.authority = 'ROLE_ADMIN' and u.enabled = true""").id
                                def price = 0
                                if (fmrow.type_id) {
                                    price = sql.firstRow("select price from membership_type where id = ?",
                                            [fmrow.type_id]).price
                                }
                                def status = price ? "NEW" : "COMPLETED"
                                currentEndDate = new LocalDate(fmrow.end_date)
                                start = currentEndDate.plusDays(1).toDate()
                                end = currentEndDate.plusYears(1).toDate()

                                def oid = sql.executeInsert("""insert into `order`
                                        (version, date_created, date_delivery, last_updated, article, customer_id,
                                                facility_id, description, issuer_id, origin, price, vat, status)
                                        values (0, now(), now(), now(), ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                                        ['MEMBERSHIP', fmrow.customer_id, fmrow.facility_id, fmrow.name ?: '',
                                                issuerId, 'facility', price, 0, status])[0][0]
                                sql.executeInsert("""insert into membership
                                        (version, customer_id, date_created, last_updated, status, type_id,
                                                created_by_id, order_id, cancel, activated, start_date,
                                                end_date, grace_period_end_date, family_id)
                                        values (0, ?, now(), now(), 'ACTIVE', ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                                        [fmrow.customer_id, fmrow.type_id, issuerId, oid,
                                                false, true, start, end, end, familtyId])
                            }
                        }
                    }
                }
                println "Updated"
            }
        }
    }

    changeSet(author: "sergei", id: "1537882284811-28") {
        grailsChange {
            change {
                println "Updating membership orders without payments..."
                sql.eachRow("""select o.id, o.price, o.issuer_id from membership m
                        join `order` o on m.order_id = o.id
                        left join order_order_payments oop on oop.order_id = o.id
                        where (m.status = 'ACTIVE' or m.status = 'CANCEL')
                        and o.status = 'COMPLETED' and o.price != ?
                        and oop.payment_id is null""", [0]) { row ->
                    def pid = sql.executeInsert("""insert into order_payment
                            (version, amount, date_created, last_updated, issuer_id, status, vat, type, credited, method)
                            values (0, ?, now(), now(), ?, ?, ?, ?, ?, ?)""",
                            [row.price, row.issuer_id, 'CAPTURED', 0, 'cash', 0, 'CASH'])[0][0]
                    sql.executeInsert("insert into order_order_payments (order_id, payment_id) values (?, ?)",
                            [row.id, pid])
                }
                println "Updated"
            }
        }
    }

    changeSet(author: "sergei", id: "1537882284811-29") {
        grailsChange {
            change {
                println "Updating memberships without orders..."
                def issuerId = sql.firstRow("""select u.id from `user` u
                        join user_role ur on ur.user_id = u.id
                        join role r on r.id = ur.role_id
                        where r.authority = 'ROLE_ADMIN' and u.enabled = true""").id
                sql.eachRow("""select m.id, m.customer_id, c.facility_id, f.name from membership m
                        left join customer c on c.id = m.customer_id
                        left join facility f on f.id = c.facility_id
                        where m.order_id is null""") { row ->
                    def oid = sql.executeInsert("""insert into `order`
                            (version, date_created, date_delivery, last_updated, article, customer_id,
                                    facility_id, description, issuer_id, origin, price, vat, status)
                            values (0, now(), now(), now(), ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                            ['MEMBERSHIP', row.customer_id, row.facility_id, row.name ?: '',
                                    issuerId, 'facility', 0, 0, 'COMPLETED'])[0][0]
                    sql.executeUpdate("update membership set order_id = ? where id = ?", [oid, row.id])
                }
                println "Updated"
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1537882284811-30") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "order_id", tableName: "membership")
    }

    changeSet(author: "sergei", id: "1537882284811-31") {
        sql("delete from QRTZ_CRON_TRIGGERS where TRIGGER_NAME = 'MembershipStatusUpdateJob.trigger'")
        sql("delete from QRTZ_TRIGGERS where JOB_NAME = 'com.matchi.jobs.MembershipStatusUpdateJob'")
        sql("delete from QRTZ_JOB_DETAILS where JOB_NAME = 'com.matchi.jobs.MembershipStatusUpdateJob'")
    }
}
