package org.bitrepository.common.database;

/**
 * Interface for specifying the name of the driver class for a database connection.
 */
public interface DBSpecifics {
    /**
     * @return The name of the specific driver.
     */
    String getDriverClassName();
}
