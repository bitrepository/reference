package org.bitrepository.common.database;

import java.io.File;
import java.sql.Connection;

public interface DBConnector {
    /**
     * Creates an embedded connection to a Derby database through the given URL.
     * @param dbUrl The URL to connect to the database with.
     * @return The connection to the database.
     * @throws Exception If problems with the instantiation of the connection database occurs.
     * E.g. The connection cannot be instantiated, or the driver is not available.
     */
    Connection getEmbeddedDBConnection(String dbUrl);
    
    /**
     * Instantiates the database from a SQL file.
     * @param sqlDatabaseFile The SQL file to instantiate the database from.
     */
    void createDatabase(File sqlDatabaseFile);
}
