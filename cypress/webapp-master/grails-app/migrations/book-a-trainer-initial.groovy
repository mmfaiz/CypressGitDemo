databaseChangeLog = {

	changeSet(author: "mattias (generated)", id: "1520415731527-5") {
		createTable(tableName: "request") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "requestPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "message", type: "varchar(255)")

			column(name: "requester_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "end", type: "datetime")

			column(name: "start", type: "datetime")

			column(name: "trainer_id", type: "bigint")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-6") {
		createTable(tableName: "trainer_availability") {
			column(name: "trainer_availabilities_id", type: "bigint")

			column(name: "availability_id", type: "bigint")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-8") {
		addColumn(tableName: "trainer") {
			column(name: "is_bookable", type: "bit")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-151") {
		createIndex(indexName: "FK414EF28F1AF23500", tableName: "request") {
			column(name: "trainer_id")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-152") {
		createIndex(indexName: "FK414EF28F706BFC26", tableName: "request") {
			column(name: "requester_id")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-157") {
		createIndex(indexName: "FKAD8F0A51E3BBAF2", tableName: "trainer_availability") {
			column(name: "trainer_availabilities_id")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-158") {
		createIndex(indexName: "FKAD8F0A5D0BAFA77", tableName: "trainer_availability") {
			column(name: "availability_id")
		}
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-125") {
		addForeignKeyConstraint(baseColumnNames: "requester_id", baseTableName: "request", constraintName: "FK414EF28F706BFC26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-126") {
		addForeignKeyConstraint(baseColumnNames: "trainer_id", baseTableName: "request", constraintName: "FK414EF28F1AF23500", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-131") {
		addForeignKeyConstraint(baseColumnNames: "availability_id", baseTableName: "trainer_availability", constraintName: "FKAD8F0A5D0BAFA77", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "availability", referencesUniqueColumn: "false")
	}

	changeSet(author: "mattias (generated)", id: "1520415731527-132") {
		addForeignKeyConstraint(baseColumnNames: "trainer_availabilities_id", baseTableName: "trainer_availability", constraintName: "FKAD8F0A51E3BBAF2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trainer", referencesUniqueColumn: "false")
	}
}
