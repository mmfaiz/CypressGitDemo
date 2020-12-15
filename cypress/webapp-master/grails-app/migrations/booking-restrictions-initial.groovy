databaseChangeLog = {
    changeSet(author: "mattias (generated)", id: "1517513357775-4") {
        createTable(tableName: "booking_restriction") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "booking_restrPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "valid_until_min_before_start", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-5") {
        createTable(tableName: "booking_restriction_requirement_profiles") {
            column(name: "profile_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "restriction_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-15") {
        addColumn(tableName: "slot") {
            column(name: "booking_restriction_id", type: "bigint") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-94") {
        addPrimaryKey(columnNames: "profile_id, restriction_id", tableName: "booking_restriction_requirement_profiles")
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-163") {
        createIndex(indexName: "FKFBE8A61FAB714BF8", tableName: "booking_restriction_requirement_profiles") {
            column(name: "profile_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-164") {
        createIndex(indexName: "FKFBE8A61FF3748CFE", tableName: "booking_restriction_requirement_profiles") {
            column(name: "restriction_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-178") {
        createIndex(indexName: "FK35E9FEB14B15A4", tableName: "slot") {
            column(name: "booking_restriction_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-127") {
        addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "booking_restriction_requirement_profiles", constraintName: "FKFBE8A61FAB714BF8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "requirement_profile", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-128") {
        addForeignKeyConstraint(baseColumnNames: "restriction_id", baseTableName: "booking_restriction_requirement_profiles", constraintName: "FKFBE8A61FF3748CFE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking_restriction", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1517513357775-144") {
        addForeignKeyConstraint(baseColumnNames: "booking_restriction_id", baseTableName: "slot", constraintName: "FK35E9FEB14B15A4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "booking_restriction", referencesUniqueColumn: "false")
    }
}