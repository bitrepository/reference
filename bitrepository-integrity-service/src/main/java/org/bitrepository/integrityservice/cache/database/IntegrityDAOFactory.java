package org.bitrepository.integrityservice.cache.database;

import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;


/**
 * Factory class to obtain the appropriate type of DAO class for the specified database type 
 */
public class IntegrityDAOFactory extends DatabaseFactory<IntegrityDAO> {

    public IntegrityDAO getIntegrityDAOInstance(DatabaseSpecifics ds) {
        IntegrityDAO dao = getDAOInstance(ds);
        return dao;
    }

    @Override
    protected IntegrityDAO getDerbyDAO(DatabaseManager dm) {
        return new DerbyIntegrityDAO(dm.getConnector());
    }

    @Override
    protected IntegrityDAO getPostgresDAO(DatabaseManager dm) {
        return new PostgresIntegrityDAO(dm.getConnector());
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new IntegrityDatabaseManager(ds);
    }
    
}
