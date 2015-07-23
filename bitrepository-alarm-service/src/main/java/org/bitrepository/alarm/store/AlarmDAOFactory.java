package org.bitrepository.alarm.store;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.UnsupportedDatabaseTypeException;

/**
 * Factory class for obtaining the appropriate DAO for the specific database backend 
 */
public class AlarmDAOFactory {
    
    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgressDriver = "org.postgresql.Driver";
    
    public static AlarmServiceDAO getAlarmServiceDAOInstance(Settings settings) {
        AlarmDatabaseManager dm = new AlarmDatabaseManager(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());
        String dbDriver = settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase().getDriverClass();
        if(dbDriver.equals(derbyDriver)) {
            return new DerbyAlarmServiceDAO(dm);
        } else if(dbDriver.equals(postgressDriver)) {
            return new PostgresAlarmServiceDAO(dm);
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
}
