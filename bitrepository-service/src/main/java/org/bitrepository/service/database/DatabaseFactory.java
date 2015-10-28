package org.bitrepository.service.database;

import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public abstract class DatabaseFactory {

    public static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String postgressDriver = "org.postgresql.Driver";
    
    /**
     * Obtain the appropriate DAO instance for the concrete backend. 
     */
    public DAO getDAOInstance(DatabaseSpecifics ds) {
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
    
    protected abstract DAO getDerbyDAO(DatabaseManager dm);
    
    protected abstract DAO getPostgresDAO(DatabaseManager dm);
    
    protected abstract DatabaseManager getDatabaseManager(DatabaseSpecifics ds);
}
