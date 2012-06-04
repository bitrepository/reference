package org.bitrepository.common.database;

/**
 * Specifies the name of the driver for the PostgreSQL database.
 */
public class PostgreSQLConnector implements DBSpecifics {
    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }
}
