databaseChangeLog = {

    changeSet(author: "ami (generated)", id: "1404814501991-1") {
        createTable(tableName: "abstract_price_condition") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "customer_category_id", type: "BIGINT")

            column(name: "category_id", type: "BIGINT")

            column(name: "end_date", type: "DATETIME")

            column(name: "start_date", type: "DATETIME")

            column(name: "from_hour", type: "INT")

            column(name: "from_minute", type: "INT")

            column(name: "to_hour", type: "INT")

            column(name: "to_minute", type: "INT")

            column(name: "week_days_data", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-2") {
        createTable(tableName: "activity") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueBoolean: "false", name: "archived", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "headline", type: "VARCHAR(255)")

            column(name: "large_image_id", type: "BIGINT")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "teaser", type: "LONGTEXT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-3") {
        createTable(tableName: "activity_occasion") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "activity_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "DATE") {
                constraints(nullable: "false")
            }

            column(name: "end_time", type: "TIME") {
                constraints(nullable: "false")
            }

            column(name: "max_num_participants", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "VARCHAR(255)")

            column(name: "price", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "start_time", type: "TIME") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-4") {
        createTable(tableName: "activity_occasion_booking") {
            column(name: "activity_occasion_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-5") {
        createTable(tableName: "activity_slot_condition_activity") {
            column(name: "activity_slot_condition_activities_id", type: "BIGINT")

            column(name: "activity_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-6") {
        createTable(tableName: "async_mail_attachment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "attachment_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "LONGBLOB") {
                constraints(nullable: "false")
            }

            column(name: "inline", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mime_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "attachments_idx", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-7") {
        createTable(tableName: "async_mail_bcc") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "bcc_string", type: "LONGTEXT")

            column(name: "bcc_idx", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-8") {
        createTable(tableName: "async_mail_cc") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_string", type: "LONGTEXT")

            column(name: "cc_idx", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-9") {
        createTable(tableName: "async_mail_header") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "header_name", type: "VARCHAR(255)")

            column(name: "header_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-10") {
        createTable(tableName: "async_mail_mess") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "attempt_interval", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "attempts_count", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "begin_date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "create_date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "from_column", type: "LONGTEXT")

            column(name: "html", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "last_attempt_date", type: "DATETIME")

            column(name: "mark_delete", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "max_attempts_count", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "priority", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "reply_to", type: "LONGTEXT")

            column(name: "sent_date", type: "DATETIME")

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "subject", type: "LONGTEXT") {
                constraints(nullable: "false")
            }

            column(name: "text", type: "LONGTEXT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-11") {
        createTable(tableName: "async_mail_to") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "to_string", type: "LONGTEXT")

            column(name: "to_idx", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-12") {
        createTable(tableName: "availability") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "begin", type: "TIME") {
                constraints(nullable: "false")
            }

            column(name: "end", type: "TIME") {
                constraints(nullable: "false")
            }

            column(name: "weekday", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-13") {
        createTable(tableName: "booking") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_number", type: "VARCHAR(255)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "BIGINT")

            column(name: "group_id", type: "BIGINT")

            column(defaultValueBoolean: "false", name: "online", type: "BIT")

            column(name: "payment_id", type: "BIGINT")

            column(name: "paid", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "show_comment", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "slot_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "telephone", type: "VARCHAR(255)")

            column(name: "user_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-14") {
        createTable(tableName: "booking_export") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cancelled", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "data", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "filename", type: "VARCHAR(255)")

            column(name: "time_created", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-15") {
        createTable(tableName: "booking_group") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "comment", type: "VARCHAR(255)")

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-16") {
        createTable(tableName: "boxnet_transaction") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "betalsatt", type: "VARCHAR(255)")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "deb_pris", type: "VARCHAR(255)")

            column(name: "kassa", type: "VARCHAR(255)")

            column(name: "kassakod", type: "VARCHAR(255)")

            column(name: "kund_id", type: "VARCHAR(255)")

            column(name: "kvittonr", type: "VARCHAR(255)")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "momssats", type: "VARCHAR(255)")

            column(name: "produktnr", type: "VARCHAR(255)")

            column(name: "tid", type: "VARCHAR(255)")

            column(name: "titel", type: "VARCHAR(255)")

            column(name: "synced_date", type: "DATETIME")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-17") {
        createTable(tableName: "cash_register_transaction") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "method", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "paid_amount", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "receipt_number", type: "VARCHAR(255)")

            column(name: "title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "vat", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-18") {
        createTable(tableName: "contact_me") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "contacted", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)")

            column(name: "facility", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "phone", type: "VARCHAR(255)")

            column(name: "type", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-19") {
        createTable(tableName: "coupon") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "nr_of_tickets", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "nr_of_days_valid", type: "INT")

            column(defaultValueBoolean: "false", name: "available_online", type: "BIT")

            column(defaultValueBoolean: "false", name: "unlimited", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "condition_period", type: "VARCHAR(255)")

            column(name: "nr_of_periods", type: "INT")

            column(name: "nr_of_bookings_in_period", type: "INT")

            column(defaultValueBoolean: "false", name: "total_bookings_in_period", type: "BIT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-20") {
        createTable(tableName: "coupon_condition_group") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-21") {
        createTable(tableName: "coupon_condition_groups_slot_conditions_sets") {
            column(name: "coupon_condition_group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "slot_condition_set_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-22") {
        createTable(tableName: "court") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "indoor", type: "BIT")

            column(name: "members_only", type: "BIT")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "offline_only", type: "BIT")

            column(name: "sport_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surface", type: "VARCHAR(255)")

            column(name: "external_id", type: "VARCHAR(255)")

            column(defaultValueBoolean: "false", name: "external_scheduling", type: "BIT")

            column(name: "parent_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-23") {
        createTable(tableName: "court_price_condition_courts") {
            column(name: "cc_court_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_courtpricecondition_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-24") {
        createTable(tableName: "court_slot_condition_court") {
            column(name: "court_slot_condition_courts_id", type: "BIGINT")

            column(name: "court_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-25") {
        createTable(tableName: "customer") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "address1", type: "VARCHAR(255)")

            column(name: "address2", type: "VARCHAR(255)")

            column(defaultValueBoolean: "false", name: "archived", type: "BIT")

            column(name: "cellphone", type: "VARCHAR(255)")

            column(name: "city", type: "VARCHAR(255)")

            column(name: "companyname", type: "VARCHAR(255)")

            column(name: "contact", type: "VARCHAR(255)")

            column(name: "country", type: "VARCHAR(255)")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)")

            column(name: "guardian_email", type: "VARCHAR(255)")

            column(name: "guardian_name", type: "VARCHAR(255)")

            column(name: "guardian_telephone", type: "VARCHAR(255)")

            column(name: "web", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "firstname", type: "VARCHAR(255)")

            column(name: "invoice_address1", type: "VARCHAR(255)")

            column(name: "invoice_address2", type: "VARCHAR(255)")

            column(name: "invoice_city", type: "VARCHAR(255)")

            column(name: "invoice_zipcode", type: "VARCHAR(255)")

            column(name: "invoice_contact", type: "VARCHAR(255)")

            column(name: "invoice_email", type: "VARCHAR(255)")

            column(name: "invoice_telephone", type: "VARCHAR(255)")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "lastname", type: "VARCHAR(255)")

            column(name: "notes", type: "LONGTEXT")

            column(name: "number", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "security_number", type: "VARCHAR(255)")

            column(name: "telephone", type: "VARCHAR(255)")

            column(name: "type", type: "VARCHAR(255)")

            column(name: "user_id", type: "BIGINT")

            column(name: "zipcode", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-26") {
        createTable(tableName: "customer_coupon") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "date_locked", type: "DATETIME")

            column(name: "note", type: "VARCHAR(255)")

            column(name: "created_by_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "expire_date", type: "DATETIME")

            column(name: "payment_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-27") {
        createTable(tableName: "customer_coupon_ticket") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_id", type: "BIGINT")

            column(name: "customer_coupon_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "consumed", type: "DATETIME")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-28") {
        createTable(tableName: "customer_group") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-29") {
        createTable(tableName: "customer_group_price_condition_facility_group") {
            column(name: "customer_group_price_condition_groups_id", type: "BIGINT")

            column(name: "group_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-30") {
        createTable(tableName: "device") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "blocked", type: "DATETIME")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "device_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "device_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "device_model", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_used", type: "DATETIME")

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-31") {
        createTable(tableName: "external_synchronization_entity") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "entity", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "entity_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "external_entity_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "instance", type: "VARCHAR(255)")

            column(name: "last_synchronized", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "system", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-32") {
        createTable(tableName: "facility") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "address", type: "VARCHAR(255)")

            column(name: "apikey", type: "VARCHAR(255)")

            column(name: "bookable", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "booking_invoice_row_description", type: "VARCHAR(255)")

            column(name: "booking_invoice_row_external_article_id", type: "VARCHAR(255)")

            column(name: "booking_notification_note", type: "VARCHAR(255)")

            column(name: "booking_rule_num_active_bookings_per_user", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "booking_rule_num_days_bookable", type: "INT") {
                constraints(nullable: "false")
            }

            column(defaultValueBoolean: "false", name: "boxnet", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "city", type: "VARCHAR(255)")

            column(name: "country", type: "VARCHAR(255)")

            column(name: "default_booking_customer_id", type: "BIGINT")

            column(name: "default_booking_user_id", type: "BIGINT")

            column(name: "description", type: "LONGTEXT")

            column(name: "email", type: "VARCHAR(255)")

            column(name: "facility_logotype_image_id", type: "BIGINT")

            column(name: "facility_overview_image_id", type: "BIGINT")

            column(name: "facility_welcome_image_id", type: "BIGINT")

            column(name: "fax", type: "VARCHAR(255)")

            column(name: "lat", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "lng", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "membership_end_date", type: "DATETIME")

            column(name: "municipality_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "shortname", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "telephone", type: "VARCHAR(255)")

            column(name: "vat", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "zipcode", type: "VARCHAR(255)")

            column(name: "mlcs_grace_minutes_end", type: "INT")

            column(name: "mlcs_grace_minutes_start", type: "INT")

            column(name: "mlcs_last_heartbeat", type: "DATETIME")

            column(name: "fortnox_db", type: "VARCHAR(255)")

            column(name: "fortnox_token", type: "VARCHAR(255)")

            column(defaultValueBoolean: "false", name: "recieve_membership_requests", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "membership_request_setting", type: "VARCHAR(255)")

            column(name: "membership_request_email", type: "VARCHAR(255)")

            column(name: "membership_request_description", type: "LONGTEXT")

            column(defaultValueBoolean: "false", name: "use_invoice_fees", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "invoice_fee_amount", type: "BIGINT")

            column(name: "invoice_fee_articles", type: "VARCHAR(255)")

            column(name: "invoice_fee_external_article_id", type: "VARCHAR(255)")

            column(name: "invoice_fee_description", type: "LONGTEXT")

            column(name: "bankgiro", type: "VARCHAR(255)")

            column(name: "invoicing", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "orgnr", type: "VARCHAR(255)")

            column(name: "plusgiro", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-33") {
        createTable(tableName: "facility_availability") {
            column(name: "facility_availabilities_id", type: "BIGINT")

            column(name: "availability_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-34") {
        createTable(tableName: "facility_booking_cancel_rule") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "expires", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "return_percentage", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "booking_cancel_rules_idx", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-35") {
        createTable(tableName: "facility_contract") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fixed_monthly_fee", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "variable_mediation_fee", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "3.15", name: "variable_mediation_fee_percentage", type: "DOUBLE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-36") {
        createTable(tableName: "facility_group") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-37") {
        createTable(tableName: "facility_message") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "headline", type: "VARCHAR(255)")

            column(name: "html", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "valid_from", type: "DATETIME")

            column(name: "valid_to", type: "DATETIME")

            column(name: "channel", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-38") {
        createTable(tableName: "facility_property") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "key_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "VARCHAR(3000)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-39") {
        createTable(tableName: "facility_sport") {
            column(name: "facility_sports_id", type: "BIGINT")

            column(name: "sport_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-40") {
        createTable(tableName: "invoice") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "number", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date", type: "DATE") {
                constraints(nullable: "false")
            }

            column(name: "invoice_date", type: "DATE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "paid_date", type: "DATE")

            column(name: "sent", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "text", type: "LONGTEXT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-41") {
        createTable(tableName: "invoice_payment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "invoice_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "payment_date", type: "DATE") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-42") {
        createTable(tableName: "invoice_row") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "discount", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "external_article_id", type: "VARCHAR(255)")

            column(name: "invoice_id", type: "BIGINT")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "unit", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "vat", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "account", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-43") {
        createTable(tableName: "member_type_price_condition_membership_type") {
            column(name: "member_type_price_condition_membership_types_id", type: "BIGINT")

            column(name: "membership_type_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-44") {
        createTable(tableName: "membership") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "family_id", type: "BIGINT")

            column(name: "invoice_row_id", type: "BIGINT")

            column(name: "facility_id", type: "BIGINT")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "type_id", type: "BIGINT")

            column(name: "user_id", type: "BIGINT")

            column(name: "created_by_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-45") {
        createTable(tableName: "membership_family") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "contact_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-46") {
        createTable(tableName: "membership_family_members") {
            column(name: "membership_family_id", type: "BIGINT")

            column(name: "membership_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-47") {
        createTable(tableName: "membership_payment_history") {
            column(name: "membership_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "invoice_row_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-48") {
        createTable(tableName: "membership_type") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-49") {
        createTable(tableName: "mfile") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "content_type", type: "VARCHAR(255)")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "original_file_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "size", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "text_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-50") {
        createTable(tableName: "municipality") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lat", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "lng", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "region_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "zoomlv", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-51") {
        createTable(tableName: "order") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "article", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "date_delivery", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "issuer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "origin", type: "VARCHAR(255)")

            column(name: "price", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT")

            column(name: "vat", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-52") {
        createTable(tableName: "order_metadata") {
            column(name: "metadata", type: "BIGINT")

            column(name: "metadata_idx", type: "VARCHAR(255)")

            column(name: "metadata_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-53") {
        createTable(tableName: "order_order_payments") {
            column(name: "order_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "payment_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-54") {
        createTable(tableName: "order_payment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "error_message", type: "VARCHAR(255)")

            column(name: "issuer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "vat", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "ticket_id", type: "BIGINT")

            column(name: "date_to_capture_amount", type: "DATETIME")

            column(name: "transaction_id", type: "VARCHAR(255)")

            column(name: "credited", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-55") {
        createTable(tableName: "order_refund") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "note", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vat", type: "DECIMAL(19,2)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "issuer_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-56") {
        createTable(tableName: "participation") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "BIGINT")

            column(name: "joined", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "occasion_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "payment_id", type: "BIGINT")

            column(name: "user_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-57") {
        createTable(tableName: "payment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "VARCHAR(255)")

            column(name: "article_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "auth_code", type: "VARCHAR(255)")

            column(name: "batch_id", type: "VARCHAR(255)")

            column(name: "card_number", type: "VARCHAR(255)")

            column(name: "card_type", type: "VARCHAR(255)")

            column(name: "coupon_id", type: "VARCHAR(255)")

            column(name: "currency", type: "VARCHAR(255)")

            column(name: "customer_id", type: "BIGINT")

            column(name: "date_annulled", type: "DATETIME")

            column(name: "date_confirmed", type: "DATETIME")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "date_delivery", type: "DATETIME")

            column(name: "date_reversed", type: "DATETIME")

            column(name: "exp_date", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fee_amount", type: "VARCHAR(255)")

            column(name: "ipcountry", type: "VARCHAR(255)")

            column(name: "issueing_bank", type: "VARCHAR(255)")

            column(name: "issueing_country", type: "VARCHAR(255)")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "mac", type: "VARCHAR(255)")

            column(name: "merchant_id", type: "VARCHAR(255)")

            column(name: "method", type: "VARCHAR(255)")

            column(name: "my3dsec", type: "VARCHAR(255)")

            column(name: "nets_status", type: "VARCHAR(255)")

            column(name: "nets_status_code", type: "VARCHAR(255)")

            column(name: "order_description", type: "VARCHAR(255)")

            column(name: "order_nr", type: "VARCHAR(255)")

            column(name: "payment_method", type: "VARCHAR(255)")

            column(name: "payment_version", type: "VARCHAR(255)")

            column(name: "risk_score", type: "VARCHAR(255)")

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_id", type: "VARCHAR(255)")

            column(name: "user_id", type: "BIGINT")

            column(name: "vat", type: "VARCHAR(255)")

            column(name: "order_number", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-58") {
        createTable(tableName: "payment_info") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "transaction_id", type: "VARCHAR(255)")

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "expiry_date", type: "VARCHAR(255)")

            column(name: "hash", type: "VARCHAR(255)")

            column(name: "masked_pan", type: "VARCHAR(255)")

            column(name: "issuer", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-59") {
        createTable(tableName: "payment_order") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "article_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "customer_coupon_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "method", type: "VARCHAR(255)")

            column(name: "order_description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "order_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "DECIMAL(19,2)")

            column(name: "price_description", type: "VARCHAR(255)")

            column(name: "save_payment_info", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT")

            column(name: "user_coupon_id", type: "BIGINT")

            column(name: "vat", type: "DECIMAL(19,2)")

            column(name: "customer_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-60") {
        createTable(tableName: "payment_order_order_parameters") {
            column(name: "order_parameters", type: "VARCHAR(255)")

            column(name: "order_parameters_idx", type: "VARCHAR(255)")

            column(name: "order_parameters_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-61") {
        createTable(tableName: "payment_order_parameters") {
            column(name: "order_parameters", type: "BIGINT")

            column(name: "order_parameters_idx", type: "VARCHAR(255)")

            column(name: "order_parameters_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-62") {
        createTable(tableName: "payment_request") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "order_nr", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-63") {
        createTable(tableName: "payment_transaction") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cash_register_transaction_id", type: "VARCHAR(255)")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "paid_amount", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "payment_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "invoice_row_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-64") {
        createTable(tableName: "price") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "customer_category_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "price_category_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-65") {
        createTable(tableName: "price_list") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "end_date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sport_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(defaultValueBoolean: "false", name: "subscriptions", type: "BIT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-66") {
        createTable(tableName: "price_list_condition_category") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "default_category", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "pricelist_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-67") {
        createTable(tableName: "price_list_customer_category") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "default_category", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-68") {
        createTable(tableName: "qrtz_blob_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "BLOB_DATA", type: "BLOB")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-69") {
        createTable(tableName: "qrtz_calendars") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "CALENDAR_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "CALENDAR", type: "BLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-70") {
        createTable(tableName: "qrtz_cron_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "CRON_EXPRESSION", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TIME_ZONE_ID", type: "VARCHAR(80)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-71") {
        createTable(tableName: "qrtz_fired_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "ENTRY_ID", type: "VARCHAR(95)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "INSTANCE_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "FIRED_TIME", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "PRIORITY", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "STATE", type: "VARCHAR(16)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_NAME", type: "VARCHAR(200)")

            column(name: "JOB_GROUP", type: "VARCHAR(200)")

            column(name: "IS_NONCONCURRENT", type: "VARCHAR(1)")

            column(name: "REQUESTS_RECOVERY", type: "VARCHAR(1)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-72") {
        createTable(tableName: "qrtz_job_details") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR(250)")

            column(name: "JOB_CLASS_NAME", type: "VARCHAR(250)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DURABLE", type: "VARCHAR(1)") {
                constraints(nullable: "false")
            }

            column(name: "IS_NONCONCURRENT", type: "VARCHAR(1)") {
                constraints(nullable: "false")
            }

            column(name: "IS_UPDATE_DATA", type: "VARCHAR(1)") {
                constraints(nullable: "false")
            }

            column(name: "REQUESTS_RECOVERY", type: "VARCHAR(1)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_DATA", type: "BLOB")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-73") {
        createTable(tableName: "qrtz_locks") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "LOCK_NAME", type: "VARCHAR(40)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-74") {
        createTable(tableName: "qrtz_paused_trigger_grps") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-75") {
        createTable(tableName: "qrtz_scheduler_state") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "INSTANCE_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_CHECKIN_TIME", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "CHECKIN_INTERVAL", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-76") {
        createTable(tableName: "qrtz_simple_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "REPEAT_COUNT", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "REPEAT_INTERVAL", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "TIMES_TRIGGERED", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-77") {
        createTable(tableName: "qrtz_simprop_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "STR_PROP_1", type: "VARCHAR(512)")

            column(name: "STR_PROP_2", type: "VARCHAR(512)")

            column(name: "STR_PROP_3", type: "VARCHAR(512)")

            column(name: "INT_PROP_1", type: "INT")

            column(name: "INT_PROP_2", type: "INT")

            column(name: "LONG_PROP_1", type: "BIGINT")

            column(name: "LONG_PROP_2", type: "BIGINT")

            column(name: "DEC_PROP_1", type: "DECIMAL(13,4)")

            column(name: "DEC_PROP_2", type: "DECIMAL(13,4)")

            column(name: "BOOL_PROP_1", type: "VARCHAR(1)")

            column(name: "BOOL_PROP_2", type: "VARCHAR(1)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-78") {
        createTable(tableName: "qrtz_triggers") {
            column(name: "SCHED_NAME", type: "VARCHAR(120)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_NAME", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "JOB_GROUP", type: "VARCHAR(200)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "VARCHAR(250)")

            column(name: "NEXT_FIRE_TIME", type: "BIGINT")

            column(name: "PREV_FIRE_TIME", type: "BIGINT")

            column(name: "PRIORITY", type: "INT")

            column(name: "TRIGGER_STATE", type: "VARCHAR(16)") {
                constraints(nullable: "false")
            }

            column(name: "TRIGGER_TYPE", type: "VARCHAR(8)") {
                constraints(nullable: "false")
            }

            column(name: "START_TIME", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "END_TIME", type: "BIGINT")

            column(name: "CALENDAR_NAME", type: "VARCHAR(200)")

            column(name: "MISFIRE_INSTR", type: "SMALLINT")

            column(name: "JOB_DATA", type: "BLOB")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-79") {
        createTable(tableName: "redeem_strategy") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "BIGINT")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "external_article_id", type: "VARCHAR(255)")

            column(name: "amount", type: "BIGINT")

            column(name: "redeem_amount_type", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-80") {
        createTable(tableName: "region") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lat", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "lng", type: "DOUBLE") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "zoomlv", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-81") {
        createTable(tableName: "role") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "authority", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-82") {
        createTable(tableName: "season") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "end_time", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "start_time", type: "DATETIME") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-83") {
        createTable(tableName: "season_court_opening_hours") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_length", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "closes", type: "TIME") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "opens", type: "TIME") {
                constraints(nullable: "false")
            }

            column(name: "season_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "time_between", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "week_day", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-84") {
        createTable(tableName: "season_deviation") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_length", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "court_ids", type: "VARCHAR(255)")

            column(name: "from_date", type: "DATE")

            column(name: "from_time", type: "TIME")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "open", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "season_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "time_between", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "to_date", type: "DATE")

            column(name: "to_time", type: "TIME")

            column(name: "week_days", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-85") {
        createTable(tableName: "season_deviation_slot") {
            column(name: "slot_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "season_deviation_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-86") {
        createTable(tableName: "slot") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "end_time", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "start_time", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "subscription_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-87") {
        createTable(tableName: "slot_condition") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "end_time", type: "TIME")

            column(name: "start_time", type: "TIME")

            column(name: "end_date", type: "DATE")

            column(name: "start_date", type: "DATE")

            column(name: "not_valid_for_activities", type: "BIT")

            column(name: "nr_of_days", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-88") {
        createTable(tableName: "slot_condition_set") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-89") {
        createTable(tableName: "slot_condition_set_slot_condition") {
            column(name: "slot_condition_set_slot_conditions_id", type: "BIGINT")

            column(name: "slot_condition_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-90") {
        createTable(tableName: "slot_payment") {
            column(name: "slot_payments_id", type: "VARCHAR(255)")

            column(name: "payment_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-91") {
        createTable(tableName: "slot_redeem") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "invoice_row_id", type: "BIGINT")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "slot_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-92") {
        createTable(tableName: "sport") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-93") {
        createTable(tableName: "sport_attribute") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sport_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-94") {
        createTable(tableName: "sport_profile") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "frequency", type: "VARCHAR(255)")

            column(name: "skill_level", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "sport_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-95") {
        createTable(tableName: "sport_profile_attribute") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "skill_level", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "sport_attribute_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sport_profile_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-96") {
        createTable(tableName: "sport_profile_facilities") {
            column(name: "sport_profile_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-97") {
        createTable(tableName: "sport_profile_mindset") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "badge_color", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-98") {
        createTable(tableName: "sport_profile_mindsets") {
            column(name: "sport_profile_mindset_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sport_profile_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-99") {
        createTable(tableName: "subscription") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_group_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "copied_date", type: "DATETIME")

            column(name: "customer_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT")

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "show_comment", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "time_interval", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT")

            column(name: "weekday", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "invoice_row_id", type: "BIGINT")

            column(name: "time", type: "TIME")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-100") {
        createTable(tableName: "subscription_redeem") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "redeem_at", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "strategy_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-101") {
        createTable(tableName: "ticket") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "consumed", type: "DATETIME")

            column(name: "customer_id", type: "BIGINT")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "expires", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "ticketKey", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-102") {
        createTable(tableName: "token") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "blocked", type: "DATETIME")

            column(name: "date_created", type: "DATETIME") {
                constraints(nullable: "false")
            }

            column(name: "device_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "DATETIME") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-103") {
        createTable(tableName: "user") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account_expired", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "account_locked", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "activationcode", type: "VARCHAR(255)")

            column(name: "address", type: "VARCHAR(255)")

            column(name: "birthday", type: "DATETIME")

            column(name: "city", type: "VARCHAR(255)")

            column(name: "country", type: "VARCHAR(255)")

            column(name: "date_activated", type: "DATETIME")

            column(name: "date_blocked", type: "DATETIME")

            column(name: "date_created", type: "DATETIME")

            column(name: "description", type: "LONGTEXT")

            column(name: "email", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "facebookuid", type: "VARCHAR(255)")

            column(name: "facility_id", type: "BIGINT")

            column(name: "firstname", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "gender", type: "VARCHAR(255)")

            column(name: "last_logged_in", type: "DATETIME")

            column(name: "last_updated", type: "DATETIME")

            column(name: "lastname", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "matchable", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "municipality_id", type: "BIGINT")

            column(name: "password", type: "VARCHAR(255)")

            column(name: "password_expired", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "profile_image_id", type: "BIGINT")

            column(name: "receive_booking_notifications", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "receive_newsletters", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "searchable", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "telephone", type: "VARCHAR(255)")

            column(name: "zipcode", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-104") {
        createTable(tableName: "user_availability") {
            column(name: "user_availabilities_id", type: "BIGINT")

            column(name: "availability_id", type: "BIGINT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-105") {
        createTable(tableName: "user_coupon") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-106") {
        createTable(tableName: "user_coupon_ticket") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "booking_id", type: "BIGINT")

            column(name: "user_coupon_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "valid", type: "BIT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-107") {
        createTable(tableName: "user_role") {
            column(name: "role_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-108") {
        createTable(tableName: "userconnection") {
            column(name: "userId", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "providerId", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "providerUserId", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "accessToken", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "displayName", type: "VARCHAR(255)")

            column(name: "expireTime", type: "BIGINT")

            column(name: "imageUrl", type: "VARCHAR(255)")

            column(name: "profileUrl", type: "VARCHAR(255)")

            column(name: "rank", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "refreshToken", type: "VARCHAR(255)")

            column(name: "secret", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-109") {
        createTable(tableName: "weekday_slot_condition_weekdays") {
            column(name: "weekday_slot_condition_id", type: "BIGINT")

            column(name: "weekdays_integer", type: "INT")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-110") {
        addPrimaryKey(columnNames: "order_id, payment_id", tableName: "order_order_payments")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-111") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", tableName: "qrtz_blob_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-112") {
        addPrimaryKey(columnNames: "SCHED_NAME, CALENDAR_NAME", tableName: "qrtz_calendars")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-113") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", tableName: "qrtz_cron_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-114") {
        addPrimaryKey(columnNames: "SCHED_NAME, ENTRY_ID", tableName: "qrtz_fired_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-115") {
        addPrimaryKey(columnNames: "SCHED_NAME, JOB_NAME, JOB_GROUP", tableName: "qrtz_job_details")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-116") {
        addPrimaryKey(columnNames: "SCHED_NAME, LOCK_NAME", tableName: "qrtz_locks")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-117") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_GROUP", tableName: "qrtz_paused_trigger_grps")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-118") {
        addPrimaryKey(columnNames: "SCHED_NAME, INSTANCE_NAME", tableName: "qrtz_scheduler_state")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-119") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", tableName: "qrtz_simple_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-120") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", tableName: "qrtz_simprop_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-121") {
        addPrimaryKey(columnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", tableName: "qrtz_triggers")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-122") {
        addPrimaryKey(columnNames: "slot_id, season_deviation_id", tableName: "season_deviation_slot")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-123") {
        addPrimaryKey(columnNames: "facility_id, sport_profile_id", tableName: "sport_profile_facilities")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-124") {
        addPrimaryKey(columnNames: "sport_profile_id, sport_profile_mindset_id", tableName: "sport_profile_mindsets")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-125") {
        addPrimaryKey(columnNames: "role_id, user_id", tableName: "user_role")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-126") {
        addPrimaryKey(columnNames: "userId, providerId, providerUserId", tableName: "userconnection")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-267") {
        createIndex(indexName: "FK9D4BF30F5BCD968F", tableName: "activity", unique: "false") {
            column(name: "large_image_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-268") {
        createIndex(indexName: "FK9D4BF30FDDF0EBF7", tableName: "activity", unique: "false") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-269") {
        createIndex(indexName: "FK61502A17BF3CB3E", tableName: "activity_occasion", unique: "false") {
            column(name: "activity_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-270") {
        createIndex(indexName: "FK2E16D2311071833D", tableName: "activity_occasion_booking", unique: "false") {
            column(name: "activity_occasion_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-271") {
        createIndex(indexName: "FK2E16D2319821AE5D", tableName: "activity_occasion_booking", unique: "false") {
            column(name: "booking_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-272") {
        createIndex(indexName: "FK28713064BF3CB3E", tableName: "activity_slot_condition_activity", unique: "false") {
            column(name: "activity_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-273") {
        createIndex(indexName: "FK28713064C1105B8D", tableName: "activity_slot_condition_activity", unique: "false") {
            column(name: "activity_slot_condition_activities_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-274") {
        createIndex(indexName: "FK1A546842C973F405", tableName: "coupon_condition_group", unique: "false") {
            column(name: "coupon_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-275") {
        createIndex(indexName: "FKE33F2445704B68BB", tableName: "coupon_condition_groups_slot_conditions_sets", unique: "false") {
            column(name: "slot_condition_set_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-276") {
        createIndex(indexName: "FKE33F24457FBD2ED", tableName: "coupon_condition_groups_slot_conditions_sets", unique: "false") {
            column(name: "coupon_condition_group_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-277") {
        createIndex(indexName: "FK5C43E35A13E6BDDD", tableName: "court_slot_condition_court", unique: "false") {
            column(name: "court_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-278") {
        createIndex(indexName: "FK5C43E35AFDE2F5EC", tableName: "court_slot_condition_court", unique: "false") {
            column(name: "court_slot_condition_courts_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-279") {
        createIndex(indexName: "facility_id", tableName: "customer", unique: "true") {
            column(name: "facility_id")

            column(name: "user_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-280") {
        createIndex(indexName: "user_id", tableName: "device", unique: "true") {
            column(name: "user_id")

            column(name: "device_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-281") {
        createIndex(indexName: "entity_id", tableName: "external_synchronization_entity", unique: "true") {
            column(name: "entity_id")

            column(name: "system")

            column(name: "external_entity_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-282") {
        createIndex(indexName: "shortname", tableName: "facility", unique: "true") {
            column(name: "shortname")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-283") {
        createIndex(indexName: "facility_id", tableName: "facility_property", unique: "true") {
            column(name: "facility_id")

            column(name: "key_name")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-284") {
        createIndex(indexName: "FKB6018D411773B33D", tableName: "participation", unique: "false") {
            column(name: "payment_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-285") {
        createIndex(indexName: "FKB6018D4119A51177", tableName: "participation", unique: "false") {
            column(name: "user_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-286") {
        createIndex(indexName: "FKB6018D41D7E0506D", tableName: "participation", unique: "false") {
            column(name: "occasion_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-287") {
        createIndex(indexName: "authority", tableName: "role", unique: "true") {
            column(name: "authority")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-288") {
        createIndex(indexName: "start_time_court_idx", tableName: "slot", unique: "false") {
            column(name: "court_id")

            column(name: "start_time")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-289") {
        createIndex(indexName: "FK97ECADDC2E52FA44", tableName: "slot_condition_set_slot_condition", unique: "false") {
            column(name: "slot_condition_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-290") {
        createIndex(indexName: "FK97ECADDCA1E61F21", tableName: "slot_condition_set_slot_condition", unique: "false") {
            column(name: "slot_condition_set_slot_conditions_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-291") {
        createIndex(indexName: "name", tableName: "sport", unique: "true") {
            column(name: "name")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-292") {
        createIndex(indexName: "name", tableName: "sport_profile_mindset", unique: "true") {
            column(name: "name")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-293") {
        createIndex(indexName: "identifier", tableName: "token", unique: "true") {
            column(name: "identifier")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-294") {
        createIndex(indexName: "email", tableName: "user", unique: "true") {
            column(name: "email")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-295") {
        createIndex(indexName: "FK23EAB4B93022CC77", tableName: "weekday_slot_condition_weekdays", unique: "false") {
            column(name: "weekday_slot_condition_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1404814501991-127") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "abstract_price_condition", constraintName: "FK457293889034C250", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "price_list_condition_category", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-128") {
        addForeignKeyConstraint(baseColumnNames: "customer_category_id", baseTableName: "abstract_price_condition", constraintName: "FK4572938886086D9C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "price_list_customer_category", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-129") {
        addForeignKeyConstraint(baseColumnNames: "message_id", baseTableName: "async_mail_attachment", constraintName: "FK1CACA0E817082B9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "async_mail_mess", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-130") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "booking", constraintName: "fk_b_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-131") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "booking", constraintName: "FK3DB08596EFA0F1E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "booking_group", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-132") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "booking", constraintName: "FK3DB08599195D0E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-133") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "booking", constraintName: "booking_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-134") {
        addForeignKeyConstraint(baseColumnNames: "slot_id", baseTableName: "booking", constraintName: "FK3DB0859A4738097", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "slot", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-135") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "booking", constraintName: "FK3DB085919A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-136") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "cash_register_transaction", constraintName: "fk_crt_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-137") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "coupon", constraintName: "FKAF42D826DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-138") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "court", constraintName: "FK5A7518BDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-139") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "court", constraintName: "FK5A7518BA541BE5E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "court", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-140") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "court", constraintName: "FK5A7518BBD84893D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-141") {
        addForeignKeyConstraint(baseColumnNames: "cc_court_id", baseTableName: "court_price_condition_courts", constraintName: "FK602DD9562623CEDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "court", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-142") {
        addForeignKeyConstraint(baseColumnNames: "cc_courtpricecondition_id", baseTableName: "court_price_condition_courts", constraintName: "FK602DD95649BFBBB7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "abstract_price_condition", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-143") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "customer", constraintName: "fk_c_facility_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-144") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "customer", constraintName: "fk_c_user_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-145") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "customer_coupon", constraintName: "fk_cc_coupon_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-146") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "customer_coupon", constraintName: "fk_user_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-147") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "customer_coupon", constraintName: "fk_cc_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-148") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "customer_coupon", constraintName: "customer_coupon_order_key", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-149") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "customer_coupon", constraintName: "fk_payment_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-150") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "customer_coupon_ticket", constraintName: "fk_cct_booking_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "booking", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-151") {
        addForeignKeyConstraint(baseColumnNames: "customer_coupon_id", baseTableName: "customer_coupon_ticket", constraintName: "fk_cct_customer_coupon_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer_coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-152") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "customer_group", constraintName: "fk_cg_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-153") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "customer_group", constraintName: "fk_cg_group_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility_group", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-154") {
        addForeignKeyConstraint(baseColumnNames: "customer_group_price_condition_groups_id", baseTableName: "customer_group_price_condition_facility_group", constraintName: "fk_cgpcfg_customer_group_price_condition_groups_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "abstract_price_condition", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-155") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "customer_group_price_condition_facility_group", constraintName: "fk_cgpcfg_group_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility_group", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-156") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "device", constraintName: "FKB06B1E5619A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-157") {
        addForeignKeyConstraint(baseColumnNames: "default_booking_customer_id", baseTableName: "facility", constraintName: "fk_f_default_booking_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-158") {
        addForeignKeyConstraint(baseColumnNames: "default_booking_user_id", baseTableName: "facility", constraintName: "facility_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-159") {
        addForeignKeyConstraint(baseColumnNames: "facility_logotype_image_id", baseTableName: "facility", constraintName: "FK1DDE6EA3E234EF09", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "mfile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-160") {
        addForeignKeyConstraint(baseColumnNames: "facility_overview_image_id", baseTableName: "facility", constraintName: "FK1DDE6EA31A541F75", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "mfile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-161") {
        addForeignKeyConstraint(baseColumnNames: "facility_welcome_image_id", baseTableName: "facility", constraintName: "FK1DDE6EA34E71A064", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "mfile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-162") {
        addForeignKeyConstraint(baseColumnNames: "municipality_id", baseTableName: "facility", constraintName: "FK1DDE6EA3CBA79AD7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "municipality", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-163") {
        addForeignKeyConstraint(baseColumnNames: "availability_id", baseTableName: "facility_availability", constraintName: "FKF16B3177D0BAFA77", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "availability", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-164") {
        addForeignKeyConstraint(baseColumnNames: "facility_availabilities_id", baseTableName: "facility_availability", constraintName: "FKF16B31778AB62505", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-165") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_booking_cancel_rule", constraintName: "FKCB29131FDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-166") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_contract", constraintName: "FK98DA9B2EDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-167") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_group", constraintName: "FK36901E3DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-168") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_message", constraintName: "FKF49DC26BDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-169") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_property", constraintName: "FK7F7367F1DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-170") {
        addForeignKeyConstraint(baseColumnNames: "facility_sports_id", baseTableName: "facility_sport", constraintName: "FK41132D85E844EFF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-171") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "facility_sport", constraintName: "FK41132D8BD84893D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-172") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "invoice", constraintName: "FK74D6432D9AE61E17", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-173") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_payment", constraintName: "FK95969EF4DA38911C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-174") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice_row", constraintName: "FKCCA3CDC896C1A954", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-175") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "invoice_row", constraintName: "FKCCA3CDC89AE61E17", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-176") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_row", constraintName: "FKCCA3CDC8DA38911C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-177") {
        addForeignKeyConstraint(baseColumnNames: "member_type_price_condition_membership_types_id", baseTableName: "member_type_price_condition_membership_type", constraintName: "FK49F8B8E98CB52318", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "abstract_price_condition", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-178") {
        addForeignKeyConstraint(baseColumnNames: "membership_type_id", baseTableName: "member_type_price_condition_membership_type", constraintName: "FK49F8B8E95D3CBAE2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership_type", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-179") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "membership", constraintName: "fk_created_by_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-180") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "membership", constraintName: "FKB01D87D69AE61E17", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-181") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "membership", constraintName: "FKB01D87D6DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-182") {
        addForeignKeyConstraint(baseColumnNames: "family_id", baseTableName: "membership", constraintName: "fk_m_family_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership_family", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-183") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "membership", constraintName: "fk_invoice_row_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice_row", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-184") {
        addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "membership", constraintName: "fk_type_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership_type", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-185") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "membership", constraintName: "FKB01D87D619A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-186") {
        addForeignKeyConstraint(baseColumnNames: "contact_id", baseTableName: "membership_family", constraintName: "fk_contact_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-187") {
        addForeignKeyConstraint(baseColumnNames: "membership_family_id", baseTableName: "membership_family_members", constraintName: "fk_membership_family_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership_family", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-188") {
        addForeignKeyConstraint(baseColumnNames: "membership_id", baseTableName: "membership_family_members", constraintName: "fk_membership_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-189") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "membership_payment_history", constraintName: "fk_invoice_row_id_idx", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice_row", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-190") {
        addForeignKeyConstraint(baseColumnNames: "membership_id", baseTableName: "membership_payment_history", constraintName: "fk_membership_id_idx", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "membership", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-191") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "membership_type", constraintName: "fk_facility_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-192") {
        addForeignKeyConstraint(baseColumnNames: "region_id", baseTableName: "municipality", constraintName: "FKBDF4447C2EFA6197", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "region", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-193") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "order", constraintName: "FK651874E9AE61E17", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-194") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "order", constraintName: "FK651874EDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-195") {
        addForeignKeyConstraint(baseColumnNames: "issuer_id", baseTableName: "order", constraintName: "FK651874E2D7AFBC9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-196") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "order", constraintName: "FK651874E19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-197") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_order_payments", constraintName: "FK7213B20F9195D0E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-198") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "order_order_payments", constraintName: "FK7213B20F3090B438", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_payment", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-199") {
        addForeignKeyConstraint(baseColumnNames: "issuer_id", baseTableName: "order_payment", constraintName: "FKC66032152D7AFBC9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-200") {
        addForeignKeyConstraint(baseColumnNames: "ticket_id", baseTableName: "order_payment", constraintName: "FKC6603215490859C9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer_coupon_ticket", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-201") {
        addForeignKeyConstraint(baseColumnNames: "issuer_id", baseTableName: "order_refund", constraintName: "FK3B8C21892D7AFBC9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-202") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_refund", constraintName: "FK3B8C21899195D0E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-203") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "participation", constraintName: "fk_p_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-204") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "participation", constraintName: "participation_order_constraint", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-205") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "payment", constraintName: "fk_pay_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-206") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "payment", constraintName: "FKD11C3206DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-207") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "payment", constraintName: "FKD11C320619A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-208") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "payment_info", constraintName: "FKE25C3F4719A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-209") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "payment_order", constraintName: "fk_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-210") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "payment_order", constraintName: "FK6981FFD519A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-211") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "payment_transaction", constraintName: "fk_p_constraint", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-212") {
        addForeignKeyConstraint(baseColumnNames: "customer_category_id", baseTableName: "price", constraintName: "FK65FB14986086D9C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "price_list_customer_category", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-213") {
        addForeignKeyConstraint(baseColumnNames: "price_category_id", baseTableName: "price", constraintName: "FK65FB14975DC2C5A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "price_list_condition_category", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-214") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "price_list", constraintName: "FKA8C61DD4DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-215") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "price_list", constraintName: "FKA8C61DD4BD84893D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-216") {
        addForeignKeyConstraint(baseColumnNames: "pricelist_id", baseTableName: "price_list_condition_category", constraintName: "FK11CA9FEDE83D9AFD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "price_list", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-217") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "price_list_customer_category", constraintName: "FK13CA2294DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-218") {
        addForeignKeyConstraint(baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", baseTableName: "qrtz_blob_triggers", constraintName: "qrtz_blob_triggers_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", referencedTableName: "qrtz_triggers", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-219") {
        addForeignKeyConstraint(baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", baseTableName: "qrtz_cron_triggers", constraintName: "qrtz_cron_triggers_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", referencedTableName: "qrtz_triggers", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-220") {
        addForeignKeyConstraint(baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", baseTableName: "qrtz_simple_triggers", constraintName: "qrtz_simple_triggers_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", referencedTableName: "qrtz_triggers", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-221") {
        addForeignKeyConstraint(baseColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", baseTableName: "qrtz_simprop_triggers", constraintName: "qrtz_simprop_triggers_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP", referencedTableName: "qrtz_triggers", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-222") {
        addForeignKeyConstraint(baseColumnNames: "SCHED_NAME, JOB_NAME, JOB_GROUP", baseTableName: "qrtz_triggers", constraintName: "qrtz_triggers_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "SCHED_NAME, JOB_NAME, JOB_GROUP", referencedTableName: "qrtz_job_details", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-223") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "redeem_strategy", constraintName: "fk_coupon_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-224") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "season", constraintName: "FKC9FA6AE3DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-225") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "season_court_opening_hours", constraintName: "FKFE97DA5813E6BDDD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "court", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-226") {
        addForeignKeyConstraint(baseColumnNames: "season_id", baseTableName: "season_court_opening_hours", constraintName: "FKFE97DA58989C2737", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "season", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-227") {
        addForeignKeyConstraint(baseColumnNames: "season_id", baseTableName: "season_deviation", constraintName: "FK7AF4DA25989C2737", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "season", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-228") {
        addForeignKeyConstraint(baseColumnNames: "season_deviation_id", baseTableName: "season_deviation_slot", constraintName: "FK829757D85437B696", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "season_deviation", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-229") {
        addForeignKeyConstraint(baseColumnNames: "slot_id", baseTableName: "season_deviation_slot", constraintName: "FK829757D8A4738097", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "slot", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-230") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "slot", constraintName: "FK35E9FE13E6BDDD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "court", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-231") {
        addForeignKeyConstraint(baseColumnNames: "subscription_id", baseTableName: "slot", constraintName: "FK35E9FEA1C84B37", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "subscription", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-232") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "slot_payment", constraintName: "FKBDCEE4C51773B33D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-233") {
        addForeignKeyConstraint(baseColumnNames: "slot_payments_id", baseTableName: "slot_payment", constraintName: "FKBDCEE4C5E133D447", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "slot", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-234") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "slot_redeem", constraintName: "fk_sr_coupon_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-235") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "slot_redeem", constraintName: "fk_sr_invoice_row_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice_row", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-236") {
        addForeignKeyConstraint(baseColumnNames: "slot_id", baseTableName: "slot_redeem", constraintName: "fk_sr_slot_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "slot", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-237") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "sport_attribute", constraintName: "FK262E0B91BD84893D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-238") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "sport_profile", constraintName: "FK8392A3DEBD84893D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-239") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "sport_profile", constraintName: "FK8392A3DE19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-240") {
        addForeignKeyConstraint(baseColumnNames: "sport_attribute_id", baseTableName: "sport_profile_attribute", constraintName: "FK193E8B7BE4E7E16D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport_attribute", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-241") {
        addForeignKeyConstraint(baseColumnNames: "sport_profile_id", baseTableName: "sport_profile_attribute", constraintName: "FK193E8B7B66131B0D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport_profile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-242") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "sport_profile_facilities", constraintName: "FK16625D82DDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-243") {
        addForeignKeyConstraint(baseColumnNames: "sport_profile_id", baseTableName: "sport_profile_facilities", constraintName: "FK16625D8266131B0D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport_profile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-244") {
        addForeignKeyConstraint(baseColumnNames: "sport_profile_id", baseTableName: "sport_profile_mindsets", constraintName: "FKAF8482E466131B0D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport_profile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-245") {
        addForeignKeyConstraint(baseColumnNames: "sport_profile_mindset_id", baseTableName: "sport_profile_mindsets", constraintName: "FKAF8482E43B52EF8A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "sport_profile_mindset", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-246") {
        addForeignKeyConstraint(baseColumnNames: "booking_group_id", baseTableName: "subscription", constraintName: "FK1456591DCFF58C44", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "booking_group", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-247") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "subscription", constraintName: "fk_s_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-248") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "subscription", constraintName: "FK1456591DDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-249") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "subscription", constraintName: "fk_invoice_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "invoice_row", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-250") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "subscription", constraintName: "FK1456591D19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-251") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "subscription_redeem", constraintName: "fk_sr_facility_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-252") {
        addForeignKeyConstraint(baseColumnNames: "strategy_id", baseTableName: "subscription_redeem", constraintName: "fk_sr_strategy_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "redeem_strategy", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-253") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "ticket", constraintName: "fk_t_customer_id", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "customer", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-254") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "ticket", constraintName: "FKCBE86B0C19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-255") {
        addForeignKeyConstraint(baseColumnNames: "device_id", baseTableName: "token", constraintName: "FK696B9F97D163788", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "device", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-256") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "user", constraintName: "FK36EBCBDDF0EBF7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "facility", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-257") {
        addForeignKeyConstraint(baseColumnNames: "municipality_id", baseTableName: "user", constraintName: "FK36EBCBCBA79AD7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "municipality", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-258") {
        addForeignKeyConstraint(baseColumnNames: "profile_image_id", baseTableName: "user", constraintName: "FK36EBCBB523E1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "mfile", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-259") {
        addForeignKeyConstraint(baseColumnNames: "availability_id", baseTableName: "user_availability", constraintName: "FK342D554FD0BAFA77", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "availability", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-260") {
        addForeignKeyConstraint(baseColumnNames: "user_availabilities_id", baseTableName: "user_availability", constraintName: "FK342D554FFF903BD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-261") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "user_coupon", constraintName: "FKDB7C1F1AC973F405", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-262") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_coupon", constraintName: "FKDB7C1F1A19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-263") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "user_coupon_ticket", constraintName: "FKBFA9F7D19821AE5D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "booking", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-264") {
        addForeignKeyConstraint(baseColumnNames: "user_coupon_id", baseTableName: "user_coupon_ticket", constraintName: "FKBFA9F7D151096EBC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user_coupon", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-265") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46A747A4D97", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1404814501991-266") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46A19A51177", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "matchi", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1408521373690-1") {
        addColumn(tableName: "booking") {
            column(name: "date_reminded", type: "bit")
        }
    }
}
