databaseChangeLog = {
    changeSet(author: "ami", id: "1") {
        preConditions(onFail: "WARN", onFailMessage: "DATABASECHANGELOG already exist, skipping") {
            sqlCheck(expectedResult: "0", 'select count(*) from DATABASECHANGELOG')
        }

        sqlFile(path: 'changelog-master-data.sql')
    }

    include file: 'matchi-initial.groovy'
    include file: 'diff-between-gorm-and-database.groovy'

    /*
     */

    changeSet(author: "ami (generated)", id: "1408521373690-3") {
        dropNotNullConstraint(columnDataType: "longtext", columnName: "value", tableName: "facility_property")
    }

    changeSet(author: "ami (generated)", id: "1408521373690-4") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "type", tableName: "order_payment")
    }

    changeSet(author: "ami (generated)", id: "1408610466991-2") {
        dropColumn(columnName: "end_date", tableName: "price_list")
    }

    changeSet(author: "sergei (generated)", id: "1408911045708-1") {
        addColumn(tableName: "ticket") {
            column(name: "booking_customer_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1408911045708-2") {
        addColumn(tableName: "ticket") {
            column(name: "booking_id", type: "bigint")
        }
    }

    changeSet(author: "calle (generated)", id: "1409348205363-1") {
        addColumn(tableName: "invoice") {
            column(name: "last_sent", type: "datetime")
        }
    }

    changeSet(author: "ami (generated)", id: "1409127065025-1") {
        addColumn(tableName: "abstract_price_condition") {
            column(name: "hours", type: "integer")
        }
    }

    changeSet(author: "ami (generated)", id: "1409127065025-2") {
        addColumn(tableName: "abstract_price_condition") {
            column(name: "minutes", type: "integer")
        }
    }

    changeSet(author: "calle (generated)", id: "1409916076185-2") {
        createIndex(indexName: "start_end_court_idx", tableName: "slot") {
            column(name: "court_id")
            column(name: "start_time")
            column(name: "end_time")
        }
    }

    changeSet(author: "sergei (generated)", id: "1410212031098-1") {
        addColumn(tableName: "court") {
            column(name: "list_position", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1410212031098-2") {
        sql('update court set list_position = id')
    }

    changeSet(author: "sergei (generated)", id: "1409176569170-1") {
        addColumn(tableName: "customer") {
            column(name: "birthyear", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1409176569170-2") {
        sql('''update customer set birthyear = 1900 + substr(security_number, 1, 2) where birthyear is null and type != 'COMPANY' and security_number regexp '^[[:digit:]]{6}(-.*)?$' ''')
        sql('''update customer set birthyear = substr(security_number, 1, 4) where birthyear is null and type != 'COMPANY' and security_number regexp '^[[:digit:]]{8}(-.*)?$' ''')
        sql('''update customer set birthyear = 1900 + substring_index(security_number, '/', -1) where birthyear is null and type != 'COMPANY' and security_number regexp '^[[:digit:]]{1,2}(/[[:digit:]]{1,2}){2}$' ''')
        sql('''update customer set birthyear = substr(security_number, 1, 4) where birthyear is null and type != 'COMPANY' and security_number regexp '^[[:digit:]]{4}-[[:digit:]]{1,2}-[[:digit:]]{1,2}$' ''')
        sql('''update customer set birthyear = security_number where birthyear is null and type != 'COMPANY' and security_number regexp '^[[:digit:]]{4}$' ''')
        sql('update customer set birthyear = birthyear + 100 where birthyear is not null and birthyear < 1915')
    }

    changeSet(author: "sergei (generated)", id: "1409081184559-1") {
        addColumn(tableName: "customer") {
            column(name: "club_messages_disabled", type: "bit")
        }
    }

    changeSet(author: "sergei (generated)", id: "1411237310332-1") {
        addColumn(tableName: "subscription") {
            column(name: "status", type: "varchar(255)", defaultValue: "ACTIVE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "calle", id: "1409176569170-3") {
        sql('''delete from QRTZ_TRIGGERS where JOB_NAME = 'com.matchi.jobs.PaymentDeliveryDateJob';''')
        sql('''delete from QRTZ_TRIGGERS where JOB_NAME = 'com.matchi.jobs.BookingIntegrationExportJob';''')
        sql('''delete from QRTZ_TRIGGERS where JOB_NAME = 'com.matchi.jobs.FortnoxContactJob';''')
    }

    changeSet(author: "sergei (generated)", id: "1408664273789-1") {
        createTable(tableName: "facility_contract_item") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "facility_contPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "contract_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "decimal(19,2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1408664273789-2") {
        addColumn(tableName: "facility_contract") {
            column(name: "date_valid_from", type: "date", defaultValueDate: "2001-01-01") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1408664273789-3") {
        createIndex(indexName: "FKB27965846D15345A", tableName: "facility_contract_item") {
            column(name: "contract_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1408664273789-4") {
        dropColumn(columnName: "active", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1408664273789-5") {
        addForeignKeyConstraint(baseColumnNames: "contract_id", baseTableName: "facility_contract_item", constraintName: "FKB27965846D15345A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_contract", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1412592704891-1") {
        createTable(tableName: "user_favorite") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_favoritePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }


        createIndex(indexName: "FK41A5239019A51177", tableName: "user_favorite") {
            column(name: "user_id")
        }

        createIndex(indexName: "FK41A52390DDF0EBF7", tableName: "user_favorite") {
            column(name: "facility_id")
        }

        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "user_favorite", constraintName: "FK41A52390DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_favorite", constraintName: "FK41A5239019A51177", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias", id: "1412592704891-2") {
        sql('''insert into user_favorite (version, facility_id, user_id) select 0 as version, f.id as facility_id, u.id as user_id from sport_profile_facilities spf left join sport_profile sp on spf.sport_profile_id = sp.id left join user u on u.id = sp.user_id left join facility f on f.id = spf.facility_id group by u.id,f.id order by u.id;''')
    }

    changeSet(author: "mattias (generated)", id: "1412592704891-3") {
        dropForeignKeyConstraint(baseTableName: "sport_profile_facilities", constraintName: "FK16625D82DDF0EBF7")
        dropForeignKeyConstraint(baseTableName: "sport_profile_facilities", constraintName: "FK16625D8266131B0D")

        dropColumn(columnName: "description", tableName: "sport_profile")
        dropTable(tableName: "sport_profile_facilities")
    }

    changeSet(author: "calle (generated)", id: "1413213890822-2") {
        addColumn(tableName: "facility_contract") {
            column(name: "coupon_contract_type", type: "varchar(255)", defaultValue: "PER_TICKET") {
                constraints(nullable: "false")
            }
        }

        addColumn(tableName: "facility_contract") {
            column(name: "variable_coupon_mediation_fee", type: "integer")
        }
    }

    changeSet(author: "calle (generated)", id: "1413274561839-1") {
        addColumn(tableName: "membership") {
            column(name: "order_id", type: "bigint")
        }
        createIndex(indexName: "FKB01D87D69195D0E", tableName: "membership") {
            column(name: "order_id")
        }

        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "membership", constraintName: "FKB01D87D69195D0E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "order", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-1") {
        createTable(tableName: "facility_form_templates") {
            column(name: "form_template_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-2") {
        createTable(tableName: "form") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "formPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "active_from", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "active_to", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "hash", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "related_form_template_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-3") {
        createTable(tableName: "form_field") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "form_fieldPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "form_id", type: "bigint")

            column(name: "help_text", type: "varchar(255)")

            column(name: "is_required", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "template_id", type: "bigint")

            column(name: "type", type: "varchar(9)") {
                constraints(nullable: "false")
            }

            column(name: "template_fields_idx", type: "integer")

            column(name: "fields_idx", type: "integer")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-4") {
        createTable(tableName: "form_field_value") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "form_field_vaPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "field_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "submission_id", type: "bigint")

            column(name: "value", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "predefined_values_idx", type: "integer")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-5") {
        createTable(tableName: "form_template") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "form_templatePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-6") {
        createTable(tableName: "submission") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "submissionPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "form_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-7") {
        addColumn(tableName: "facility") {
            column(name: "is_dynamic_forms_engine_enabled", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-9") {
        addPrimaryKey(columnNames: "facility_id, form_template_id", tableName: "facility_form_templates")
    }


    changeSet(author: "ami (generated)", id: "1414755959574-34") {
        createIndex(indexName: "FKA90E2A9A67025C04", tableName: "facility_form_templates") {
            column(name: "form_template_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-35") {
        createIndex(indexName: "FKA90E2A9ADDF0EBF7", tableName: "facility_form_templates") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-36") {
        createIndex(indexName: "FK300CC43D389D98", tableName: "form") {
            column(name: "related_form_template_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-37") {
        createIndex(indexName: "FK300CC4DDF0EBF7", tableName: "form") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-38") {
        createIndex(indexName: "hash_uniq_1414755943373", tableName: "form", unique: "true") {
            column(name: "hash")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-39") {
        createIndex(indexName: "FKF276DBF2AB65EDB", tableName: "form_field") {
            column(name: "form_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-40") {
        createIndex(indexName: "FKF276DBF9346F6FF", tableName: "form_field") {
            column(name: "template_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-41") {
        createIndex(indexName: "FKFDB0D6F14D8A3E15", tableName: "form_field_value") {
            column(name: "field_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-42") {
        createIndex(indexName: "FKFDB0D6F15ADE751B", tableName: "form_field_value") {
            column(name: "submission_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-44") {
        createIndex(indexName: "FK84363B4C19A51177", tableName: "submission") {
            column(name: "user_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-45") {
        createIndex(indexName: "FK84363B4C2AB65EDB", tableName: "submission") {
            column(name: "form_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1414755959574-22") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_form_templates", constraintName: "FKA90E2A9ADDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-23") {
        addForeignKeyConstraint(baseColumnNames: "form_template_id", baseTableName: "facility_form_templates", constraintName: "FKA90E2A9A67025C04", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form_template", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-24") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "form", constraintName: "FK300CC4DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-25") {
        addForeignKeyConstraint(baseColumnNames: "related_form_template_id", baseTableName: "form", constraintName: "FK300CC43D389D98", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form_template", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-26") {
        addForeignKeyConstraint(baseColumnNames: "form_id", baseTableName: "form_field", constraintName: "FKF276DBF2AB65EDB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-27") {
        addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "form_field", constraintName: "FKF276DBF9346F6FF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form_template", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-28") {
        addForeignKeyConstraint(baseColumnNames: "field_id", baseTableName: "form_field_value", constraintName: "FKFDB0D6F14D8A3E15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form_field", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-29") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "form_field_value", constraintName: "FKFDB0D6F15ADE751B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "submission", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-31") {
        addForeignKeyConstraint(baseColumnNames: "form_id", baseTableName: "submission", constraintName: "FK84363B4C2AB65EDB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1414755959574-32") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "submission", constraintName: "FK84363B4C19A51177", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1415313149957-1") {
        addColumn(tableName: "form_field_value") {
            column(name: "label", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-1") {
        createTable(tableName: "submission_value") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "submission_vaPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "field_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "submission_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-2") {
        dropForeignKeyConstraint(baseTableName: "form_field_value", constraintName: "FKFDB0D6F15ADE751B")
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-3") {
        createIndex(indexName: "FK708086BE4D8A3E15", tableName: "submission_value") {
            column(name: "field_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-4") {
        createIndex(indexName: "FK708086BE5ADE751B", tableName: "submission_value") {
            column(name: "submission_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-5") {
        dropColumn(columnName: "submission_id", tableName: "form_field_value")
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-6") {
        addForeignKeyConstraint(baseColumnNames: "field_id", baseTableName: "submission_value", constraintName: "FK708086BE4D8A3E15", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form_field", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1415740277994-7") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "submission_value", constraintName: "FK708086BE5ADE751B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "submission", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1416004079046-1") {
        dropColumn(columnName: "last_updated", tableName: "submission")
    }

    changeSet(author: "sergei (generated)", id: "1415449062045-1") {
        addColumn(tableName: "membership_type") {
            column(name: "available_online", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias", id: "1415449062046-1") {
        sql('''update facility_property set key_name = "FEATURE_SUBSCRIPTION_REMINDER" where key_name = "SUBSCRIPTION_REMINDER";''')
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-1") {
        createTable(tableName: "coupon_price") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "coupon_pricePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "coupon_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "customer_category_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1416677295137-2") {
        sql('insert into coupon_price (version, coupon_id, customer_category_id, price) select 0, c.id, cc.id, c.price from coupon c, price_list_customer_category cc where c.facility_id = cc.facility_id')
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-3") {
        createIndex(indexName: "FK7A69B7F086086D9C", tableName: "coupon_price") {
            column(name: "customer_category_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-4") {
        createIndex(indexName: "FK7A69B7F0C973F405", tableName: "coupon_price") {
            column(name: "coupon_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-5") {
        dropColumn(columnName: "price", tableName: "coupon")
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-6") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "coupon_price", constraintName: "FK7A69B7F0C973F405", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "coupon", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1416677295137-7") {
        addForeignKeyConstraint(baseColumnNames: "customer_category_id", baseTableName: "coupon_price", constraintName: "FK7A69B7F086086D9C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "price_list_customer_category", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1418935928860-1") {
        addColumn(tableName: "facility") {
            column(name: "facebook", type: "varchar(255)") {
                constraints(nullable: "true")
            }
            column(name: "twitter", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sergei", id: "1416677295138-1") {
        createIndex(indexName: "payment_date_delivery_idx", tableName: "payment") {
            column(name: "date_delivery")
        }
    }

    changeSet(author: "sergei", id: "1416677295138-2") {
        createIndex(indexName: "order_date_delivery_idx", tableName: "order") {
            column(name: "date_delivery")
        }
    }

    changeSet(author: "sergei", id: "1416677295138-3") {
        createIndex(indexName: "payment_order_parameters_idx", tableName: "payment_order_parameters") {
            column(name: "order_parameters")
        }
    }

    changeSet(author: "sergei", id: "1416677295138-4") {
        createIndex(indexName: "payment_order_order_parameters_idx", tableName: "payment_order_order_parameters") {
            column(name: "order_parameters")
        }
    }

    changeSet(author: "sergei", id: "1419083281538-1") {
        dropIndex(indexName: "order_date_delivery_idx", tableName: "order")
    }

    changeSet(author: "sergei", id: "1419083281538-2") {
        createIndex(indexName: "order_date_delivery_idx", tableName: "order") {
            column(name: "date_delivery")
            column(name: "status")
        }
    }

    changeSet(author: "sergei (generated)", id: "1419377924236-1") {
        createTable(tableName: "user_message") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_messagePK")
            }

            column(name: "date_created", type: "datetime")

            column(name: "from_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "marked_as_read", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "to_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1419377924236-2") {
        createIndex(indexName: "FK93DA97932900B0C7", tableName: "user_message") {
            column(name: "to_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1419377924236-3") {
        createIndex(indexName: "FK93DA9793FEFD6538", tableName: "user_message") {
            column(name: "from_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1419377924236-4") {
        addForeignKeyConstraint(baseColumnNames: "from_id", baseTableName: "user_message", constraintName: "FK93DA9793FEFD6538", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1419377924236-5") {
        addForeignKeyConstraint(baseColumnNames: "to_id", baseTableName: "user_message", constraintName: "FK93DA97932900B0C7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1421479222401-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "number", tableName: "invoice")
    }

    changeSet(author: "sergei (generated)", id: "1420572534271-1") {
        addColumn(tableName: "facility") {
            column(name: "website", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1421352492322-1") {
        addColumn(tableName: "activity") {
            column(name: "online_by_default", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1421352492322-2") {
        addColumn(tableName: "activity_occasion") {
            column(name: "available_online", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1421963125890-1") {
        addColumn(tableName: "facility") {
            column(name: "opening_hours_type", type: "varchar(255)", defaultValue: "OPENING_HOURS") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1422059664344-1") {
        modifyDataType(columnName: "type", newDataType: "varchar(255)", tableName: "form_field")
    }

    changeSet(author: "sergei", id: "1422059664344-2") {
        sql('rename table submission_value to submission_field')
    }

    changeSet(author: "sergei", id: "1422059664344-3") {
        createTable(tableName: "submission_value") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "submission_vPK")
            }

            column(name: "submission_field_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "input", type: "varchar(255)")

            column(name: "value", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "values_idx", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1422059664344-4") {
        createIndex(indexName: "FK708086BE10B1D4E0", tableName: "submission_value") {
            column(name: "submission_field_id")
        }
    }

    changeSet(author: "sergei", id: "1422059664344-5") {
        addForeignKeyConstraint(baseColumnNames: "submission_field_id", baseTableName: "submission_value", constraintName: "FK708086BE10B1D4E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "submission_field", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1422059664344-6") {
        sql('insert into submission_value (submission_field_id, value, values_idx) select id, value, 0 from submission_field')
    }

    changeSet(author: "sergei", id: "1422059664344-7") {
        dropColumn(columnName: "version", tableName: "submission")
    }

    changeSet(author: "sergei", id: "1422059664344-8") {
        dropColumn(columnName: "version", tableName: "submission_field")
    }

    changeSet(author: "sergei", id: "1422059664344-9") {
        dropColumn(columnName: "value", tableName: "submission_field")
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-1") {
        addColumn(tableName: "form") {
            column(name: "max_submissions", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-2") {
        addColumn(tableName: "form") {
            column(name: "membership_required", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-3") {
        addColumn(tableName: "form") {
            column(name: "payment_required", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-4") {
        createTable(tableName: "form_price") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "form_pricePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "customer_category_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "form_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-5") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "type", tableName: "form_field")
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-6") {
        createIndex(indexName: "FKFB87E0E2AB65EDB", tableName: "form_price") {
            column(name: "form_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-7") {
        createIndex(indexName: "FKFB87E0E86086D9C", tableName: "form_price") {
            column(name: "customer_category_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-8") {
        addForeignKeyConstraint(baseColumnNames: "customer_category_id", baseTableName: "form_price", constraintName: "FKFB87E0E86086D9C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "price_list_customer_category", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1422402744714-9") {
        addForeignKeyConstraint(baseColumnNames: "form_id", baseTableName: "form_price", constraintName: "FKFB87E0E2AB65EDB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422614498758-1") {
        addColumn(tableName: "form") {
            column(name: "description", type: "longtext")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422746111490-1") {
        addColumn(tableName: "submission") {
            column(name: "order_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422746111490-2") {
        createIndex(indexName: "FK84363B4C9195D0E", tableName: "submission") {
            column(name: "order_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422746111490-3") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "submission", constraintName: "FK84363B4C9195D0E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "order", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422684258073-1") {
        createTable(tableName: "trainer") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trainerPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "longtext")

            column(name: "email", type: "varchar(255)")

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "first_name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "is_active", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "last_name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "phone", type: "varchar(255)")

            column(name: "profile_image_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "sport_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1422684258073-27") {
        createIndex(indexName: "FKC0639CB519A51177", tableName: "trainer") {
            column(name: "user_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1422684258073-28") {
        createIndex(indexName: "FKC0639CB5B523E1", tableName: "trainer") {
            column(name: "profile_image_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1422684258073-29") {
        createIndex(indexName: "FKC0639CB5BD84893D", tableName: "trainer") {
            column(name: "sport_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1422684258073-30") {
        createIndex(indexName: "FKC0639CB5DDF0EBF7", tableName: "trainer") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1422684258073-17") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "trainer", constraintName: "FKC0639CB5DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422684258073-18") {
        addForeignKeyConstraint(baseColumnNames: "profile_image_id", baseTableName: "trainer", constraintName: "FKC0639CB5B523E1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "mfile", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422684258073-19") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "trainer", constraintName: "FKC0639CB5BD84893D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sport", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422684258073-20") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "trainer", constraintName: "FKC0639CB519A51177", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1422725694538-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "profile_image_id", tableName: "trainer")
    }
    changeSet(author: "ami (generated)", id: "1413720219680-1") {
        createTable(tableName: "scheduled_task") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "scheduled_tasPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "domain_class", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "domain_identifier", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "error_string", type: "varchar(255)")

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "is_task_finished", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "is_task_report_read", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1413720219680-10") {
        createIndex(indexName: "FK239391D7DDF0EBF7", tableName: "scheduled_task") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1413720219680-2") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "scheduled_task", constraintName: "FK239391D7DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1413720219680-3") {
        renameColumn(columnDataType: "varchar(255)", newColumnName: "related_domain_class", oldColumnName: "domain_class", tableName: "scheduled_task")
    }

    changeSet(author: "mattias (generated)", id: "1422920993696-1") {
        addColumn(tableName: "contact_me") {
            column(name: "name", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422924627734-1") {
        addColumn(tableName: "form_field") {
            column(name: "field_text", type: "longtext")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422924627734-2") {
        addColumn(tableName: "form_field") {
            column(name: "max_value", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1422924627734-3") {
        addColumn(tableName: "form_field") {
            column(name: "min_value", type: "varchar(255)")
        }
    }

    changeSet(author: "mattias (generated)", id: "1423133553285-1") {
        addColumn(tableName: "user") {
            column(name: "welcome_image_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1423142155544-1") {
        addColumn(tableName: "form_field") {
            column(name: "is_editable", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1423614290431-1") {
        createTable(tableName: "user_facility") {
            column(name: "user_facilities_id", type: "bigint")
            column(name: "facility_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1423614290431-2") {
        createIndex(indexName: "FK20E1C8979B88958D", tableName: "user_facility") {
            column(name: "user_facilities_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1423614290431-3") {
        createIndex(indexName: "FK20E1C897DDF0EBF7", tableName: "user_facility") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1423614290431-4") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "user_facility", constraintName: "FK20E1C897DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1423614290431-5") {
        addForeignKeyConstraint(baseColumnNames: "user_facilities_id", baseTableName: "user_facility", constraintName: "FK20E1C8979B88958D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1423614290431-6") {
        sql('insert into user_facility (user_facilities_id, facility_id) select id, facility_id from user where facility_id is not null')
    }

    changeSet(author: "sergei (generated)", id: "1423703174114-1") {
        createTable(tableName: "course") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "coursePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "longtext")

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1423703174114-87") {
        createIndex(indexName: "FKAF42E01BDDF0EBF7", tableName: "course") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1423703174114-66") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "course", constraintName: "FKAF42E01BDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }


    changeSet(author: "ami (generated)", id: "1423774462113-4") {
        addColumn(tableName: "subscription") {
            column(name: "court_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1423774462113-56") {
        createIndex(indexName: "FK1456591D13E6BDDD", tableName: "subscription") {
            column(name: "court_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1423774462113-44") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "subscription", constraintName: "FK1456591D13E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami", id: "1423774462113-45") {
        sql('update subscription s set s.court_id = (select crt.id from court crt inner join slot sl on sl.court_id = crt.id where sl.subscription_id = s.id limit 1)')
    }

    changeSet(author: "ami", id: "1423774462113-46") {
        sql('delete from subscription where court_id is null')
    }

    changeSet(author: "ami (generated)", id: "1423775414922-2") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "court_id", tableName: "subscription")
    }

    changeSet(author: "sergei", id: "1423864615204-1") {
        sql("update facility_contract set variable_coupon_mediation_fee = variable_mediation_fee where coupon_contract_type = 'PER_TICKET' or variable_coupon_mediation_fee is null")
    }

    changeSet(author: "sergei (generated)", id: "1423864615204-2") {
        addNotNullConstraint(columnDataType: "integer", columnName: "variable_coupon_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1423864615204-3") {
        addColumn(tableName: "facility_contract") {
            column(name: "variable_unlimited_coupon_mediation_fee", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1423864615204-4") {
        sql('update facility_contract set variable_unlimited_coupon_mediation_fee = variable_coupon_mediation_fee')
    }

    changeSet(author: "sergei (generated)", id: "1423864615204-5") {
        addNotNullConstraint(columnDataType: "integer", columnName: "variable_unlimited_coupon_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1423864615204-6") {
        addColumn(tableName: "facility_contract") {
            column(name: "unlimited_coupon_contract_type", type: "varchar(255)", defaultValue: "PER_TICKET") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1423864615204-7") {
        sql('update facility_contract set unlimited_coupon_contract_type = coupon_contract_type')
    }
    changeSet(author: "dles (generated)", id: "1424164484942-2") {
        addColumn(tableName: "course") {
            column(name: "end_date", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "dles (generated)", id: "1424164484942-3") {
        addColumn(tableName: "course") {
            column(name: "start_date", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "dles (generated)", id: "1424346150386-2") {
        addColumn(tableName: "course") {
            column(name: "form_id", type: "bigint")
        }
    }
    changeSet(author: "dles (generated)", id: "1424346150386-16") {
        createIndex(indexName: "FKAF42E01B2AB65EDB", tableName: "course") {
            column(name: "form_id")
        }
    }
    changeSet(author: "dles (generated)", id: "1424346150386-6") {
        addForeignKeyConstraint(baseColumnNames: "form_id", baseTableName: "course", constraintName: "FKAF42E01B2AB65EDB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1424346150389-0") {
        dropForeignKeyConstraint(baseTableName: "submission", constraintName: "FK84363B4C19A51177")
    }

    changeSet(author: "ami (generated)", id: "1424346150389-1") {
        renameColumn(columnDataType: "bigint", newColumnName: "submission_issuer_id", oldColumnName: "user_id", tableName: "submission")
    }

    changeSet(author: "ami (generated)", id: "1424346150389-2") {
        addColumn(tableName: "submission") {
            column(name: "customer_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1424346150389-3") {
        createIndex(indexName: "FK84363B4CF6898296", tableName: "submission") {
            column(name: "submission_issuer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1424346150389-4") {
        createIndex(indexName: "FKAF42E01B2AB65EDM", tableName: "submission") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1424346150389-5") {
        addForeignKeyConstraint(baseColumnNames: "submission_issuer_id", baseTableName: "submission", constraintName: "FK84363B4CF6898296", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1424346150389-6") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "submission", constraintName: "FKAF42E01B2AB65EDM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1424476562556-1") {
        addColumn(tableName: "subscription") {
            column(name: "reminder_enabled", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1424546801148-1") {
        createTable(tableName: "global_notification") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "global_notifiPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "notification_text", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "publish_date", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1424546801148-2") {
        createIndex(indexName: "publish_end_date_idx", tableName: "global_notification") {
            column(name: "publish_date")
            column(name: "end_date")
        }
    }
    changeSet(author: "dles (generated)", id: "1424429136916-2") {
        addColumn(tableName: "form_field") {
            column(name: "is_active", type: "bit") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "dles", id: "1424429136916-3") {
        sql('update form_field set is_active = true')
    }

    changeSet(author: "dles", id: "1408521373690-4") {
        addNotNullConstraint(columnDataType: "bit", columnName: "is_active", tableName: "form_field")
    }

    changeSet(author: "mattias (generated)", id: "1424788570638-2") {
        addColumn(tableName: "global_notification") {
            column(name: "title", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-1") {
        createTable(tableName: "course_participant") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "course_particPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "course_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-2") {
        createIndex(indexName: "FKB26EDD0F5800592D", tableName: "course_participant") {
            column(name: "course_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-3") {
        createIndex(indexName: "FKB26EDD0F9AE61E17", tableName: "course_participant") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-4") {
        createIndex(indexName: "unique_customer_id", tableName: "course_participant", unique: "true") {
            column(name: "course_id")
            column(name: "customer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-5") {
        addForeignKeyConstraint(baseColumnNames: "course_id", baseTableName: "course_participant", constraintName: "FKB26EDD0F5800592D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "course", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1424824624289-6") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "course_participant", constraintName: "FKB26EDD0F9AE61E17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }
    changeSet(author: "koloritnij (generated)", id: "1425070755946-2") {
        addColumn(tableName: "course") {
            column(name: "trainer_id", type: "bigint")
        }
    }
    changeSet(author: "koloritnij (generated)", id: "1425070755946-15") {
        createIndex(indexName: "FKAF42E01B22D3C2A7", tableName: "course") {
            column(name: "trainer_id")
        }
    }
    changeSet(author: "koloritnij (generated)", id: "1425070755946-6") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "course", constraintName: "FKAF42E01B22D3C2A7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias", id: "1425070755947-1") {
        sql('update sport_profile set skill_level = 0 where skill_level < 0')
    }

    changeSet(author: "ami (generated)", id: "1425635612912-1") {
        renameTable(oldTableName: 'course_participant', newTableName: 'participant')
        renameTable(oldTableName: 'activity_slot_condition_activity', newTableName: 'activity_slot_condition_class_activity')
    }

    changeSet(author: "ami (generated)", id: "1425635612912-2") {
        sql("delete from participant")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-1") {
        createTable(tableName: "activity_occasion_participants") {
            column(name: "activity_occasion_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "participant_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-3") {
        addColumn(tableName: "activity") {
            column(name: "class", type: "varchar(255)")
        }
        sql("update activity set class = 'class_activity'")

    }

    changeSet(author: "ami (generated)", id: "1425638689892-3x") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "class", tableName: "activity")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-4") {
        addColumn(tableName: "activity") {
            column(name: "end_date", type: "datetime")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-5") {
        addColumn(tableName: "activity") {
            column(name: "form_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-6") {
        addColumn(tableName: "activity") {
            column(name: "start_date", type: "datetime")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-7") {
        addColumn(tableName: "activity") {
            column(name: "trainer_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-8") {
        addColumn(tableName: "participant") {
            column(name: "activity_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-10") {
        dropNotNullConstraint(columnDataType: "bit", columnName: "archived", tableName: "activity")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-12") {
        dropNotNullConstraint(columnDataType: "bit", columnName: "online_by_default", tableName: "activity")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-13") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "activity_slot_condition_activities_id", tableName: "activity_slot_condition_class_activity")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-15") {
        addPrimaryKey(columnNames: "activity_occasion_id, participant_id", tableName: "activity_occasion_participants")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-18") {
        dropForeignKeyConstraint(baseTableName: "course", constraintName: "FKAF42E01BDDF0EBF7")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-19") {
        dropForeignKeyConstraint(baseTableName: "course", constraintName: "FKAF42E01B2AB65EDB")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-20") {
        dropForeignKeyConstraint(baseTableName: "course", constraintName: "FKAF42E01B22D3C2A7")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-21") {
        dropForeignKeyConstraint(baseTableName: "participant", constraintName: "FKB26EDD0F5800592D")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-44") {
        dropIndex(indexName: "unique_customer_id", tableName: "participant")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-49") {
        createIndex(indexName: "FK9D4BF30F1AF23500", tableName: "activity") {
            column(name: "trainer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-50") {
        createIndex(indexName: "FK9D4BF30F2AB65EDB", tableName: "activity") {
            column(name: "form_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-51") {
        createIndex(indexName: "FKFB5094C81071833D", tableName: "activity_occasion_participants") {
            column(name: "activity_occasion_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-52") {
        createIndex(indexName: "FKFB5094C8D1041E76", tableName: "activity_occasion_participants") {
            column(name: "participant_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-55") {
        createIndex(indexName: "FK2DBDEF33BF3CB3E", tableName: "participant") {
            column(name: "activity_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-56") {
        createIndex(indexName: "unique_customer_id", tableName: "participant", unique: "true") {
            column(name: "activity_id")

            column(name: "customer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425638689892-58") {
        dropColumn(columnName: "course_id", tableName: "participant")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-59") {
        dropTable(tableName: "course")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-21") {
        dropForeignKeyConstraint(baseTableName: "activity", constraintName: "FK9D4BF30FDDF0EBF7")
        dropIndex(tableName: "activity", indexName: "FK9D4BF30FDDF0EBF7")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-22") {
        dropForeignKeyConstraint(baseTableName: "activity", constraintName: "FK9D4BF30F5BCD968F")
        dropIndex(tableName: "activity", indexName: "FK9D4BF30F5BCD968F")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-23") {
        dropForeignKeyConstraint(baseTableName: "activity_occasion", constraintName: "FK61502A17BF3CB3E")
        dropIndex(tableName: "activity_occasion", indexName: "FK61502A17BF3CB3E")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-24") {
        dropForeignKeyConstraint(baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2311071833D")
        dropIndex(tableName: "activity_occasion_booking", indexName: "FK2E16D2311071833D")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-25") {
        dropForeignKeyConstraint(baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2319821AE5D")
        dropIndex(tableName: "activity_occasion_booking", indexName: "FK2E16D2319821AE5D")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-26") {
        dropForeignKeyConstraint(baseTableName: "activity_slot_condition_class_activity", constraintName: "FK28713064BF3CB3E")
        dropIndex(tableName: "activity_slot_condition_class_activity", indexName: "FK28713064BF3CB3E")
    }

    changeSet(author: "ami (generated)", id: "14256386898782-27") {
        dropForeignKeyConstraint(baseTableName: "activity_slot_condition_class_activity", constraintName: "FK28713064C1105B8D")
        dropIndex(tableName: "activity_slot_condition_class_activity", indexName: "FK28713064C1105B8D")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-22") {
        createIndex(indexName: "FK9D4BF30FDDF0EBF7", tableName: "activity") {
            column(name: "facility_id")
        }
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "activity", constraintName: "FK9D4BF30FDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-23") {
        addForeignKeyConstraint(baseColumnNames: "form_id", baseTableName: "activity", constraintName: "FK9D4BF30F2AB65EDB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-24") {
        createIndex(indexName: "FK9D4BF30F5BCD968F", tableName: "activity") {
            column(name: "large_image_id")
        }
        addForeignKeyConstraint(baseColumnNames: "large_image_id", baseTableName: "activity", constraintName: "FK9D4BF30F5BCD968F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "mfile", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-25") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "activity", constraintName: "FK9D4BF30F1AF23500", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-26") {
        createIndex(indexName: "FK61502A17BF3CB3E", tableName: "activity_occasion") {
            column(name: "activity_id")
        }
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "activity_occasion", constraintName: "FK61502A17BF3CB3E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-27") {
        createIndex(indexName: "FK2E16D2311071833D", tableName: "activity_occasion_booking") {
            column(name: "activity_occasion_id")
        }
        addForeignKeyConstraint(baseColumnNames: "activity_occasion_id", baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2311071833D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity_occasion", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-28") {
        createIndex(indexName: "FK2E16D2319821AE5D", tableName: "activity_occasion_booking") {
            column(name: "booking_id")
        }
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "activity_occasion_booking", constraintName: "FK2E16D2319821AE5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-29") {
        addForeignKeyConstraint(baseColumnNames: "activity_occasion_id", baseTableName: "activity_occasion_participants", constraintName: "FKFB5094C81071833D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity_occasion", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-30") {
        addForeignKeyConstraint(baseColumnNames: "participant_id", baseTableName: "activity_occasion_participants", constraintName: "FKFB5094C8D1041E76", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "participant", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-31") {
        createIndex(indexName: "FK28713064BF3CB3E", tableName: "activity_slot_condition_class_activity") {
            column(name: "activity_id")
        }
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "activity_slot_condition_class_activity", constraintName: "FK28713064BF3CB3E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-32") {
        createIndex(indexName: "FK28713064C1105B8D", tableName: "activity_slot_condition_class_activity") {
            column(name: "activity_slot_condition_activities_id")
        }
        addForeignKeyConstraint(baseColumnNames: "activity_slot_condition_activities_id", baseTableName: "activity_slot_condition_class_activity", constraintName: "FK28713064C1105B8D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "slot_condition", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425638689892-40") {
        addForeignKeyConstraint(baseColumnNames: "activity_id", baseTableName: "participant", constraintName: "FK2DBDEF33BF3CB3E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1425775990870-1") {
        addColumn(tableName: "facility") {
            column(name: "show_booking_holder", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1425953080278-1") {
        addColumn(tableName: "scheduled_task") {
            column(name: "result_file_name", type: "varchar(100)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1425953080278-2") {
        addColumn(tableName: "scheduled_task") {
            column(name: "result_file_path", type: "varchar(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1425980869542-2") {
        addColumn(tableName: "activity_occasion") {
            column(name: "court_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1425980869542-3") {
        addColumn(tableName: "activity_occasion") {
            column(name: "trainer_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1425980869542-22") {
        createIndex(indexName: "FK61502A1713E6BDDD", tableName: "activity_occasion") {
            column(name: "court_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425980869542-23") {
        createIndex(indexName: "FK61502A171AF23500", tableName: "activity_occasion") {
            column(name: "trainer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1425980869542-7") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "activity_occasion", constraintName: "FK61502A1713E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425980869542-8") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "activity_occasion", constraintName: "FK61502A171AF23500", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1425980869542-9") {
        dropNotNullConstraint(columnDataType: "bit", columnName: "available_online", tableName: "activity_occasion")
    }

    changeSet(author: "ami (generated)", id: "1425980869542-10") {
        dropNotNullConstraint(columnDataType: "INT", columnName: "price", tableName: "activity_occasion")
    }

    changeSet(author: "ami (generated)", id: "1425980869542-11") {
        dropNotNullConstraint(columnDataType: "INT", columnName: "max_num_participants", tableName: "activity_occasion")
    }

    changeSet(author: "mattias", id: "1425980869543-1") {
        sql('update sport_profile_mindset set badge_color = "warning" where name = "FOR_FUN"')
        sql('update sport_profile_mindset set badge_color = "success" where name = "TRAINING"')
        sql('update sport_profile_mindset set badge_color = "primary" where name = "MATCH"')
    }

    changeSet(author: "sergei (generated)", id: "1426363751415-1") {
        addColumn(tableName: "form_field_value") {
            column(name: "max_value", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1426363751415-2") {
        addColumn(tableName: "form_field_value") {
            column(name: "min_value", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1426363751415-3") {
        sql("insert into form_field_value (version, field_id, value, predefined_values_idx, min_value, max_value) select 0, id, label, 0, min_value, max_value from form_field where type = 'TIMERANGE_CHECKBOX'")
    }

    changeSet(author: "sergei (generated)", id: "1426363751415-4") {
        dropColumn(columnName: "max_value", tableName: "form_field")
    }

    changeSet(author: "sergei (generated)", id: "1426363751415-5") {
        dropColumn(columnName: "min_value", tableName: "form_field")
    }

    changeSet(author: "sergei (generated)", id: "1426363751415-6") {
        addColumn(tableName: "submission_value") {
            column(name: "input_group", type: "varchar(255)")
        }
    }
    changeSet(author: "mattias (generated)", id: "1426510997352-2") {
        addColumn(tableName: "activity") {
            column(name: "hint_color", type: "varchar(255)")
        }

    }
    changeSet(author: "mattias (generated)", id: "1426510997352-3") {
        sql('update activity set hint_color = "BLUE" where class = "course_activity" and hint_color is null')
    }

    changeSet(author: "sergei (generated)", id: "1426542555214-1") {
        dropColumn(columnName: "label", tableName: "form_field_value")
    }

    changeSet(author: "mattias", id: "1426542555214-1") {
        sql('update facility_property set key_name = "FEATURE_MEMBERSHIP_REQUEST_PAYMENT" where key_name = "MEMBERSHIP_REQUEST_PAYMENT"')
    }

    changeSet(author: "ami (generated)", id: "1427200392361-1") {
        createTable(tableName: "activity_occasion_trainer") {
            column(name: "activity_occasion_trainers_id", type: "bigint")

            column(name: "trainer_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1427200392361-12") {
        dropForeignKeyConstraint(baseTableName: "activity_occasion", constraintName: "FK61502A171AF23500")
    }


    changeSet(author: "ami (generated)", id: "1427200392361-41") {
        createIndex(indexName: "FKEA9F668D1AF23500", tableName: "activity_occasion_trainer") {
            column(name: "trainer_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1427200392361-42") {
        createIndex(indexName: "FKEA9F668DC913176E", tableName: "activity_occasion_trainer") {
            column(name: "activity_occasion_trainers_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1427200392361-48") {
        dropColumn(columnName: "trainer_id", tableName: "activity_occasion")
    }

    changeSet(author: "ami (generated)", id: "1427200392361-18") {
        addForeignKeyConstraint(baseColumnNames: "activity_occasion_trainers_id", baseTableName: "activity_occasion_trainer", constraintName: "FKEA9F668DC913176E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity_occasion", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1427200392361-19") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "activity_occasion_trainer", constraintName: "FKEA9F668D1AF23500", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "calle (generated)", id: "1430221711743-2") {
        addColumn(tableName: "facility_contract") {
            column(name: "variable_text_message_fee", type: "integer")
        }
    }

    changeSet(author: "dles (generated)", id: "1427283172246-2") {
        addColumn(tableName: "facility") {
            column(name: "whether_to_send_email_confirmation_by_default", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "dles", id: "1427283172246-3") {
        sql('update facility set whether_to_send_email_confirmation_by_default = true')
    }

    changeSet(author: "sergei", id: "1427243931461-1") {
        sql('update payment p set p.coupon_id = null where p.coupon_id is not null and not exists (select id from customer_coupon cc where cc.id = p.coupon_id)')
    }

    changeSet(author: "sergei (generated)", id: "1427243931461-2") {
        modifyDataType(columnName: "coupon_id", newDataType: "bigint", tableName: "payment")
    }

    changeSet(author: "sergei (generated)", id: "1427243931461-3") {
        createIndex(indexName: "FKD11C32066A3B4683", tableName: "payment") {
            column(name: "coupon_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1427243931461-4") {
        addForeignKeyConstraint(baseColumnNames: "coupon_id", baseTableName: "payment", constraintName: "FKD11C32066A3B4683", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer_coupon", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1427488864110-1") {
        sql("delete from participant where activity_id in (select id from activity where class = 'course_activity' and form_id is null)")
        sql("delete from activity_occasion where activity_id in (select id from activity where class = 'course_activity' and form_id is null)")
        sql("delete from activity where class = 'course_activity' and form_id is null")
    }

    changeSet(author: "dles (generated)", id: "1427790272459-2") {
        addColumn(tableName: "coupon") {
            column(name: "is_archived", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "dles (generated)", id: "1427790272459-3") {
        sql('update coupon set is_archived = false')
    }

    changeSet(author: "dles", id: "1427790272459-5") {
        renameColumn(columnDataType: "bit", newColumnName: "archived", oldColumnName: "is_archived", tableName: "coupon")
    }

    changeSet(author: "ami (generated)", id: "1427883340688-2") {
        dropIndex(indexName: "hash_uniq_1414755943373", tableName: "form")
    }

    changeSet(author: "ami (generated)", id: "1427883340688-3") {
        addColumn(tableName: "form") {
            column(name: "archived", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1427933769741-1") {
        addColumn(tableName: "form_field_value") {
            column(name: "is_active", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1428522093007-2") {
        addColumn(tableName: "order_payment") {
            column(name: "invoice_row_id", type: "bigint")
        }
    }

    changeSet(author: "mattias (generated)", id: "1428522093007-28") {
        createIndex(indexName: "FKC66032159467509D", tableName: "order_payment") {
            column(name: "invoice_row_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1428522093007-19") {
        addForeignKeyConstraint(baseColumnNames: "invoice_row_id", baseTableName: "order_payment", constraintName: "FKC66032159467509D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "invoice_row", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1429318026807-1") {
        addColumn(tableName: "facility") {
            column(name: "related_bookings_customer_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1429318026807-2") {
        createIndex(indexName: "FK1DDE6EA3CE6ED026", tableName: "facility") {
            column(name: "related_bookings_customer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1429318026807-3") {
        addForeignKeyConstraint(baseColumnNames: "related_bookings_customer_id", baseTableName: "facility", constraintName: "FK1DDE6EA3CE6ED026", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1429318026807-4") {
        sql('update facility set related_bookings_customer_id = default_booking_customer_id')
    }

    changeSet(author: "sergei (generated)", id: "1429663538721-1") {
        addColumn(tableName: "trainer") {
            column(name: "show_online", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-1") {
        addColumn(tableName: "submission_value") {
            column(name: "submission_id", type: "bigint")
        }
    }

    changeSet(author: "sergei", id: "1429827657502-2") {
        sql("update submission_value sv set submission_id = (select sf.submission_id from submission_field sf where sf.id = sv.submission_field_id)")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-3") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "submission_id", tableName: "submission_value")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-4") {
        addColumn(tableName: "submission_value") {
            column(name: "label", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1429827657502-5") {
        sql("update submission_value sv set label = (select ff.label from submission_field sf join form_field ff on ff.id = sf.field_id where sf.id = sv.submission_field_id)")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-6") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "label", tableName: "submission_value")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-7") {
        addColumn(tableName: "submission_value") {
            column(name: "field_id", type: "bigint")
        }
    }

    changeSet(author: "sergei", id: "1429827657502-8") {
        sql("update submission_value sv set field_id = (select sf.field_id from submission_field sf where sf.id = sv.submission_field_id)")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-9") {
        addColumn(tableName: "submission_value") {
            column(name: "field_type", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1429827657502-10") {
        sql("update submission_value sv set field_type = (select ff.type from submission_field sf join form_field ff on ff.id = sf.field_id where sf.id = sv.submission_field_id)")
    }

    changeSet(author: "sergei", id: "1429827657502-11") {
        renameColumn(columnDataType: "integer", newColumnName: "value_index", oldColumnName: "values_idx", tableName: "submission_value")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-12") {
        dropForeignKeyConstraint(baseTableName: "submission_field", constraintName: "FK708086BE4D8A3E15")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-13") {
        dropForeignKeyConstraint(baseTableName: "submission_field", constraintName: "FK708086BE5ADE751B")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-14") {
        dropForeignKeyConstraint(baseTableName: "submission_value", constraintName: "FK708086BE10B1D4E0")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-15") {
        createIndex(indexName: "submission_value_label_idx", tableName: "submission_value") {
            column(name: "label")
        }
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-16") {
        createIndex(indexName: "FK708086BE5ADE751B", tableName: "submission_value") {
            column(name: "submission_id")
        }
    }

    changeSet(author: "sergei", id: "1429827657502-17") {
        sql("delete from submission_value where submission_field_id in (select id from submission_field where submission_id in (select id from submission where form_id in (select id from form where archived = true)))")
        sql("delete from submission_field where submission_id in (select id from submission where form_id in (select id from form where archived = true))")
        sql("delete from submission where form_id in (select id from form where archived = true)")
        sql("delete from form_price where form_id in (select id from form where archived = true)")
        sql("delete from form_field_value where field_id in (select id from form_field where form_id in (select id from form where archived = true))")
        sql("delete from form_field where form_id in (select id from form where archived = true)")
        sql("delete from form where archived = true")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-18") {
        dropColumn(columnName: "submission_field_id", tableName: "submission_value")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-19") {
        dropTable(tableName: "submission_field")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-20") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "submission_value", constraintName: "FK708086BE5ADE751B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "submission", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-21") {
        createIndex(indexName: "hash_uniq_1429993878565", tableName: "form", unique: "true") {
            column(name: "hash")
        }
    }

    changeSet(author: "sergei (generated)", id: "1429827657502-22") {
        dropColumn(columnName: "archived", tableName: "form")
    }

    changeSet(author: "sergei (generated)", id: "1430344661118-1") {
        sql('update invoice_row set description = SUBSTRING(description, 1, 50)')
        modifyDataType(columnName: "description", newDataType: "varchar(50)", tableName: "invoice_row")
    }

    changeSet(author: "sergei (generated)", id: "1430354815635-1") {
        addColumn(tableName: "form") {
            column(name: "price", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1430354815635-2") {
        sql("update form f set price = (select max(price) from form_price fp where fp.form_id = f.id)")
    }

    changeSet(author: "sergei (generated)", id: "1430354815635-3") {
        dropForeignKeyConstraint(baseTableName: "form_price", constraintName: "FKFB87E0E86086D9C")
    }

    changeSet(author: "sergei (generated)", id: "1430354815635-4") {
        dropForeignKeyConstraint(baseTableName: "form_price", constraintName: "FKFB87E0E2AB65EDB")
    }

    changeSet(author: "sergei (generated)", id: "1430354815635-5") {
        dropTable(tableName: "form_price")
    }

    changeSet(author: "sergei (generated)", id: "1430515253010-1") {
        addColumn(tableName: "customer") {
            column(name: "guardian_email2", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1430515253010-2") {
        addColumn(tableName: "customer") {
            column(name: "guardian_name2", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1430515253010-3") {
        addColumn(tableName: "customer") {
            column(name: "guardian_telephone2", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-1") {
        createTable(tableName: "course_activity_trainer") {
            column(name: "course_activity_trainers_id", type: "bigint")
            column(name: "trainer_id", type: "bigint")
        }
    }

    changeSet(author: "sergei", id: "1431124831107-2") {
        sql('insert into course_activity_trainer (course_activity_trainers_id, trainer_id) select id, trainer_id from activity where trainer_id is not null')
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-3") {
        createIndex(indexName: "FKE1E413291AF23500", tableName: "course_activity_trainer") {
            column(name: "trainer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-4") {
        createIndex(indexName: "FKE1E41329AD9E8134", tableName: "course_activity_trainer") {
            column(name: "course_activity_trainers_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-5") {
        dropForeignKeyConstraint(baseTableName: "activity", constraintName: "FK9D4BF30F1AF23500")
        dropColumn(columnName: "trainer_id", tableName: "activity")
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-6") {
        addForeignKeyConstraint(baseColumnNames: "course_activity_trainers_id", baseTableName: "course_activity_trainer", constraintName: "FKE1E41329AD9E8134", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1431124831107-7") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "course_activity_trainer", constraintName: "FKE1E413291AF23500", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias", id: "1431124831108-1") {
        sql('update facility_contract set variable_text_message_fee = 1 where id > 0')
    }

    changeSet(author: "ami (generated)", id: "1431502712537-2") {
        addColumn(tableName: "invoice") {
            column(name: "has_been_credited", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1432405632372-1") {
        addColumn(tableName: "participant") {
            column(name: "submission_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1432405632372-2") {
        createIndex(indexName: "FK2DBDEF335ADE751B", tableName: "participant") {
            column(name: "submission_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1432405632372-3") {
        addForeignKeyConstraint(baseColumnNames: "submission_id", baseTableName: "participant", constraintName: "FK2DBDEF335ADE751B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "submission", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1432405632372-4") {
        sql("update participant p set p.submission_id = (select max(s.id) from submission s join activity a on a.form_id = s.form_id where s.customer_id = p.customer_id and a.id = p.activity_id)")
    }

    changeSet(author: "sergei (generated)", id: "1432405632372-5") {
        createIndex(indexName: "submission_value_type_value_idx", tableName: "submission_value") {
            column(name: "field_type")
            column(name: "value")
        }
    }

    changeSet(author: "mattias", id: "1432405632373-1") {
        sql("alter table user convert to character set utf8 collate utf8_swedish_ci")
        sql("alter table customer convert to character set utf8 collate utf8_swedish_ci")
        sql("update customer c left join user u on u.id = c.user_id set c.email = u.email where u.email != c.email and c.user_id is not null")
    }

    changeSet(author: "ami (generated)", id: "1433975213743-49") {
        dropColumn(columnName: "fortnox_db", tableName: "facility")
    }

    changeSet(author: "ami (generated)", id: "1433975213743-50") {
        dropColumn(columnName: "fortnox_token", tableName: "facility")
    }

    changeSet(author: "mattias (generated)", id: "1437769518329-1") {
        createTable(tableName: "code_request") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "code_requestPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "booking_id", type: "bigint")

            column(name: "code", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "mpc_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1436906831364-1") {
        addColumn(tableName: "user") {
            column(name: "language", type: "varchar(2)", defaultValue: "sv") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1436906831364-2") {
        addColumn(tableName: "facility") {
            column(name: "language", type: "varchar(2)", defaultValue: "sv") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-1") {
        modifyDataType(columnName: "fixed_monthly_fee", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-2") {
        modifyDataType(columnName: "variable_coupon_mediation_fee", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-3") {
        modifyDataType(columnName: "variable_mediation_fee", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-4") {
        modifyDataType(columnName: "variable_mediation_fee_percentage", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-5") {
        modifyDataType(columnName: "variable_text_message_fee", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-6") {
        modifyDataType(columnName: "variable_unlimited_coupon_mediation_fee", newDataType: "decimal(19,2)", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-7") {
        addNotNullConstraint(columnDataType: "decimal(19,2)", columnName: "fixed_monthly_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-8") {
        addNotNullConstraint(columnDataType: "decimal(19,2)", columnName: "variable_coupon_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-9") {
        addNotNullConstraint(columnDataType: "decimal(19,2)", columnName: "variable_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438731580155-10") {
        addNotNullConstraint(columnDataType: "decimal(19,2)", columnName: "variable_unlimited_coupon_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1438891053132-1") {
        addColumn(tableName: "booking") {
            column(name: "hide_booking_holder", type: "bit")
        }
    }

    changeSet(author: "sergei (generated)", id: "1438891053132-2") {
        addColumn(tableName: "user") {
            column(name: "anonymouse_booking", type: "bit")
        }
    }

    changeSet(author: "ami (generated)", id: "1437989421359-4") {
        addColumn(tableName: "coupon") {
            column(name: "class", type: "varchar(255)")
        }
        sql("update coupon set class = 'coupon'")

    }

    changeSet(author: "ami (generated)", id: "1437989421359-4x") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "class", tableName: "coupon")
    }

    changeSet(author: "ami (generated)", id: "1439025765996-2") {
        addColumn(tableName: "facility_contract") {
            column(name: "fixed_gift_card_fee", type: "decimal(19,2)")
        }
    }

    changeSet(author: "ami (generated)", id: "1439324501473-6") {
        addColumn(tableName: "customer_coupon") {
            column(name: "nr_of_tickets", type: "integer")
        }
    }

    changeSet(author: "ami (generated)", id: "1439316435297-7") {
        addColumn(tableName: "customer_coupon_ticket") {
            column(name: "price", type: "decimal(19,2)")
        }
    }

    changeSet(author: "ami (generated)", id: "1439316435297-8") {
        sql("update customer_coupon cc set cc.nr_of_tickets = (select count(*) from customer_coupon_ticket cct where cct.customer_coupon_id = cc.id and cct.consumed is null)")
        sql("delete from customer_coupon_ticket where consumed is null")
    }

    changeSet(author: "ami (generated)", id: "1439316435297-8x") {
        addNotNullConstraint(columnDataType: "integer", columnName: "nr_of_tickets", tableName: "customer_coupon")
    }


    changeSet(author: "sergei (generated)", id: "1439767072867-1") {
        createTable(tableName: "facility_user") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "facility_userPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-2") {
        createTable(tableName: "facility_user_role") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "facility_userPK")
            }

            column(name: "access_right", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "facility_user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-3") {
        createIndex(indexName: "FKAD8DE3E719A51177", tableName: "facility_user") {
            column(name: "user_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-4") {
        createIndex(indexName: "FKAD8DE3E7DDF0EBF7", tableName: "facility_user") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-5") {
        createIndex(indexName: "unique_user_id", tableName: "facility_user", unique: "true") {
            column(name: "facility_id")
            column(name: "user_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-6") {
        createIndex(indexName: "FKAE47ADCEAA0AC01E", tableName: "facility_user_role") {
            column(name: "facility_user_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-7") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_user", constraintName: "FKAD8DE3E7DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-8") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "facility_user", constraintName: "FKAD8DE3E719A51177", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1439767072867-9") {
        addForeignKeyConstraint(baseColumnNames: "facility_user_id", baseTableName: "facility_user_role", constraintName: "FKAE47ADCEAA0AC01E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-1") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "charge_date", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-2") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "charge_end_date", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-3") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "charge_month", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-4") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "charge_start_date", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-5") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "type", type: "varchar(255)", defaultValue: "MONTHLY") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1440967773121-6") {
        addColumn(tableName: "facility_contract") {
            column(name: "mediation_fee_mode", type: "varchar(255)", defaultValue: "OR") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1441320367609-1") {
        createTable(tableName: "player") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "playerPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "booking_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "bigint")

            column(name: "email", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1441320367609-2") {
        createIndex(indexName: "FKC53E9AE19821AE5D", tableName: "player") {
            column(name: "booking_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1441320367609-3") {
        createIndex(indexName: "FKC53E9AE19AE61E17", tableName: "player") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1441320367609-4") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "player", constraintName: "FKC53E9AE19821AE5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1441320367609-5") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "player", constraintName: "FKC53E9AE19AE61E17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "143931643522222-7") {
        addColumn(tableName: "facility") {
            column(name: "currency", type: "varchar(255)", defaultValue: "SEK") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1443828856173-1") {
        addColumn(tableName: "async_mail_mess") {
            column(name: "envelope_from", type: "varchar(256)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1444164813525-1") {
        addColumn(tableName: "customer") {
            column(name: "exlude_from_number_of_bookings_rule", type: "bit")
        }
    }

    changeSet(author: "sergei (generated)", id: "1444164813525-2") {
        dropColumn(columnName: "booking_rule_num_active_bookings_per_user", tableName: "facility")
    }

    changeSet(author: "sergei (generated)", id: "1445118878278-1") {
        addColumn(tableName: "customer") {
            column(name: "date_of_birth", type: "date")
        }
    }

    changeSet(author: "sergei", id: "1445118878278-2") {
        sql('set @old_sql_mode = @@sql_mode')
        sql("set sql_mode = ''")
        sql('''update customer set date_of_birth = str_to_date(substr(security_number, 1, 6), '%y%m%d'), security_number = substr(security_number, 8)
                where date_of_birth is null and (type is null or type != 'COMPANY') and security_number regexp '^[[:digit:]]{6}(-.*)?$' ''')
        sql('''update customer set date_of_birth = str_to_date(substr(security_number, 1, 8), '%Y%m%d'), security_number = substr(security_number, 10)
                where date_of_birth is null and (type is null or type != 'COMPANY') and security_number regexp '^[[:digit:]]{8}(-.*)?$' ''')
        sql('''update customer set date_of_birth = str_to_date(security_number, '%m/%d/%y'), security_number = if (locate('-', security_number) > 0, substr(security_number, locate('-', security_number) + 1), null)
                where date_of_birth is null and (type is null or type != 'COMPANY') and security_number regexp '^[[:digit:]]{1,2}(/[[:digit:]]{1,2}){2}(-.*)?$' ''')
        sql('''update customer set date_of_birth = str_to_date(security_number, '%Y-%m-%d'), security_number = if (locate('-', security_number, 9) > 0, substr(security_number, locate('-', security_number, 9) + 1), null)
                where date_of_birth is null and (type is null or type != 'COMPANY') and security_number regexp '^[[:digit:]]{4}-[[:digit:]]{1,2}-[[:digit:]]{1,2}(-.*)?$' ''')
        sql('''update customer set date_of_birth = date_of_birth - interval 100 year
                where date_of_birth > '2015-12-31' ''')
        sql('set sql_mode = @old_sql_mode')
    }

    changeSet(author: "sergei (generated)", id: "1445118878278-3") {
        addColumn(tableName: "customer") {
            column(name: "org_number", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1445118878278-4") {
        sql('''update customer set org_number = security_number, security_number = null
                where type = 'COMPANY' and org_number is null and security_number is not null''')
    }

    changeSet(author: "sergei (generated)", id: "1446072385479-1") {
        addColumn(tableName: "facility") {
            column(name: "require_security_number", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1446072385479-2") {
        sql("update facility set require_security_number = true where language = 'sv'")
    }

    changeSet(author: "sergei (generated)", id: "1446254545199-1") {
        addColumn(tableName: "activity") {
            column(name: "max_num_participants", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1446254545199-2") {
        addColumn(tableName: "activity") {
            column(name: "price", type: "integer")
        }
    }

    changeSet(author: "ami (generated)", id: "1446020047593-2") {
        createTable(tableName: "organization") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "organizationPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "fortnox_access_token", type: "varchar(255)")

            column(name: "fortnox_auth_code", type: "varchar(255)")

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "number", type: "varchar(255)")
        }
    }

    changeSet(author: "ami (generated)", id: "1446020047593-36") {
        createIndex(indexName: "FK4644ED33DDF0EBF7", tableName: "organization") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1446020047593-26") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "organization", constraintName: "FK4644ED33DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1446394447395-3") {
        addColumn(tableName: "invoice_row") {
            column(name: "organization_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1446394447395-54") {
        createIndex(indexName: "FKCCA3CDC814BFB888", tableName: "invoice_row") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1446394447395-36") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "invoice_row", constraintName: "FKCCA3CDC814BFB888", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1446400005845-3") {
        addColumn(tableName: "invoice") {
            column(name: "organization_id", type: "bigint")
        }
    }

    changeSet(author: "ami (generated)", id: "1446400005845-54") {
        createIndex(indexName: "FK74D6432D14BFB888", tableName: "invoice") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "ami (generated)", id: "1446400005845-36") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "invoice", constraintName: "FK74D6432D14BFB888", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", referencesUniqueColumn: "false")
    }

    changeSet(author: "ami (generated)", id: "1447791590188-55") {
        sql('update invoice set status = "CREDITED" where has_been_credited = true')
    }

    changeSet(author: "ami (generated)", id: "1447791590188-56") {
        dropColumn(columnName: "has_been_credited", tableName: "invoice")
    }

    changeSet(author: "mattias (generated)", id: "1448622838760-2") {
        addColumn(tableName: "redeem_strategy") {
            column(name: "vat", type: "decimal(19,2)")
        }
    }

    changeSet(author: "mattias (generated)", id: "1448622838760-3") {
        sql("update redeem_strategy set vat = 0 where coupon_id is null")
    }

    changeSet(author: "mattias (generated)", id: "1450095967506-2") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "account", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1450135246419-1") {
        addColumn(tableName: "court") {
            column(name: "archived", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1450401891873-1") {
        createTable(tableName: "facility_contract_item_charge_months") {
            column(name: "facility_contract_item_id", type: "bigint")

            column(name: "charge_months_integer", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1450401891873-2") {
        createIndex(indexName: "FKB7679663B16758DB", tableName: "facility_contract_item_charge_months") {
            column(name: "facility_contract_item_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1450401891873-3") {
        addForeignKeyConstraint(baseColumnNames: "facility_contract_item_id", baseTableName: "facility_contract_item_charge_months", constraintName: "FKB7679663B16758DB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_contract_item", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1450819250781-1") {
        addColumn(tableName: "redeem_strategy") {
            column(name: "gift_card_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1450819250781-2") {
        createIndex(indexName: "FKB7679663B16758DC", tableName: "redeem_strategy") {
            column(name: "gift_card_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1450819250781-3") {
        addForeignKeyConstraint(baseColumnNames: "gift_card_id", baseTableName: "redeem_strategy", constraintName: "FKB7679663B16758DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "coupon", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1450994093191-1") {
        addColumn(tableName: "facility_contract") {
            column(name: "gift_card_contract_type", type: "varchar(255)", defaultValue: "PER_USE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1450994093191-2") {
        renameColumn(columnDataType: "decimal(19,2)", newColumnName: "variable_gift_card_mediation_fee", oldColumnName: "fixed_gift_card_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei", id: "1450994093191-3") {
        sql("update facility_contract set variable_gift_card_mediation_fee = variable_coupon_mediation_fee")
    }

    changeSet(author: "sergei", id: "1450994093191-4") {
        addNotNullConstraint(columnDataType: "decimal(19,2)", columnName: "variable_gift_card_mediation_fee", tableName: "facility_contract")
    }

    changeSet(author: "sergei (generated)", id: "1451572289048-1") {
        dropColumn(columnName: "charge_end_date", tableName: "facility_contract_item")
    }

    changeSet(author: "sergei (generated)", id: "1451572289048-2") {
        dropColumn(columnName: "charge_start_date", tableName: "facility_contract_item")
    }

    changeSet(author: "sergei (generated)", id: "1452807488650-1") {
        addColumn(tableName: "activity") {
            column(name: "sign_up_days_in_advance_restriction", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1452807488650-2") {
        addColumn(tableName: "activity_occasion") {
            column(name: "sign_up_days_in_advance_restriction", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1451951814488-1") {
        addColumn(tableName: "price_list_customer_category") {
            column(name: "online_select", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1451951814488-2") {
        addColumn(tableName: "booking") {
            column(name: "selected_customer_category_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1451951814488-3") {
        createIndex(indexName: "FK3DB0859CE48D4C0", tableName: "booking") {
            column(name: "selected_customer_category_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1451951814488-4") {
        addForeignKeyConstraint(baseColumnNames: "selected_customer_category_id", baseTableName: "booking", constraintName: "FK3DB0859CE48D4C0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "price_list_customer_category", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1452807488651-1") {
        addNotNullConstraint(columnDataType: "varchar(2000)", columnName: "metadata_elt", tableName: "order_metadata")
    }

    changeSet(author: "calle (generated)", id: "1453452086662-1") {
        createIndex(indexName: "FK549C0B423C37E269", tableName: "order_metadata") {
            column(name: "metadata")
        }
    }

    changeSet(author: "sergei (generated)", id: "1453499460306-1") {
        addColumn(tableName: "coupon") {
            column(name: "end_date", type: "date")
        }
    }

    changeSet(author: "ami (generated)", id: "1453452086662-2") {
        createIndex(indexName: "async_mail_to_message_id_ind", tableName: "async_mail_to") {
            column(name: "message_id")
        }
    }

    changeSet(author: "mattias", id: "1453452086663-1") {
        sql("ALTER IGNORE TABLE facility_sport ADD UNIQUE INDEX unique_facility_sport_idx (facility_sports_id, sport_id);")
    }

    changeSet(author: "sergei (generated)", id: "1455657732297-1") {
        addColumn(tableName: "membership_type") {
            column(name: "organization_type", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1455657732297-2") {
        sql("update customer set type = 'ORGANIZATION' where type = 'COMPANY'")
    }

    changeSet(author: "sergei", id: "1453452086661-1") {
        sql("delete fur from facility_user_role fur join facility_user fu on fu.id = fur.facility_user_id join `user` u on u.id = fu.user_id join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id where r.authority = 'ROLE_FACILITY'")
        sql("delete fu from facility_user fu join `user` u on u.id = fu.user_id join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id where r.authority = 'ROLE_FACILITY'")
        sql("insert into facility_user (version, user_id, facility_id) select 0, u.id, uf.facility_id from `user` u join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id join user_facility uf on uf.user_facilities_id = u.id where r.authority = 'ROLE_FACILITY'")
        sql("insert into facility_user_role (access_right, facility_user_id) select 'FACILITY_ADMIN', fu.id from facility_user fu join `user` u on u.id = fu.user_id join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id join user_facility uf on uf.user_facilities_id = fu.user_id and uf.facility_id = fu.facility_id where r.authority = 'ROLE_FACILITY'")
        sql("delete fur from facility_user_role fur join facility_user fu on fu.id = fur.facility_user_id join `user` u on u.id = fu.user_id join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id where r.authority = 'ROLE_ADMIN'")
        sql("delete fu from facility_user fu join `user` u on u.id = fu.user_id join user_role ur on ur.user_id = u.id join role r on r.id = ur.role_id where r.authority = 'ROLE_ADMIN'")
        sql("delete from user_role where role_id = (select id from role where authority = 'ROLE_FACILITY')")
        sql("delete from role where authority = 'ROLE_FACILITY'")
        sql("delete from user_facility")
    }

    changeSet(author: "sergei (generated)", id: "1455383024319-1") {
        dropForeignKeyConstraint(baseTableName: "user_facility", constraintName: "FK20E1C897DDF0EBF7")
    }

    changeSet(author: "sergei (generated)", id: "1455383024319-2") {
        dropForeignKeyConstraint(baseTableName: "user_facility", constraintName: "FK20E1C8979B88958D")
    }

    changeSet(author: "sergei (generated)", id: "1455383024319-3") {
        dropTable(tableName: "user_facility")
    }

    changeSet(author: "sergei (generated)", id: "1458177654727-1") {
        addColumn(tableName: "subscription") {
            column(name: "order_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1458177654727-2") {
        createIndex(indexName: "FK1456591D9195D0E", tableName: "subscription") {
            column(name: "order_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1458177654727-3") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "subscription", constraintName: "FK1456591D9195D0E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "order", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1461623492760-1") {
        addColumn(tableName: "court") {
            column(name: "show_description_for_admin", type: "bit")
        }
    }

    changeSet(author: "sergei (generated)", id: "1461623492760-2") {
        addColumn(tableName: "court") {
            column(name: "show_description_online", type: "bit")
        }
    }

    changeSet(author: "sergei", id: "1463608377359-1") {
        sql("update membership_type set price = 0 where price is null")
    }

    changeSet(author: "sergei (generated)", id: "1463608377359-2") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "price", tableName: "membership_type")
    }

    changeSet(author: "sergei (generated)", id: "1464723372395-1") {
        addColumn(tableName: "invoice_row") {
            column(name: "discount_type", type: "varchar(255)", defaultValue: "AMOUNT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "calle (generated)", id: "1467740194800-2") {
        createTable(tableName: "slot_watch") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "slot_watchPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "bigint")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "from_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "to_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "sms_notify", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "calle (generated)", id: "1467739058877-3") {
        createTable(tableName: "slot_watch_event") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "slot_watch_evPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "slot_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "ami (generated)", id: "1470340477310-2") {
        addPrimaryKey(columnNames: "activity_occasion_trainers_id, trainer_id", tableName: "activity_occasion_trainer")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-3") {
        addPrimaryKey(columnNames: "activity_slot_condition_activities_id, activity_id", tableName: "activity_slot_condition_class_activity")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-4") {
        addPrimaryKey(columnNames: "message_id, bcc_idx", tableName: "async_mail_bcc")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-5") {
        addPrimaryKey(columnNames: "message_id, cc_idx", tableName: "async_mail_cc")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-6") {
        addPrimaryKey(columnNames: "message_id, header_name, header_value", tableName: "async_mail_header")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-7") {
        addPrimaryKey(columnNames: "message_id, to_idx", tableName: "async_mail_to")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-8") {
        addPrimaryKey(columnNames: "coupon_condition_group_id, slot_condition_set_id", tableName: "coupon_condition_groups_slot_conditions_sets")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-9") {
        addPrimaryKey(columnNames: "course_activity_trainers_id, trainer_id", tableName: "course_activity_trainer")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-10") {
        addPrimaryKey(columnNames: "cc_court_id, cc_courtpricecondition_id", tableName: "court_price_condition_courts")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-11") {
        addPrimaryKey(columnNames: "court_slot_condition_courts_id, court_id", tableName: "court_slot_condition_court")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-12") {
        addPrimaryKey(columnNames: "customer_group_price_condition_groups_id, group_id", tableName: "customer_group_price_condition_facility_group")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-13") {
        addPrimaryKey(columnNames: "facility_availabilities_id, availability_id", tableName: "facility_availability")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-14") {
        addPrimaryKey(columnNames: "facility_contract_item_id, charge_months_integer", tableName: "facility_contract_item_charge_months")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-15") {
        addPrimaryKey(columnNames: "facility_sports_id, sport_id", tableName: "facility_sport")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-16") {
        addPrimaryKey(columnNames: "member_type_price_condition_membership_types_id, membership_type_id", tableName: "member_type_price_condition_membership_type")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-1") {
        sql("delete from activity_occasion_booking where booking_id is null")
        addPrimaryKey(columnNames: "booking_id, activity_occasion_id", tableName: "activity_occasion_booking")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-18") {
        addPrimaryKey(columnNames: "metadata, metadata_idx", tableName: "order_metadata")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-19") {
        addPrimaryKey(columnNames: "order_parameters, order_parameters_idx", tableName: "payment_order_order_parameters")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-20") {
        addPrimaryKey(columnNames: "order_parameters, order_parameters_idx", tableName: "payment_order_parameters")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-21") {
        addPrimaryKey(columnNames: "slot_condition_set_slot_conditions_id, slot_condition_id", tableName: "slot_condition_set_slot_condition")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-22") {
        addPrimaryKey(columnNames: "slot_payments_id, payment_id", tableName: "slot_payment")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-23") {
        addPrimaryKey(columnNames: "user_availabilities_id, availability_id", tableName: "user_availability")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-24") {
        addPrimaryKey(columnNames: "weekday_slot_condition_id, weekdays_integer", tableName: "weekday_slot_condition_weekdays")
    }

    changeSet(author: "ami (generated)", id: "1470340477310-17") {
        addPrimaryKey(columnNames: "membership_id, invoice_row_id", tableName: "membership_payment_history")
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-2") {
        createTable(tableName: "training_court") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "training_courPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "bigint")

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-109") {
        createIndex(indexName: "FKE5BC31E613E6BDDD", tableName: "training_court") {
            column(name: "court_id")
        }
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-110") {
        createIndex(indexName: "FKE5BC31E6DDF0EBF7", tableName: "training_court") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-96") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "training_court", constraintName: "FKE5BC31E613E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-97") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "training_court", constraintName: "FKE5BC31E6DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "andrew (generated)", id: "1471336739564-98") {
        sql("insert into training_court(`court_id`, `facility_id`, `name`, `version`) select `id`, `facility_id`, `name`, `version` from court")
    }

    changeSet(author: "andrew (generated)", id: "1471515870993-85") {
        dropForeignKeyConstraint(baseTableName: "activity_occasion", constraintName: "FK61502A1713E6BDDD")
    }

    changeSet(author: "andrew (generated)", id: "1471515870993-87") {
        sql("update activity_occasion as ao set ao.court_id = (select id from training_court as tc where tc.court_id = ao.court_id) where ao.court_id is not null")
    }

    changeSet(author: "andrew (generated)", id: "1471515870993-86") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "activity_occasion", constraintName: "FK61502A17EECCD806", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "training_court", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1476570570904-1") {
        createTable(tableName: "facility_notification") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "facility_notiPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "notification_text", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "publish_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1476570570904-2") {
        createIndex(indexName: "FK9DD5A507DDF0EBF7", tableName: "facility_notification") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1476570570904-3") {
        createIndex(indexName: "fac_not_date_idx", tableName: "facility_notification") {
            column(name: "publish_date")
            column(name: "end_date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1476570570904-4") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_notification", constraintName: "FK9DD5A507DDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1472076910017-1") {
        createTable(tableName: "iosync") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "iosyncPK")
            }

            column(name: "customerid", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "error_message", type: "varchar(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "syncid", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1472076910017-2") {
        createIndex(indexName: "customerid_idx", tableName: "iosync") {
            column(name: "customerid")
        }
    }

    changeSet(author: "sergei (generated)", id: "1472076910017-3") {
        createIndex(indexName: "syncid_idx", tableName: "iosync") {
            column(name: "syncid")
        }
    }

    changeSet(author: "mattias (generated)", id: "1476901347044-2") {
        renameColumn(columnDataType: "bigint", newColumnName: "customer_id", oldColumnName: "customerid", tableName: "iosync")
        renameColumn(columnDataType: "varchar(255)", newColumnName: "message", oldColumnName: "error_message", tableName: "iosync")
        renameColumn(columnDataType: "varchar(255)", newColumnName: "sync_id", oldColumnName: "syncid", tableName: "iosync")

        addColumn(tableName: "iosync") {
            column(name: "status", type: "varchar(255)")
        }
    }

    changeSet(author: "mattias", id: "1476901347045-1") {
        renameColumn(columnDataType: "varchar(255)", newColumnName: "batch_id", oldColumnName: "sync_id", tableName: "iosync")
    }

    changeSet(author: "sergei (generated)", id: "1477100779491-1") {
        modifyDataType(columnName: "content", newDataType: "longtext", tableName: "facility_message")
    }

    changeSet(author: "sergei (generated)", id: "1477100779491-2") {
        dropForeignKeyConstraint(baseTableName: "facility_notification", constraintName: "FK9DD5A507DDF0EBF7")
    }

    changeSet(author: "sergei (generated)", id: "1477100779491-3") {
        dropIndex(indexName: "fac_not_date_idx", tableName: "facility_notification")
    }

    changeSet(author: "sergei (generated)", id: "1477100779491-4") {
        dropTable(tableName: "facility_notification")
    }

    changeSet(author: "sergei (generated)", id: "1477224803391-1") {
        addColumn(tableName: "slot_redeem") {
            column(name: "redeemed", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1477224803391-2") {
        addColumn(tableName: "slot_redeem") {
            column(name: "amount", type: "decimal(19,2)")
        }
    }

    changeSet(author: "andrew (generated)", id: "1477561811776-1") {
        addColumn(tableName: "facility") {
            column(name: "booking_invoice_row_organization_id", type: "bigint")
        }
    }

    changeSet(author: "andrew (generated)", id: "1477561811776-2") {
        addColumn(tableName: "facility") {
            column(name: "invoice_fee_organization_id", type: "bigint")
        }
    }

    changeSet(author: "andrew (generated)", id: "1477561811776-3") {
        addColumn(tableName: "redeem_strategy") {
            column(name: "organization_id", type: "bigint")
        }
    }

    changeSet(author: "mattias", id: "1477561811777-1") {
        sql("alter table order_metadata convert to character set utf8 collate utf8_general_ci")
    }

    changeSet(author: "andrew (generated)", id: "1479224463822-1") {
        addColumn(tableName: "activity") {
            column(name: "email", type: "varchar(255)")
        }
    }

    changeSet(author: "andrew (generated)", id: "1482821891615-1") {
        addColumn(tableName: "activity") {
            column(name: "emails", type: "varchar(255)")
        }
    }

    changeSet(author: "andrew (generated)", id: "1482821891615-2") {
        sql("update activity as a set emails = (select group_concat(t.email) from course_activity_trainer as cat inner join trainer as t on cat.trainer_id = t.id where a.id = cat.course_activity_trainers_id and t.email is not null group by cat.course_activity_trainers_id)")
    }

    changeSet(author: "sergei", id: "1482821891616-1") {
        sql("delete from facility_property where key_name = 'FEATURE_PROMO_CODES'")
    }

    changeSet(author: "matti (generated)", id: "1486117290913-2") {
        addColumn(tableName: "customer") {
            column(name: "club", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "andrew (generated)", id: "1486105629243-1") {
        addColumn(tableName: "activity") {
            column(name: "list_position", type: "integer")
        }
    }

    changeSet(author: "andrew (generated)", id: "1486105629243-2") {
        sql('update activity set list_position = id')
    }

    changeSet(author: "andrew (generated)", id: "1489492083447-1") {
        modifyDataType(columnName: "note", newDataType: "longtext", tableName: "order_refund")
    }

    changeSet(author: "victorlindhe (generated)", id: "1490104582817-1") {
        addColumn(tableName: "facility") {
            column(name: "mpc_status", type: "varchar(255)", defaultValue: "NOT_OK") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias", id: "1486105629244-1") {
        sql('insert into facility_property (version, date_created, facility_id, key_name, last_updated, value) select 0, now(), id, "FEATURE_QUEUE", now(), 0 from facility as a')
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-1") {
        createTable(tableName: "court_facility_access_codes") {
            column(name: "facility_access_code_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "bigint")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-2") {
        createTable(tableName: "facility_access_code") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "facility_accePK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "valid_from", type: "datetime")

            column(name: "valid_to", type: "datetime")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-3") {
        createIndex(indexName: "FK2A4CB1F313E6BDDD", tableName: "court_facility_access_codes") {
            column(name: "court_id")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-4") {
        createIndex(indexName: "FK2A4CB1F327EFDF3F", tableName: "court_facility_access_codes") {
            column(name: "facility_access_code_id")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-5") {
        createIndex(indexName: "FK872604CDDF0EBF7", tableName: "facility_access_code") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-6") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "court_facility_access_codes", constraintName: "FK2A4CB1F313E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-7") {
        addForeignKeyConstraint(baseColumnNames: "facility_access_code_id", baseTableName: "court_facility_access_codes", constraintName: "FK2A4CB1F327EFDF3F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_access_code", referencesUniqueColumn: "false")
    }

    changeSet(author: "victorlindhe (generated)", id: "1493742375872-8") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_access_code", constraintName: "FK872604CDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "victorlindhe", id: "1493742375872-9") {
        sql("insert into facility_access_code (version, active, content, facility_id, valid_from, valid_to) select version, active, content, facility_id, valid_from, valid_to from facility_message where channel = 'ACCESS_CODE'")
    }

    changeSet(author: "victorlindhe", id: "1493742375872-10") {
        sql("delete from facility_message where channel = 'ACCESS_CODE'")
    }

    changeSet(author: "victorlindhe", id: "1493742375872-11") {
        sql("insert into court_facility_access_codes (court_id, facility_access_code_id) select c.id as court_id, fac.id as facility_access_code_id from facility_access_code fac left join court c on c.facility_id = fac.facility_id where c.archived = 0")
    }

    changeSet(author: "mattiasaronsson", id: "1493742375872-12") {
        addPrimaryKey(columnNames: "facility_access_code_id, court_id", tableName: "court_facility_access_codes")
    }

    changeSet(author: "mattias (generated)", id: "1473155105605-2") {
        addColumn(tableName: "order_payment") {
            column(name: "reference_id", type: "varchar(255)")
        }
    }

    changeSet(author: "mattias (generated)", id: "1496827127016-6") {
        addColumn(tableName: "payment") {
            column(name: "migrated", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1498459718292-1") {
        addColumn(tableName: "activity") {
            column(name: "sign_up_days_until_restriction", type: "integer")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1498460742536-1") {
        addColumn(tableName: "activity_occasion") {
            column(name: "sign_up_days_until_restriction", type: "integer")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1498464213116-1") {
        addColumn(tableName: "activity") {
            column(name: "members_only", type: "bit")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1498465812371-6") {
        addColumn(tableName: "activity_occasion") {
            column(name: "members_only", type: "bit")
        }
    }

    changeSet(author: "matti (generated)", id: "1500371589179-6") {
        addColumn(tableName: "iosync") {
            column(name: "activity_occasion_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "matti (generated)", id: "1503671389735-6") {
        addColumn(tableName: "trainer") {
            column(name: "customer_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "matti (generated)", id: "1503671389735-134") {
        createIndex(indexName: "FKC0639CB59AE61E17", tableName: "trainer") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "matti (generated)", id: "1503671389735-110") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "trainer", constraintName: "FKC0639CB59AE61E17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "victorlindhe", id: "1505287285-1") {
        sqlFile(path: 'encode-script-migration-v2.sql')
    }

    changeSet(author: "victorlindhe", id: "1507624935-1") {
        sql("UPDATE submission_value SET value_index = 0 WHERE input_group = 'Måndag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 0")
    }

    changeSet(author: "victorlindhe", id: "1507624935-2") {
        sql("UPDATE submission_value SET value_index = 1 WHERE input_group = 'Tisdag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 1")
    }

    changeSet(author: "victorlindhe", id: "1507624935-3") {
        sql("UPDATE submission_value SET value_index = 2 WHERE input_group = 'Onsdag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 2")
    }

    changeSet(author: "victorlindhe", id: "1507624935-4") {
        sql("UPDATE submission_value SET value_index = 3 WHERE input_group = 'Torsdag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 3")
    }

    changeSet(author: "victorlindhe", id: "1507624935-5") {
        sql("UPDATE submission_value SET value_index = 4 WHERE input_group = 'Fredag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 4")
    }

    changeSet(author: "victorlindhe", id: "1507624935-6") {
        sql("UPDATE submission_value SET value_index = 5 WHERE input_group = 'Lördag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 5")
    }

    changeSet(author: "victorlindhe", id: "1507624935-7") {
        sql("UPDATE submission_value SET value_index = 6 WHERE input_group = 'Söndag' AND field_type = 'TIMERANGE_CHECKBOX' and value_index != 6")
    }

    changeSet(author: "victorlindhe", id: "1507882694-1") {
        sql("update court set offline_only = 0 where offline_only is null")
    }

    changeSet(author: "victorlindhe", id: "1507882694-2") {
        sql("alter table court modify offline_only bit(1) not null default 0")
    }

    changeSet(author: "victorlindhe (generated)", id: "1508823969011-1") {
        addColumn(tableName: "customer") {
            column(name: "access_code", type: "varchar(255)")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1508836876272-1") {
        addColumn(tableName: "subscription") {
            column(name: "access_code", type: "varchar(255)")
        }
    }

    changeSet(author: "mattias (generated)", id: "1496870599950-6") {
        addColumn(tableName: "payment_info") {
            column(name: "expiry_month", type: "varchar(255)")
        }
        addColumn(tableName: "payment_info") {
            column(name: "expiry_year", type: "varchar(255)")
        }
        addColumn(tableName: "payment_info") {
            column(name: "provider", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
        addColumn(tableName: "payment_info") {
            column(name: "holder_name", type: "varchar(255)")
        }
        addColumn(tableName: "payment_info") {
            column(name: "number", type: "varchar(255)")
        }

        sql('update payment_info set provider = "NETAXEPT" where id is not null')
    }

    changeSet(author: "mattias (generated)", id: "1499897030972-1") {
        createTable(tableName: "adyen_order_payment_error") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "adyen_order_pPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "action", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "reason", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1499897030972-7") {
        addColumn(tableName: "order_payment") {
            column(name: "error_id", type: "bigint")
        }

        createIndex(indexName: "FKC6603215828BDB0F", tableName: "order_payment") {
            column(name: "error_id")
        }

        addForeignKeyConstraint(baseColumnNames: "error_id", baseTableName: "order_payment", constraintName: "FKC6603215828BDB0F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "adyen_order_payment_error", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1500418280230-36") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "country", tableName: "facility")
    }

    changeSet(author: "mattias (generated)", id: "1500418854903-6") {
        addColumn(tableName: "region") {
            column(name: "country", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1506075959842-6") {
        addColumn(tableName: "order_payment") {
            column(name: "method", type: "varchar(255)")
        }
    }

    changeSet(author: "mattias (generated)", id: "1506075959843-1") {
        sql("UPDATE order_payment SET method = 'CREDIT_CARD' WHERE type = 'netaxept'")
    }

    changeSet(author: "mattias (generated)", id: "1509448388073-1") {
        createTable(tableName: "adyen_notification") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "adyen_notificPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "event_code", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "psp_reference", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "reason", type: "varchar(255)")

            column(name: "success", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1509448388073-2") {
        createTable(tableName: "adyen_notification_additional_data") {
            column(name: "additional_data", type: "bigint")

            column(name: "additional_data_idx", type: "varchar(255)")

            column(name: "additional_data_elt", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1510839431471-6") {
        addColumn(tableName: "submission") {
            column(name: "status", type: "varchar(255)")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1512458262412-6") {
        addColumn(tableName: "submission") {
            column(name: "original_date", type: "datetime")
        }
    }

    changeSet(author: "stan (generated)", id: "1513072465141-43") {
        modifyDataType(columnName: "note", newDataType: "varchar(2000)", tableName: "customer_coupon")
    }

    changeSet(author: "mattias (generated)", id: "1515675344300-6") {
        addColumn(tableName: "adyen_notification") {
            column(name: "executed", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "stan (generated)", id: "migrate-mlcs-data") {
        sql("""insert into facility_property (version,date_created,facility_id,key_name,last_updated,`value`) select '0',NOW(), id, 'MLCS_LAST_HEARTBEAT',NOW(), mlcs_last_heartbeat from facility f where mlcs_last_heartbeat is not null;
               insert into facility_property (version,date_created,facility_id,key_name,last_updated,`value`) select '0',NOW(), id, 'MLCS_GRACE_MINUTES_START',NOW(), mlcs_grace_minutes_start from facility f where mlcs_grace_minutes_start is not null;
               insert into facility_property (version,date_created,facility_id,key_name,last_updated,`value`) select '0',NOW(), id, 'MLCS_GRACE_MINUTES_END',NOW(), mlcs_grace_minutes_end from facility f where mlcs_grace_minutes_end is not null;
        """)
    }

    include file: "requirement-profiles-initial.groovy"
    include file: "booking-restrictions-initial.groovy"

    changeSet(author: "mattias", id: "1509448388074-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "facility_id", tableName: "order")
    }

    changeSet(author: "mattiasaronsson", id: "migrate-mpc-status") {
        sql("""insert into facility_property (version,date_created,facility_id,key_name,last_updated,`value`) select '0',NOW(), id, 'MPC_STATUS',NOW(), mpc_status from facility f where mpc_status != '';
        """)
    }

    include file: "book-a-trainer-initial.groovy"

    changeSet(author: "mattias (generated)", id: "1522846274594-6") {
        addColumn(tableName: "request") {
            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }
        }

        addColumn(tableName: "request") {
            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1522932190131-6") {
        addColumn(tableName: "customer") {
            column(name: "vat_number", type: "varchar(255)")
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-4") {
        createTable(tableName: "court_group") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "court_groupPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)")

            column(name: "tab_position", type: "integer")
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-5") {
        createTable(tableName: "court_groups") {
            column(name: "court_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "group_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-86") {
        addPrimaryKey(columnNames: "court_id, group_id", tableName: "court_groups")
    }

    changeSet(author: "stan (generated)", id: "1524061397753-147") {
        createIndex(indexName: "FK262616CBDDF0EBF7", tableName: "court_group") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-148") {
        createIndex(indexName: "FK9E9CC30813E6BDDD", tableName: "court_groups") {
            column(name: "court_id")
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-149") {
        createIndex(indexName: "FK9E9CC308AA1B86C", tableName: "court_groups") {
            column(name: "group_id")
        }
    }

    changeSet(author: "stan (generated)", id: "1524061397753-122") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "court_group", constraintName: "FK262616CBDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }

    changeSet(author: "stan (generated)", id: "1524061397753-123") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "court_groups", constraintName: "FK9E9CC30813E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "stan (generated)", id: "1524061397753-124") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "court_groups", constraintName: "FK9E9CC308AA1B86C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court_group", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1537006712834-1") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "court_group")
    }

    changeSet(author: "mattias (generated)", id: "1523872234494-6") {
        addColumn(tableName: "court") {
            column(name: "restriction", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }

        addColumn(tableName: "court") {
            column(name: "profiles", type: "varchar(255)")
        }

        sql("""update court set restriction = "NONE" where id > 0;
               update court set restriction = "MEMBERS_ONLY" where members_only = true;
               update court set restriction = "OFFLINE_ONLY" where offline_only = true;
        """)
    }

    changeSet(author: "stan (generated)", id: "1523533473455-6") {
        addColumn(tableName: "ticket") {
            column(name: "new_email", type: "varchar(255)")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1526992134712-6") {
        addColumn(tableName: "user") {
            column(name: "date_deleted", type: "datetime")
        }
    }

    changeSet(author: "matti (generated)", id: "1525279495766-6") {
        addColumn(tableName: "user") {
            column(name: "date_agreed_to_terms", type: "datetime") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "matti (generated)", id: "1525279495766-8") {
        addColumn(tableName: "user") {
            column(name: "receive_facility_notifications", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "victorlindhe", id: "insert-pickleball") {
        sql("insert into sport (id, version, name) values (7, 0, 'Pickleball');")
    }


    changeSet(author: "stan (generated)", id: "1526389683053-6") {
        addColumn(tableName: "customer") {
            column(name: "deleted", type: "datetime")
        }
    }

    changeSet(author: "matti (generated)", id: "1525279495766-9") {
        addColumn(tableName: "user") {
            column(name: "receive_customer_surveys", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "stan (generated)", id: "1528368062892-6") {
        addColumn(tableName: "facility") {
            column(name: "instagram", type: "varchar(255)")
        }
    }

    changeSet(author: "stan (generated)", id: "1529925238049-48") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "email", tableName: "facility")
    }

    changeSet(author: "stan", id: "1528368062892-7") {
        sql('''update customer set firstname="Kund", lastname="borttagen" where firstname = "Kund borttagen";''')
        sql('''update customer set firstname="Fjern", lastname="kunde" where firstname = "Fjern kunde";''')
        sql('''update customer set firstname="Cliente", lastname="borrado" where firstname = "Cliente borrado";''')
    }

    changeSet(author: "sergei", id: "1528368062893-1") {
        grailsChange {
            change {
                sql.eachRow("select * from facility_property where key_name = 'MULTIPLE_PLAYERS_NUMBER'") { fp ->
                    def newValue = sql.rows("select * from facility_sport where facility_sports_id = ?",
                            [fp.facility_id]).collectEntries {
                        [(it.sport_id.toString()): fp.value]
                    }
                    sql.executeUpdate("update facility_property set value = ? where id = ?",
                            [newValue.inspect(), fp.id])
                }
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1534278356062-147") {
        createIndex(indexName: "player_email_idx", tableName: "player") {
            column(name: "email")
        }
    }

    changeSet(author: "mattias", id: "1493742375873-1") {
        sqlFile(path: 'quartz-1.0.2.sql')
    }

    changeSet(author: "victorlindhe (generated)", id: "1536051776276-73") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "facility_id", tableName: "scheduled_task")
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-1") {
        createTable(tableName: "translatable") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "translatablePK")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-2") {
        createTable(tableName: "translatable_translations") {
            column(name: "translations", type: "bigint")

            column(name: "translations_idx", type: "varchar(255)")

            column(name: "translations_elt", type: "longtext") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-3") {
        addColumn(tableName: "global_notification") {
            column(name: "is_for_facility_admins", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-4") {
        addColumn(tableName: "global_notification") {
            column(name: "is_for_users", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-5") {
        addColumn(tableName: "global_notification") {
            column(name: "notification_text_id", type: "bigint")
        }
    }

    changeSet(author: "sergei", id: "1535545434027-6") {
        grailsChange {
            change {
                sql.eachRow("select * from global_notification") { gn ->
                    def tid = sql.executeInsert("insert into translatable (id) values (null)")[0][0]
                    sql.executeInsert("insert into translatable_translations (translations, translations_idx, translations_elt) values (?, ?, ?)",
                            [tid, "en", gn.notification_text])
                    sql.executeUpdate("update global_notification set notification_text_id = ? where id = ?", [tid, gn.id])
                }
            }
        }
    }

    changeSet(author: "sergei", id: "1535545434027-7") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "notification_text_id", tableName: "global_notification")
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-8") {
        createIndex(indexName: "FKFC653007D4BA9B7D", tableName: "global_notification") {
            column(name: "notification_text_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-9") {
        addForeignKeyConstraint(baseColumnNames: "notification_text_id", baseTableName: "global_notification", constraintName: "FKFC653007D4BA9B7D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "translatable", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1535545434027-10") {
        createIndex(indexName: "translations_trbl_idx", tableName: "translatable_translations") {
            column(name: "translations")
        }
    }

    changeSet(author: "sergei (generated)", id: "1535545434027-11") {
        dropColumn(columnName: "notification_text", tableName: "global_notification")
    }

    changeSet(author: "victorlindhe (generated)", id: "1537512571109-6") {
        addColumn(tableName: "availability") {
            column(name: "valid_end", type: "date")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1537512571109-7") {
        addColumn(tableName: "availability") {
            column(name: "valid_start", type: "date")
        }
    }

    changeSet(author: "sergei (generated)", id: "1539863818848-1") {
        addColumn(tableName: "facility") {
            column(name: "is_all_courts_tab_default", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "victorlindhe", id: "delete-notify-mpc-status") {
        sql("delete from facility_property where key_name = 'FEATURE_NOTIFY_MPC_STATUS';")
    }

    changeSet(author: "mattiasaronsson", id: "1541162620848-1") {
        addColumn(tableName: "sport") {
            column(name: "position", type: "int") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattiasaronsson", id: "1541162630900-1") {
        sql("update sport set position = 1 where id = 1;")
        sql("update sport set position = 2 where id = 2;")
        sql("update sport set position = 3 where id = 3;")
        sql("update sport set position = 4 where id = 4;")
        sql("update sport set position = 5 where id = 5;")
        sql("update sport set position = 7 where id = 6;")
        sql("update sport set position = 6 where id = 7;")
    }

    changeSet(author: "magnus (generated)", id: "1542183133652-2") {
        addColumn(tableName: "facility") {
            column(name: "bic", type: "varchar(255)")
        }
    }

    changeSet(author: "magnus (generated)", id: "1542183133652-3") {
        addColumn(tableName: "facility") {
            column(name: "iban", type: "varchar(255)")
        }
    }

    changeSet(author: "magnuslundahl", id: "1542375063000-1") {
        sqlFile(path: 'MW-3585-correct-offer-order-vat.sql')
    }
    changeSet(author: "magnus (generated)", id: "1542716295644-2") {
        addColumn(tableName: "activity") {
            column(name: "deleted", type: "bit") {
                constraints(nullable: "true")
            }
        }
        sql("update activity set deleted = false;")
    }

    changeSet(author: "victorlindhe (generated)", id: "1543304208074-142") {
        dropTable(tableName: "adyen_notification_additional_data")
    }

    changeSet(author: "stan", id: "1537512571109-8") {
        sql('update sport set name = "Tennis"       where id = 1')
        sql('update sport set name = "Badminton"    where id = 2')
        sql('update sport set name = "Squash"       where id = 3')
        sql('update sport set name = "Table tennis" where id = 4')
        sql('update sport set name = "Padel"        where id = 5')
        sql('update sport set name = "Other"        where id = 6')
        sql('update sport set name = "Pickleball"   where id = 7')
    }

    include file: "MW-2722-membership-pro.groovy"

    changeSet(author: "magnus (generated)", id: "1544453734856-1") {
        createIndex(indexName: "booking_id_idx", tableName: "code_request") {
            column(name: "booking_id")
        }
    }

    changeSet(author: "magnus (generated)", id: "1544530957160-1") {
        createIndex(indexName: "type_transaction_id_idx", tableName: "order_payment") {
            column(name: "type")
            column(name: "transaction_id")
        }
    }

    changeSet(author: "magnus (generated)", id: "1545212979792-1") {
        dropForeignKeyConstraint(baseTableName: "async_mail_attachment", constraintName: "FK1CACA0E817082B9")
        addForeignKeyConstraint(baseColumnNames: "message_id", baseTableName: "async_mail_attachment", constraintName: "FK1CACA0E817082B9", deferrable: "false", initiallyDeferred: "false", onDelete: "CASCADE", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "async_mail_mess", referencesUniqueColumn: "false")
    }

    changeSet(author: "matti", id: "1545249333123-2") {
        renameColumn(columnDataType: "varchar(255)", newColumnName: "external_system", oldColumnName: "system", tableName: "external_synchronization_entity")
    }

    changeSet(author: "sergei (generated)", id: "1546440757144-1") {
        addColumn(tableName: "slot_condition") {
            column(name: "nr_of_hours", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1546440757144-2") {
        sql("update slot_condition set class = 'com.matchi.conditions.HoursInAdvanceBookableSlotCondition', nr_of_hours = nr_of_days * 24 where class = 'com.matchi.conditions.DaysInAdvanceBookableSlotCondition'")
    }

    changeSet(author: "sergei (generated)", id: "1546440757144-3") {
        dropColumn(columnName: "nr_of_days", tableName: "slot_condition")
    }

    changeSet(author: "sergei (generated)", id: "1547116750616-1") {
        addColumn(tableName: "membership") {
            column(name: "starting_grace_period_days", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1547039593208-2") {
        addColumn(tableName: "membership_type") {
            column(name: "paid_on_renewal", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "magnus (manual)", id: "1548251078730-1") {
        createIndex(indexName: "mpc_id_idx", tableName: "code_request") {
            column(name: "mpc_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1546508980144-1") {
        addColumn(tableName: "scheduled_task") {
            column(name: "success_message", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1548341147176-1") {
        sql("alter table adyen_notification add index psp_reference_idx (psp_reference(16) asc)")
    }

    changeSet(author: "sergei", id: "1548341147177-1") {
        sql("alter table code_request add index code_status_idx (status(10) asc)")
    }

    changeSet(author: "magnus (manual)", id: "1548922249433-1") {
        createIndex(indexName: "adyen_notification_create_idx", tableName: "adyen_notification") {
            column(name: "date_created")
            column(name: "executed")
        }
    }

    changeSet(author: "sergei", id: "1548922249444-1") {
        sql("""update order_payment op
            join order_order_payments oop on oop.payment_id = op.id
            join `order` o on oop.order_id = o.id
            set op.status = 'CREDITED', op.credited = op.amount, invoice_row_id = null
            where o.article = 'MEMBERSHIP' and op.status != 'CREDITED' and op.invoice_row_id in (
                select ir.id from invoice_row ir
                join invoice i on ir.invoice_id = i.id
                where i.status = 'CREDITED')""")
    }

    changeSet(author: "sergei (generated)", id: "1550076898197-1") {
        addColumn(tableName: "membership") {
            column(name: "auto_pay", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "sergei (generated)", id: "1551436149901-1") {
        modifyDataType(columnName: "expire_date", newDataType: "date", tableName: "customer_coupon")
    }

    changeSet(author: "magnus (generated)", id: "1551187554124-2") {
        addColumn(tableName: "facility") {
            column(name: "multisport", type: "bit")
        }
    }

    changeSet(author: "magnus (generated)", id: "1551100696746-3") {
        addColumn(tableName: "sport") {
            column(name: "core_sport", type: "bit")
        }
    }

    changeSet(author: "magnus", id: "1551100696746-4") {
        sql("update sport set core_sport = true")
        sql("insert into sport (id, version, name, position, core_sport) values (8, 1, 'Yoga', 8, false);")
        sql("insert into sport (id, version, name, position, core_sport) values (9, 1, 'Trampolin', 9, false);")
    }

    changeSet(author: "trainingday01", id: "insert-golf") {
        sql("insert into sport (id, version, name, position, core_sport) values (10, 1, 'Golf', 10, false);")
    }

    changeSet(author: "trainingday01", id: "reorder-sports") {
        sql("update sport set position = 8 where id = 10;")
        sql("update sport set position = 9 where id = 9;")
        sql("update sport set position = 10 where id = 8;")
    }

    changeSet(author: "victor", id: "denmark-regions-and-cities") {
        sqlFile(path: 'denmark.sql')
    }

    changeSet(author: "sergei", id: "1556523438283-1") {
        renameColumn(columnDataType: "varchar(255)", newColumnName: "name", oldColumnName: "related_domain_class", tableName: "scheduled_task")
    }

    changeSet(author: "sergei (generated)", id: "1556523438283-2") {
        addColumn(tableName: "scheduled_task") {
            column(name: "related_domain_class", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1556523438283-3") {
        createIndex(indexName: "domain_identifier_idx", tableName: "scheduled_task") {
            column(name: "domain_identifier")
        }
    }

    changeSet(author: "victor", id: "booking-comments-length") {
        sql("alter table booking modify comments varchar(1000);")
    }

    changeSet(author: "sergei (generated)", id: "1557933673954-1") {
        addColumn(tableName: "organization") {
            column(name: "fortnox_cost_center", type: "varchar(6)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1558013497021-1") {
        addColumn(tableName: "activity") {
            column(name: "show_online", type: "bit")
        }
    }

    changeSet(author: "sergei (generated)", id: "1558346145678-1") {
        addColumn(tableName: "activity") {
            column(name: "terms", type: "longtext")
        }
    }

    changeSet(author: "sergei (generated)", id: "1558346145678-2") {
        addColumn(tableName: "activity") {
            column(name: "user_message_label", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1558512427666-1") {
        addColumn(tableName: "activity") {
            column(name: "cancel_by_user", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei (generated)", id: "1558512427666-2") {
        addColumn(tableName: "activity") {
            column(name: "cancel_limit", type: "integer")
        }
    }

    changeSet(author: "magnus", id: "1559029796539-1") {
        sql("insert into role (id, version, authority) values (3, 0, 'ROLE_INTEGRATION');")
    }

    changeSet(author: "sergei (generated)", id: "1559805734006-1") {
        addColumn(tableName: "facility") {
            column(name: "membership_starting_grace_nr_of_days", type: "integer")
        }
    }

    changeSet(author: "sergei", id: "1559805734006-2") {
        sql("update facility set membership_starting_grace_nr_of_days = 30 where membership_request_setting = 'DIRECT'")
    }

    changeSet(author: "stan", id: "1559805734006-3") {
        sql('ALTER TABLE customer MODIFY number bigint(20) NULL;')
    }

    changeSet(author: "sergei (generated)", id: "1560431269545-1") {
        addColumn(tableName: "membership_type") {
            column(name: "renewal_starting_grace_nr_of_days", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1560431269545-2") {
        addColumn(tableName: "membership") {
            column(name: "auto_pay_attempts", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-1") {
        dropForeignKeyConstraint(baseTableName: "membership_payment_history", constraintName: "fk_invoice_row_id_idx")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-2") {
        dropForeignKeyConstraint(baseTableName: "membership_payment_history", constraintName: "fk_membership_id_idx")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-3") {
        dropTable(tableName: "membership_payment_history")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-4") {
        dropForeignKeyConstraint(baseTableName: "membership", constraintName: "fk_invoice_row_id")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-5") {
        dropColumn(columnName: "invoice_row_id", tableName: "membership")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-6") {
        dropColumn(columnName: "status", tableName: "membership")
    }

    changeSet(author: "sergei (generated)", id: "1561535430336-7") {
        dropColumn(columnName: "membership_end_date", tableName: "facility")
    }

    changeSet(author: "sergei (generated)", id: "1559133904646-1") {
        createIndex(indexName: "facility_cust_no_idx", tableName: "customer") {
            column(name: "facility_id")
            column(name: "number")
        }
    }

    changeSet(author: "sergei (generated)", id: "1562334540126-1") {
        addColumn(tableName: "slot_watch") {
            column(name: "sport_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1562334540126-3") {
        addForeignKeyConstraint(baseColumnNames: "sport_id", baseTableName: "slot_watch", constraintName: "FKF5A91B4EBD84893D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "sport", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-1") {
        addColumn(tableName: "customer_coupon_ticket") {
            column(name: "description", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-2") {
        addColumn(tableName: "customer_coupon_ticket") {
            column(name: "issuer_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-3") {
        addColumn(tableName: "customer_coupon_ticket") {
            column(name: "nr_of_tickets", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-4") {
        addColumn(tableName: "customer_coupon_ticket") {
            column(name: "type", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-5") {
        dropForeignKeyConstraint(baseTableName: "customer_coupon_ticket", constraintName: "fk_cct_booking_id")
    }

    changeSet(author: "sergei", id: "1563868517209-6") {
        sql("alter table customer_coupon_ticket change booking_id purchased_object_id bigint")
    }

    changeSet(author: "sergei", id: "1563868517209-7") {
        sql("alter table customer_coupon_ticket change price purchased_object_price decimal(19,2)")
    }

    changeSet(author: "sergei", id: "1563868517209-8") {
        sql("update customer_coupon_ticket set type = 'BOOKING' where purchased_object_id is not null")
    }

    changeSet(author: "sergei", id: "1563868517209-9") {
        sql("update customer_coupon_ticket set type = 'BOOKING_REFUND' where purchased_object_id is null")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-10") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "type", tableName: "customer_coupon_ticket")
    }

    changeSet(author: "sergei", id: "1563868517209-11") {
        sql("""update customer_coupon_ticket cct
            join customer_coupon cc on cct.customer_coupon_id = cc.id
            join coupon c on cc.coupon_id = c.id
            set cct.nr_of_tickets = 1
            where c.class = 'coupon' and cct.purchased_object_id is not null and cct.purchased_object_price is not null""")
    }

    changeSet(author: "sergei", id: "1563868517209-12") {
        sql("""update customer_coupon_ticket cct
            join customer_coupon cc on cct.customer_coupon_id = cc.id
            join coupon c on cc.coupon_id = c.id
            set cct.nr_of_tickets = cct.purchased_object_price
            where c.class = 'gift_card' and cct.purchased_object_id is not null and cct.purchased_object_price is not null""")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-13") {
        dropColumn(columnName: "consumed", tableName: "customer_coupon_ticket")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-14") {
        dropColumn(columnName: "last_updated", tableName: "customer_coupon_ticket")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-15") {
        dropColumn(columnName: "version", tableName: "customer_coupon_ticket")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-16") {
        createIndex(indexName: "FK46CA6EA42D7AFBC9", tableName: "customer_coupon_ticket") {
            column(name: "issuer_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-17") {
        addForeignKeyConstraint(baseColumnNames: "issuer_id", baseTableName: "customer_coupon_ticket", constraintName: "FK46CA6EA42D7AFBC9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei (generated)", id: "1563868517209-18") {
        createIndex(indexName: "ticket_obj_type_idx", tableName: "customer_coupon_ticket") {
            column(name: "purchased_object_id")
            column(name: "type")
        }
    }

    changeSet(author: "sergei", id: "1563868517209-19", failOnError: false) {
        dropIndex(indexName: "fk_cct_booking_id_idx", tableName: "customer_coupon_ticket")
    }

    changeSet(author: "Martin", id: "1568106093000-1") {
        addColumn(tableName: "activity") {
            column(name: "extended_email_message", type: "longtext")
        }
    }

    changeSet(author: "sergei (generated)", id: "1568155917431-1") {
        addColumn(tableName: "price_list_customer_category") {
            column(name: "force_use_category_price", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sergei", id: "1568155917442-1") {
        sql("""update customer_coupon_ticket set nr_of_tickets = -nr_of_tickets
            where type = 'BOOKING' and nr_of_tickets is not null and nr_of_tickets > 0""")
    }

    changeSet(author: "sergei", id: "1569014177788-1") {
        renameTable(oldTableName: 'slot_watch', newTableName: 'object_watch')
    }

    changeSet(author: "sergei (generated)", id: "1569014177788-2") {
        addColumn(tableName: "object_watch") {
            column(name: "class", type: "varchar(255)")
        }
    }

    changeSet(author: "sergei", id: "1569014177788-3") {
        sql("update object_watch set class = 'slot_watch'")
    }

    changeSet(author: "sergei", id: "1569014177788-4") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "class", tableName: "object_watch")
    }

    changeSet(author: "sergei (generated)", id: "1569014177788-5") {
        addColumn(tableName: "object_watch") {
            column(name: "class_activity_id", type: "bigint")
        }
    }

    changeSet(author: "sergei (generated)", id: "1569014177788-6") {
        createIndex(indexName: "FKC83A1FCF5DA6F347", tableName: "object_watch") {
            column(name: "class_activity_id")
        }
    }

    changeSet(author: "sergei (generated)", id: "1569014177788-7") {
        addForeignKeyConstraint(baseColumnNames: "class_activity_id", baseTableName: "object_watch", constraintName: "FKC83A1FCF5DA6F347", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "activity", referencesUniqueColumn: "false")
    }

    changeSet(author: "sergei", id: "1569014177788-8") {
        sql("delete from QRTZ_CRON_TRIGGERS where TRIGGER_NAME = 'SlotWatchJob.trigger'")
        sql("delete from QRTZ_TRIGGERS where JOB_NAME = 'com.matchi.jobs.SlotWatchJob'")
        sql("delete from QRTZ_JOB_DETAILS where JOB_NAME = 'com.matchi.jobs.SlotWatchJob'")
    }

    changeSet(author: "victorlindhe (generated)", id: "1570091195813-2") {
        addColumn(tableName: "price_list_customer_category") {
            column(name: "days_bookable", type: "integer")
        }
    }

    changeSet(author: "Martin", id: "1570455590000") {
        addColumn(tableName: "activity") {
            column(name: "level_min", type: "integer") {
                constraints(nullable: "true")
            }
            column(name: "level_max", type: "integer") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "stan", id: "1548922249445-1") {
        dropTable(tableName: "facility_booking_cancel_rule")
    }

    changeSet(author: "victorlindhe (generated)", id: "1571751070044-2") {
        addColumn(tableName: "activity") {
            column(name: "cancel_hours_in_advance", type: "integer")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1571751070044-3") {
        addColumn(tableName: "activity") {
            column(name: "min_num_participants", type: "integer")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1571751070044-4") {
        addColumn(tableName: "activity_occasion") {
            column(name: "automatic_cancellation_date_time", type: "datetime")
        }
    }

    changeSet(author: "victorlindhe (generated)", id: "1571751070044-5") {
        addColumn(tableName: "activity_occasion") {
            column(name: "min_num_participants", type: "integer")
        }
    }

    changeSet(author: "sergei (generated)", id: "1571101280641-1") {
        addColumn(tableName: "price_list") {
            column(name: "type", type: "varchar(255)", defaultValue: "SLOT_BASED") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "andrii", id: "1574063596-1") {
        addColumn(tableName: "facility_message") {
            column(name: "list_position", type: "integer") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "andrii", id: "1574063596-2") {
        sql('update facility_message set list_position = id')
    }

    changeSet(author: "mattiasaronsson", id: "1576158279788-1") {
        addColumn(tableName: "facility") {
            column(name: "google_tag_manager_container_id", type: "varchar(255)")
        }
    }

    changeSet(author: "martinahlberger", id: "1578403241000") {
        addColumn(tableName: "scheduled_task") {
            column(name: "identifier", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "andriiromaniv", id: "1579177351-1") {
        createIndex(indexName: "facility_offline_only_idx", tableName: "court") {
            column(name: "facility_id")
            column(name: "offline_only")
        }
    }

    changeSet(author: "andriiromaniv", id: "1579177351-2") {
        createIndex(indexName: "facility_email_idx", tableName: "customer") {
            column(name: "facility_id")
            column(name: "email")
        }
    }
    changeSet(author: "andriiromaniv", id: "1579177351-3") {
        createIndex(indexName: "facebookuid_idx", tableName: "user") {
            column(name: "facebookuid")
        }
    }

    changeSet(author: "martinahlberger", id: "1579185210000") {
        createTable(tableName: "matchi_config") {
            column(name: "key_name", type: "varchar(100)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "matchi_configPK")
            }

            column(name: "value", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "user_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "varchar(255)")
        }

        createIndex(indexName: "matchi_config_user_id_user_FK", tableName: "matchi_config") {
            column(name: "user_id")
        }

        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "matchi_config", constraintName: "matchi_config_user_id_user_FK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
    }

    changeSet(author: "petersvensson", id: "1579533786000") {
        addColumn(tableName: "facility") {
            column(name: "subscription_redeem_id", type: "bigint(20)") {
                constraints(nullable: "true")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "subscription_redeem_id", baseTableName: "facility", constraintName: "fk_fa_subscription_redeem_id", referencedColumnNames: "id", referencedTableName: "subscription_redeem", deferrable: "false", initiallyDeferred: "false")
        sql("update facility set facility.subscription_redeem_id = (select id from subscription_redeem where facility_id = facility.id)")
        dropForeignKeyConstraint(baseTableName: "subscription_redeem", constraintName: "fk_sr_facility_id")
        dropIndex(tableName: "subscription_redeem", indexName: "fk_sr_facility_id_idx")
        dropColumn(tableName: "subscription_redeem", columnName: "facility_id")
    }

    changeSet(author: "martinahlberger", id: "15807316500000-1") {
        createTable(tableName: "front_end_message") {

            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "front_end_messagePK")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "base_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "publish_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "html_content", type: "longtext")

            column(name: "css_code", type: "longtext")


            column(name: "image_id", type: "bigint") {

            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "martinahlberger", id: "15807316500000-2") {
        createIndex(indexName: "index_fem_image_id", tableName: "front_end_message") {
            column(name: "image_id")
        }
    }

    changeSet(author: "martinahlberger", id: "15807316500000-3") {
        addForeignKeyConstraint(baseColumnNames: "image_id", baseTableName: "front_end_message", constraintName: "FKC_fem_image_id_mfile_id", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "mfile", referencesUniqueColumn: "false")
    }

    changeSet(author: "martinahlberger", id: "15807316500000-4") {
        createTable(tableName: "front_end_message_countries") {
            column(name: "front_end_message_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "country", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "martinahlberger", id: "15807316500000-5") {
        addPrimaryKey(columnNames: "front_end_message_id, country", tableName: "front_end_message_countries")
    }

    changeSet(author: "martinahlberger", id: "15807316500000-6") {
        createTable(tableName: "front_end_message_mfile") {
            column(name: "front_end_message_images_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "mfile_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "martinahlberger", id: "15807316500000-7") {
        addPrimaryKey(columnNames: "front_end_message_images_id, mfile_id", tableName: "front_end_message_mfile")
    }

    changeSet(author: "matti (generated)", id: "1581686272914-1") {
        createTable(tableName: "camera") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cameraPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "camera_id", type: "integer") {
                constraints(nullable: "false")
            }

            column(name: "court_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "matti (generated)", id: "1581686272914-147") {
        createIndex(indexName: "FKAE79C32513E6BDDD", tableName: "camera") {
            column(name: "court_id")
        }
    }

    changeSet(author: "matti (generated)", id: "1581686272914-117") {
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "camera", constraintName: "FKAE79C32513E6BDDD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false")
    }

    changeSet(author: "andriiromaniv", id: "1582633023") {
        sql('update form_field set help_text = "Välj de dagar som passar, samt ange vilka tider under dessa dagar som önskas genom att justera reglagen" WHERE help_text = "Bocka för de dagar du kan spela. Ange även tidsintervall."')
    }

    changeSet(author: "andriiromaniv", id: "1582713926") {
        sql("update user set language='en' where language = 'pl'")
    }

    changeSet(author: "viktorlevytskyi", id: "1581521260000-1") {
        addColumn(tableName: "coupon") {
            column(name: "code", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1581521260000-2") {
        addColumn(tableName: "coupon") {
            column(name: "start_date", type: "date") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1581521260000-3") {
        addColumn(tableName: "coupon") {
            column(name: "discount_amount", type: "decimal(10,2)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1581521260000-4") {
        addColumn(tableName: "coupon") {
            column(name: "discount_percent", type: "decimal(5,2)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1581521260000-5") {
        createIndex(indexName: "code_idx", tableName: "coupon") {
            column(name: "code")
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1581521260000-6") {
        addColumn(tableName: "order_refund") {
            column(name: "promo_code", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "viktorlevytskyi", id: "1585133153") {
        addColumn(tableName: "facility") {
            column(name: "enabled", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "magnus (generated)", id: "1584007309292-1") {
        createTable(tableName: "invoice_article") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "invoice_articPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "article_number", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "varchar(255)")

            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "first_price", type: "float")

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "sales_price", type: "float")

            column(name: "vat", type: "integer")
        }
    }

    changeSet(author: "magnus (generated)", id: "1584007309292-2") {
        createIndex(indexName: "type_invoice_article_id_idx", tableName: "invoice_article") {
            column(name: "facility_id")
            column(name: "article_number")
        }
    }

    changeSet(author: "martinahlberger", id: "1589966696000") {
        addColumn(tableName: "activity_occasion") {
            column(name: "delete_reason", type: "varchar(255)") {
                constraints(nullable: "true")
            }
            column(name: "deletecol", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "stan", id: "remove-payment-service-adyen-property") {
        sql("delete from facility_property where key_name = 'FEATURE_PAYMENT_SERVICE_ADYEN';")
    }

    changeSet(author: "magnus", id: "1590994005121-1") {
        sql("insert into sport (id, version, name, position, core_sport) values (11, 1, 'Innebandy', 11, false);")
    }

    changeSet(author: "aromaniv", id: "1591707584") {
        sqlFile(path: 'MW-4809-croatia-regions.sql')
    }

    changeSet(author: "petersvensson", id: "archive-for-facility") {
        addColumn(tableName: "facility") {
            column(name: "archived", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "petersvensson", id: "remove-headline-for-class-activity") {
        sql("update activity set name=headline where headline != null")
        dropColumn(columnName: "headline", tableName: "activity")
    }

    changeSet(author: "magnus (generated)", id: "1590995756254-1") {
        addColumn(tableName: "invoice_article") {
            column(name: "organization_id", type: "bigint")
        }

        dropIndex(indexName: "type_invoice_article_id_idx", tableName: "invoice_article")
        createIndex(indexName: "type_invoice_article_id_idx", tableName: "invoice_article") {
            column(name: "facility_id")
            column(name: "organization_id")
            column(name: "article_number")
        }
    }

    changeSet(author: "aromaniv", id: "1592324714") {
        sqlFile(path: 'croatia-municipalities.sql')
    }

    changeSet(author: "martinahlberger", id: "1592313121000") {
        addColumn(tableName: "user") {
            column(name: "appleuid", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
        createIndex(indexName: "appleuid_idx", tableName: "user") {
            column(name: "appleuid")
        }
    }

    changeSet(author: "magnus (generated)", id: "1593516912726-1") {
        addColumn(tableName: "user") {
            column(name: "nationality", type: "varchar(255)")
        }
    }

    changeSet(author: "magnus (generated)", id: "1593516912726-2") {
        addColumn(tableName: "customer") {
            column(name: "nationality", type: "varchar(255)")
        }
    }

    changeSet(author: "viktorlevytskyi", id: "1594044611413-1") {
        createTable(tableName: "booking_trainer") {
            column(name: "booking_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "trainer_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1594044611413-2") {
        createIndex(indexName: "unique_booking_id", tableName: "booking_trainer", unique: "true") {
            column(name: "booking_id")
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1594044611413-3") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "booking_trainer", constraintName: "booking_booking_trainer", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false")
    }
    changeSet(author: "viktorlevytskyi", id: "1594044611413-4") {
        addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "booking_trainer", constraintName: "trainer_booking_trainer", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
    }

    changeSet(author: "stan", id: "showOnline-course-activities") {
        sql("update activity set show_online=1 where class = 'course_activity';")
    }

    changeSet(author: "stan", id: "insert-bowling-shuffleboard") {
        sql("insert into sport (id, version, name, position, core_sport) values (12, 1, 'Bowling', 12, false);")
        sql("insert into sport (id, version, name, position, core_sport) values (13, 1, 'Shuffleboard', 13, false);")
    }



    changeSet(author: "martinahlberger (generated)", id: "1594703496480-1") {
        createTable(tableName: "recording_purchase") {
            column(name: "booking_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "customer_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-2") {
        addPrimaryKey(columnNames: "booking_id, customer_id", constraintName: "recording_purPK", tableName: "recording_purchase")
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-4") {
        createIndex(indexName: "FK2675D78F9195D0E", tableName: "recording_purchase") {
            column(name: "order_id")
        }
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-5") {
        createIndex(indexName: "FK2675D78F9821AE5D", tableName: "recording_purchase") {
            column(name: "booking_id")
        }
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-6") {
        createIndex(indexName: "FK2675D78F9AE61E17", tableName: "recording_purchase") {
            column(name: "customer_id")
        }
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-7") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "recording_purchase", constraintName: "FK2675D78F9821AE5D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false")
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-8") {
        addForeignKeyConstraint(baseColumnNames: "customer_id", baseTableName: "recording_purchase", constraintName: "FK2675D78F9AE61E17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "customer", referencesUniqueColumn: "false")
    }

    changeSet(author: "martinahlberger (generated)", id: "1594703496480-9") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "recording_purchase", constraintName: "FK2675D78F9195D0E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "order", referencesUniqueColumn: "false")
    }
    changeSet(author: "stan (generated)", id: "1594703496480-10") {
        addColumn(tableName: "price_list_customer_category") {
            column(name: "deleted", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
    }
    changeSet(author: "viktorlevytskyi", id: "1597383452489-1") {
        dropForeignKeyConstraint (baseTableName: "booking_trainer", constraintName: "booking_booking_trainer")
    }
    changeSet(author: "viktorlevytskyi", id: "1597383452489-2") {
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "booking_trainer", constraintName: "booking_booking_trainer", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martinahlberger", id: "1598618979480") {
        addColumn(tableName: "recording_purchase") {
            column(name: "archive_url", type: "text") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "petersvensson", id: "1599490718012") {
        sqlFile(path: 'fix_broken_private_lessons.sql')
    }
    changeSet(author: "petersvensson", id: "rename_gamecam") {
        sqlFile(path: 'rename_gamecam.sql')
    }

    changeSet(author: "aromaniv", id: "1600340945") {
        addColumn(tableName: "facility") {
            column(name: "sales_person", type: "varchar(255)")
        }
    }

    changeSet(author: "matti (generated)", id: "1601281870393-2") {
        addColumn(tableName: "camera") {
            column(name: "camera_provider", type: "varchar(255)", defaultValue: "CAAI") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "aromaniv", id: "1601031198") {
        addColumn(tableName: "activity") {
            column(name: "notify_when_sign_up", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
            column(name: "notify_when_cancel", type: "bit", defaultValueBoolean: false) {
                constraints(nullable: "false")
            }
        }
        sqlFile(path: 'set_notify_when_cancel_activity.sql')
    }

    changeSet(author: "martinahlberger", id: "1600175094001") {
        createTable(tableName: "recording_status") {
            column(name: "booking_id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "status", type: "varchar(50)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "booking_id", baseTableName: "recording_status", constraintName: "recording_status_booking", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martinahlberger (generated)", id: "1597758664061-2") {
        addColumn(tableName: "facility") {
            column(name: "facility_group_parent_id", type: "bigint")
        }
    }

    changeSet(author: "martinahlberger", id: "1601471740000-1") {
        createTable(tableName: "facility_hierarchy_group") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "invoice_articPK")
            }
            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }
            column(name: "master_facility_id", type: "bigint") {
                constraints(nullable: "true")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "master_facility_id", baseTableName: "facility_hierarchy_group", constraintName: "master_facility_facility", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martinahlberger", id: "1601471740000-2") {
        createTable(tableName: "facility_hierarchy_group_facilities") {
            column(name: "facility_group_id", type: "bigint") {
                constraints(nullable: "false")
            }
            column(name: "facility_id", type: "bigint") {
                constraints(nullable: "true")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "facility_group_id", baseTableName: "facility_hierarchy_group_facilities", constraintName: "facility_hierarchy_group_facilities_facility_group", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_hierarchy_group", referencesUniqueColumn: "false", onDelete: "cascade")
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "facility_hierarchy_group_facilities", constraintName: "facility_facility_group", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martinahlberger", id: "1601471740000-3") {
        addUniqueConstraint(tableName: "facility_hierarchy_group_facilities", columnNames: "facility_group_id,facility_id")
    }

    changeSet(author: "viktorlevytskyi", id: "1601039703000") {
        sqlFile(path: 'payout_properties.sql')
    }

    changeSet(author: "joakimolsson", id: "1602171160000") {
        sqlFile(path: 'master_facility_parent_trigger.sql', splitStatements: false)
    }

    changeSet(author: "martin", id: "1602666482000") {
        sql('''insert into sport (id, version, name, position, core_sport) values (14, 1, 'Facility resource', 99, true)''')
    }

    changeSet(author: "magnus (generated)", id: "1594630770929-2") {
        addColumn(tableName: "facility_contract_item") {
            column(name: "date_created", type: "datetime")
        }
        addColumn(tableName: "facility_contract_item") {
            column(name: "last_updated", type: "datetime")
        }
        addColumn(tableName: "facility_contract_item") {
            column(name: "article_number", type: "bigint")
        }
    }

    changeSet(author: "magnus (generated)", id: "1597409663120-2") {
        addColumn(tableName: "organization") {
            column(name: "fortnox_customer_id", type: "varchar(255)")
        }
    }

    changeSet(author: "aromaniv", id: "1603450348") {
        addColumn(tableName: "court_group") {
            column(name: "visible", type: "bit", defaultValueBoolean: true) {
                constraints(nullable: "false")
            }
            column(name: "max_number_of_bookings", type: "bigint") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "joakimolsson", id: "1604397295000") {
        preConditions(onFail: "MARK_RAN", onFailMessage: "index xi1_order_date_created already exists, skipping") {
            tableExists(tableName: "order")
            not {
                indexExists(indexName: "xi1_order_date_created")
            }
        }
        createIndex(indexName: "xi1_order_date_created", tableName: "order") {
            column(name: "date_created")
        }
    }

    changeSet(author: "matti (generated)", id: "1604486942996") {
        addColumn(tableName: "recording_status") {
            column(name: "media_url", type: "text") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "martinahlberger", id: "1603811104000") {
        createTable(tableName: "court_type_attribute") {
            column(name: "court_id", type: "bigint") {
                constraints(nullable: "false")
            }
            column(name: "court_type_enum", type: "varchar(255)") {
                constraints(nullable: "false")
            }
            column(name: "value", type: "varchar(255)") {
                constraints(nullable: "true")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "court_id", baseTableName: "court_type_attribute", constraintName: "court_type_attribute_court", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "court", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martinahlberger", id: "1603811104000-2") {
        sqlFile(path: 'set-standard-court-size-tennis-and-padel.sql')
    }

    changeSet(author: "martin", id: "1604934538000") {
        addColumn(tableName: "customer") {
            column(name: "home_facility_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "home_facility_id", baseTableName: "customer", constraintName: "customer_home_facility", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false", onDelete: "cascade")
    }

    changeSet(author: "martin", id: "1606141074000") {
        sql('''UPDATE court_type_attribute SET value = 'Single' WHERE court_id IN (select court.id from facility inner join court on facility.id = court.facility_id where court.name like '%singel%' and (court.sport_id = 5 OR court.sport_id = 1))''')
    }


    changeSet(author: "martin", id: "1606384220000") {
        dropForeignKeyConstraint(baseTableName: "customer", constraintName: "customer_home_facility")
        dropColumn(columnName: "home_facility_id", tableName: "customer")
    }

    changeSet(author: "martin", id: "1606384220000-1") {
        addColumn(tableName: "membership_type") {
            column(name: "grouped_sub_facility_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
        addForeignKeyConstraint(baseColumnNames: "grouped_sub_facility_id", baseTableName: "membership_type", constraintName: "membership_type_grouped_sub_facility", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false", onDelete: "cascade")
    }
}
