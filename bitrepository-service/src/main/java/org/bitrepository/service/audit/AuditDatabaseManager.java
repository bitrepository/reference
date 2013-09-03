package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public class AuditDatabaseManager extends DatabaseManager {

    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator;
    public final static String AUDIT_CONTRIBUTOR_DATABASE_SCHEMA = "sql/derby/auditContributorDBCreation.sql";
    
    public AuditDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }
    
    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected synchronized DatabaseMigrator getMigrator() {
        if(migrator == null) {
            migrator = new AuditTrailContributorDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration();
    }

    @Override
    protected String getDatabaseCreationScript() {
        return AUDIT_CONTRIBUTOR_DATABASE_SCHEMA;
    }

}
