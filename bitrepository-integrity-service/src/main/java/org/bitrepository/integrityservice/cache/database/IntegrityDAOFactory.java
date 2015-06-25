package org.bitrepository.integrityservice.cache.database;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.service.database.DatabaseManager;


/**
 * Factory class to obtain the appropriate type of DAO class for the specified database type 
 */
public class IntegrityDAOFactory {

    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgressDriver = "org.postgresql.Driver";
    
    public static IntegrityDAO getDAO2Instance(Settings settings) {
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        String dbDriver = settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase().getDriverClass(); 
        if(dbDriver.equals(derbyDriver)) {
            return new DerbyIntegrityDAO(dm.getConnector(), settings);    
        } else if(dbDriver.equals(postgressDriver)) {
            return new PostgresIntegrityDAO(dm.getConnector(), settings);
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
}
