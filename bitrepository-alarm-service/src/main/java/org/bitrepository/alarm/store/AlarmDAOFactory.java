package org.bitrepository.alarm.store;

import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Factory class for obtaining the appropriate DAO for the specific database backend 
 */
public class AlarmDAOFactory extends DatabaseFactory<AlarmServiceDAO> {
    
    /**
     * Obtain an instance of AlarmServiceDAO appropriate for the specific database backend.
     * @param ds the specific database backend
     * @return an instance of AlarmServiceDAO appropriate for the specific database backend.
     */
    public AlarmServiceDAO getAlarmServiceDAOInstance(DatabaseSpecifics ds) {
        AlarmServiceDAO dao = getDAOInstance(ds);
        return dao;
    }

    @Override
    protected AlarmServiceDAO getDerbyDAO(DatabaseManager dm) {
        return new DerbyAlarmServiceDAO(dm);
    }

    @Override
    protected AlarmServiceDAO getPostgresDAO(DatabaseManager dm) {
        return new PostgresAlarmServiceDAO(dm);
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new AlarmDatabaseManager(ds);
    }
    
}
