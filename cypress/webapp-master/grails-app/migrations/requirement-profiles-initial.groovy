databaseChangeLog = {
    changeSet(author: "mattias (generated)", id: "1516831223633-5") {
        createTable(tableName: "group_requirement") {
            column(name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "requirementPK")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "profile_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "all_selected_groups", type: "bit")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-6") {
        createTable(tableName: "group_requirement_facility_group") {
            column(name: "group_requirement_groups_id", type: "bigint")

            column(name: "group_id", type: "bigint")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-7") {
        createTable(tableName: "is_active_member_requirement") {
            column(name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "requirementPK")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "profile_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "membership_required", type: "bit")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-8") {
        createTable(tableName: "member_type_requirement") {
            column(name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "requirementPK")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "profile_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-9") {
        createTable(tableName: "member_type_requirement_membership_type") {
            column(name: "member_type_requirement_types_id", type: "bigint")

            column(name: "membership_type_id", type: "bigint")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-10") {
        createTable(tableName: "requirement_profile") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "requirement_pPK")
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

            column(name: "name", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-12") {
        addColumn(tableName: "form") {
            column(name: "requirement_profile_id", type: "bigint")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-159") {
        createIndex(indexName: "FK300CC4EB438D74", tableName: "form") {
            column(name: "requirement_profile_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-160") {
        createIndex(indexName: "FK15A8DC43AB714BF861b56a83", tableName: "group_requirement") {
            column(name: "profile_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-161") {
        createIndex(indexName: "FK8D034F1F4843295D", tableName: "group_requirement_facility_group") {
            column(name: "group_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-162") {
        createIndex(indexName: "FK8D034F1FC208EDCF", tableName: "group_requirement_facility_group") {
            column(name: "group_requirement_groups_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-164") {
        createIndex(indexName: "FK15A8DC43AB714BF848468122", tableName: "is_active_member_requirement") {
            column(name: "profile_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-165") {
        createIndex(indexName: "FK15A8DC43AB714BF8d94ae1c3", tableName: "member_type_requirement") {
            column(name: "profile_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-166") {
        createIndex(indexName: "FK22CB87875D3CBAE2", tableName: "member_type_requirement_membership_type") {
            column(name: "membership_type_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-167") {
        createIndex(indexName: "FK22CB8787AE8A2C0B", tableName: "member_type_requirement_membership_type") {
            column(name: "member_type_requirement_types_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-168") {
        createIndex(indexName: "FK51419ADDDF0EBF7", tableName: "requirement_profile") {
            column(name: "facility_id")
        }
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-129") {
        addForeignKeyConstraint(baseColumnNames: "requirement_profile_id", baseTableName: "form", constraintName: "FK300CC4EB438D74", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "requirement_profile", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-130") {
        addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "group_requirement", constraintName: "FK15A8DC43AB714BF861b56a83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "requirement_profile", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-131") {
        addForeignKeyConstraint(baseColumnNames: "group_id", baseTableName: "group_requirement_facility_group", constraintName: "FK8D034F1F4843295D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility_group", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-132") {
        addForeignKeyConstraint(baseColumnNames: "group_requirement_groups_id", baseTableName: "group_requirement_facility_group", constraintName: "FK8D034F1FC208EDCF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "group_requirement", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-133") {
        addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "is_active_member_requirement", constraintName: "FK15A8DC43AB714BF848468122", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "requirement_profile", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-134") {
        addForeignKeyConstraint(baseColumnNames: "profile_id", baseTableName: "member_type_requirement", constraintName: "FK15A8DC43AB714BF8d94ae1c3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "requirement_profile", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-135") {
        addForeignKeyConstraint(baseColumnNames: "member_type_requirement_types_id", baseTableName: "member_type_requirement_membership_type", constraintName: "FK22CB8787AE8A2C0B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "member_type_requirement", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-136") {
        addForeignKeyConstraint(baseColumnNames: "membership_type_id", baseTableName: "member_type_requirement_membership_type", constraintName: "FK22CB87875D3CBAE2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "membership_type", referencesUniqueColumn: "false")
    }

    changeSet(author: "mattias (generated)", id: "1516831223633-137") {
        addForeignKeyConstraint(baseColumnNames: "facility_id", baseTableName: "requirement_profile", constraintName: "FK51419ADDDF0EBF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "facility", referencesUniqueColumn: "false")
    }
}
