databaseChangeLog = {

    changeSet(author: "calle (generated)", id: "1407840037990-1") {
        modifyDataType(columnName: "online", newDataType: "bit", tableName: "booking")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-2") {
        addNotNullConstraint(columnDataType: "bit", columnName: "online", defaultNullValue: "0", tableName: "booking")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-4") {
        modifyDataType(columnName: "available_online", newDataType: "bit", tableName: "coupon")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-5") {
        addNotNullConstraint(columnDataType: "bit", columnName: "available_online", defaultNullValue: "0", tableName: "coupon")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-6") {
        modifyDataType(columnName: "booking_notification_note", newDataType: "longtext", tableName: "facility")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-7") {
        modifyDataType(columnName: "variable_mediation_fee_percentage", newDataType: "double precision", tableName: "facility_contract")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-8") {
        dropNotNullConstraint(columnDataType: "double precision", columnName: "variable_mediation_fee_percentage", tableName: "facility_contract")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-11") {
        modifyDataType(columnName: "expiration_date", newDataType: "date", tableName: "invoice")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-12") {
        dropNotNullConstraint(columnDataType: "date", columnName: "expiration_date", tableName: "invoice")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-13") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "created_by_id", tableName: "membership")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-14") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "note", tableName: "order_refund")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-15") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "order_number", tableName: "payment")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-16") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "region")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-17") {
        modifyDataType(columnName: "time", newDataType: "time", tableName: "subscription")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-18") {
        addNotNullConstraint(columnDataType: "time", columnName: "time", tableName: "subscription")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-19") {
        dropForeignKeyConstraint(baseTableName: "booking", constraintName: "FK3DB085919A51177")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-20") {
        dropForeignKeyConstraint(baseTableName: "facility", constraintName: "facility_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-21") {
        dropForeignKeyConstraint(baseTableName: "membership", constraintName: "FKB01D87D6DDF0EBF7")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-22") {
        dropForeignKeyConstraint(baseTableName: "membership", constraintName: "FKB01D87D619A51177")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-23") {
        dropForeignKeyConstraint(baseTableName: "membership_family_members", constraintName: "fk_membership_family_id")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-24") {
        dropForeignKeyConstraint(baseTableName: "membership_family_members", constraintName: "fk_membership_id")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-25") {
        dropForeignKeyConstraint(baseTableName: "payment", constraintName: "FKD11C320619A51177")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-26") {
        dropForeignKeyConstraint(baseTableName: "QRTZ_BLOB_TRIGGERS", constraintName: "qrtz_blob_triggers_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-27") {
        dropForeignKeyConstraint(baseTableName: "QRTZ_CRON_TRIGGERS", constraintName: "qrtz_cron_triggers_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-28") {
        dropForeignKeyConstraint(baseTableName: "QRTZ_SIMPLE_TRIGGERS", constraintName: "qrtz_simple_triggers_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-29") {
        dropForeignKeyConstraint(baseTableName: "QRTZ_SIMPROP_TRIGGERS", constraintName: "qrtz_simprop_triggers_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-30") {
        dropForeignKeyConstraint(baseTableName: "QRTZ_TRIGGERS", constraintName: "qrtz_triggers_ibfk_1")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-31") {
        dropForeignKeyConstraint(baseTableName: "subscription", constraintName: "FK1456591DDDF0EBF7")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-32") {
        dropForeignKeyConstraint(baseTableName: "subscription", constraintName: "FK1456591D19A51177")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-33") {
        dropForeignKeyConstraint(baseTableName: "user_coupon", constraintName: "FKDB7C1F1AC973F405")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-34") {
        dropForeignKeyConstraint(baseTableName: "user_coupon", constraintName: "FKDB7C1F1A19A51177")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-35") {
        dropForeignKeyConstraint(baseTableName: "user_coupon_ticket", constraintName: "FKBFA9F7D19821AE5D")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-36") {
        dropForeignKeyConstraint(baseTableName: "user_coupon_ticket", constraintName: "FKBFA9F7D151096EBC")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-59") {
        dropIndex(indexName: "FKB6018D4119A51177", tableName: "participation")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-60") {
        dropIndex(indexName: "start_time_court_idx", tableName: "slot")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-61") {
        createIndex(indexName: "FK28713064C1105B8D", tableName: "activity_slot_condition_activity") {
            column(name: "activity_slot_condition_activities_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-62") {
        createIndex(indexName: "FK5A7518BA541BE5E", tableName: "court") {
            column(name: "parent_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-63") {
        createIndex(indexName: "FK95969EF4DA38911C", tableName: "invoice_payment") {
            column(name: "invoice_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-64") {
        createIndex(indexName: "customer_id_uniq_1407840036982", tableName: "membership", unique: "true") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-65") {
        createIndex(indexName: "FK6981FFD5DDF0EBF7", tableName: "payment_order") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-66") {
        createIndex(indexName: "FK90008A259467509D", tableName: "payment_transaction") {
            column(name: "invoice_row_id")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-67") {
        createIndex(indexName: "ticketKey_uniq_1407840037014", tableName: "ticket", unique: "true") {
            column(name: "ticketKey")
        }
    }

    changeSet(author: "calle (generated)", id: "1407840037990-68") {
        dropColumn(columnName: "user_id", tableName: "booking")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-69") {
        dropColumn(columnName: "default_booking_user_id", tableName: "facility")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-70") {
        dropColumn(columnName: "facility_id", tableName: "membership")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-71") {
        dropColumn(columnName: "user_id", tableName: "membership")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-72") {
        dropColumn(columnName: "date_to_capture_amount", tableName: "order_payment")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-73") {
        dropColumn(columnName: "user_id", tableName: "participation")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-74") {
        dropColumn(columnName: "user_id", tableName: "payment")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-75") {
        dropColumn(columnName: "user_coupon_id", tableName: "payment_order")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-76") {
        dropColumn(columnName: "facility_id", tableName: "subscription")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-77") {
        dropColumn(columnName: "user_id", tableName: "subscription")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-79") {
        dropTable(tableName: "membership_family_members")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-91") {
        dropTable(tableName: "user_coupon")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-92") {
        dropTable(tableName: "user_coupon_ticket")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-37") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "activity", constraintName: "FK9D4BF30FDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-38") {
        addForeignKeyConstraint(baseColumnNames: "large_image_id", baseTableName: "activity", constraintName: "FK9D4BF30F5BCD968F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "mfile", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-39") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "activity_occasion", constraintName: "FK61502A17BF3CB3E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-40") {
        addForeignKeyConstraint(baseColumnNames: "activity_occasion_id", baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2311071833D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity_occasion", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-41") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2319821AE5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-42") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "activity_slot_condition_activity", constraintName: "FK28713064BF3CB3E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-43") {
        addForeignKeyConstraint(baseColumnNames: "activity_slot_condition_activities_id", baseTableName: "activity_slot_condition_activity", constraintName: "FK28713064C1105B8D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-44") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "coupon_condition_group", constraintName: "FK1A546842C973F405", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "coupon", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-45") {
        addForeignKeyConstraint(baseColumnNames: "coupon_condition_group_id", baseTableName: "coupon_condition_groups_slot_conditions_sets", constraintName: "FKE33F24457FBD2ED", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "coupon_condition_group", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-46") {
        addForeignKeyConstraint(baseColumnNames: "slot_condition_set_id", baseTableName: "coupon_condition_groups_slot_conditions_sets", constraintName: "FKE33F2445704B68BB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition_set", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-47") {
        addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "court", constraintName: "FK5A7518BA541BE5E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-48") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "court_slot_condition_court", constraintName: "FK5C43E35A13E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-49") {
        addForeignKeyConstraint(baseColumnNames: "court_slot_condition_courts_id", baseTableName: "court_slot_condition_court", constraintName: "FK5C43E35AFDE2F5EC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-50") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_payment", constraintName: "FK95969EF4DA38911C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "invoice", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-51") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "membership", constraintName: "FKB01D87D69AE61E17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-52") {
        addForeignKeyConstraint(baseColumnNames: "occasion_id", baseTableName: "participation", constraintName: "FKB6018D41D7E0506D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity_occasion", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-53") {
        addForeignKeyConstraint(baseColumnNames: "payment_id", baseTableName: "participation", constraintName: "FKB6018D411773B33D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "payment", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-54") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "payment_order", constraintName: "FK6981FFD5DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-55") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "payment_transaction", constraintName: "FK90008A259467509D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "invoice_row", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-56") {
        addForeignKeyConstraint(baseColumnNames: "slot_condition_id", baseTableName: "slot_condition_set_slot_condition", constraintName: "FK97ECADDC2E52FA44", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-57") {
        addForeignKeyConstraint(baseColumnNames: "slot_condition_set_slot_conditions_id", baseTableName: "slot_condition_set_slot_condition", constraintName: "FK97ECADDCA1E61F21", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition_set", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1407840037990-58") {
        addForeignKeyConstraint(baseColumnNames: "weekday_slot_condition_id", baseTableName: "weekday_slot_condition_weekdays", constraintName: "FK23EAB4B93022CC77", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition", referencesUniqueColumn: "false")
    }

}
