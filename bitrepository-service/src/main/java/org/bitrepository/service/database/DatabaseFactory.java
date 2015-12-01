package org.bitrepository.service.database;

import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * DatabaseFactory which knows - based on the driver class - whether to use a Derby or Postgres backend.
 * @param <T> class
 */
// FIXME:  Is tasting on the name of the driver class the right way to detect the database type?

public abstract class DatabaseFactory<T> {

    public static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String postgressDriver = "org.postgresql.Driver";
    
    /**
     * Obtain the appropriate DAO instance for the concrete backend.
     * @param ds the database specifics
     * @return the appropriate DAO instance for the concrete backend.
     * @throws UnsupportedDatabaseTypeException if the driver is not either derby or postgres
     * @see #derbyDriver
     * @see #postgressDriver
     */
    protected T getDAOInstance(DatabaseSpecifics ds) throws UnsupportedDatabaseTypeException{
        DatabaseManager dm = getDatabaseManager(ds);
        String dbDriver = ds.getDriverClass();
        if(dbDriver.equals(derbyDriver)) {
            return getDerbyDAO(dm);
        } else if(dbDriver.equals(postgressDriver)) {
            return getPostgresDAO(dm);
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
    protected abstract T getDerbyDAO(DatabaseManager dm);
    
    protected abstract T getPostgresDAO(DatabaseManager dm);
    
    protected abstract DatabaseManager getDatabaseManager(DatabaseSpecifics ds);
}
