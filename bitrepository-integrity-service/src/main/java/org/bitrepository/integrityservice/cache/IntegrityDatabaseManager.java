package org.bitrepository.integrityservice.cache;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Manager for the database of the IntegrityService. For usage, see @DatabaseManager. 
 */
public class IntegrityDatabaseManager extends DatabaseManager {

    private static final String INTEGRITY_SERVICE_DATABASE_SCHEMA = "sql/derby/integrityDBCreation.sql";
    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator = null;
        
    public IntegrityDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }
    
    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected synchronized DatabaseMigrator getMigrator() {
        if(migrator == null) {
            migrator = new IntegrityDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration();
    }

    @Override
    protected String getDatabaseCreationScript() {
        return INTEGRITY_SERVICE_DATABASE_SCHEMA;
    }

}
