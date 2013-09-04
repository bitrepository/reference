package org.bitrepository.alarm.store;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public class AlarmDatabaseManager extends DatabaseManager {

    public final static String ALARM_SERVICE_DATABASE_SCHEMA = "sql/derby/alarmServiceDBCreation.sql";
    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator = null;
    
    public AlarmDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }
    
    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected DatabaseMigrator getMigrator() {
        if(migrator == null) {
            migrator = new AlarmDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration();
    }

    @Override
    protected String getDatabaseCreationScript() {
        return ALARM_SERVICE_DATABASE_SCHEMA;
    }

}
