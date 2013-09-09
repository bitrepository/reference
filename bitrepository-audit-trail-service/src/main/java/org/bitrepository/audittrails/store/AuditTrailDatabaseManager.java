package org.bitrepository.audittrails.store;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Manager for the database of the AuditTrailService. For usage, see @DatabaseManager. 
 */
public class AuditTrailDatabaseManager extends DatabaseManager {

    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator = null;
    
    public AuditTrailDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }
    
    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected synchronized DatabaseMigrator getMigrator() {
        if(migrator == null) {
            migrator = new AuditTrailServiceDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration(); 
    }

    @Override
    protected String getDatabaseCreationScript() {
        return AuditTrailDatabaseCreator.DEFAULT_AUDIT_TRAIL_DB_SCRIPT;
    }

}
