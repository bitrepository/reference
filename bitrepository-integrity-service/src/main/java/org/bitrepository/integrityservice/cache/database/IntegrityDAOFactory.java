package org.bitrepository.integrityservice.cache.database;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;


/**
 * Factory class to obtain the appropriate type of DAO class for the specified database type 
 */
public class IntegrityDAOFactory {

    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgressDriver = "org.postgresql.Driver";
    
    public static IntegrityDAO getDAOInstance(Settings settings) {
        String dbDriver = settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase().getDriverClass(); 
        if(dbDriver.equals(derbyDriver)) {
            return new DerbyIntegrityDAO(new DBConnector(
                    settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                    settings.getRepositorySettings().getCollections());    
        } else if(dbDriver.equals(postgressDriver)) {
            return new PostgresIntegrityDAO(new DBConnector(
                    settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                    settings.getRepositorySettings().getCollections());
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
        
        
    }
}
