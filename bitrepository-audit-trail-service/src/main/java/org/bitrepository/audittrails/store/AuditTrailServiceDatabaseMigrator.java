package org.bitrepository.audittrails.store;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.DATABASE_VERSION_ENTRY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;

import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration class for the AuditTrailDatabase of the AuditTrailService.
 */
public class AuditTrailServiceDatabaseMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    /** The settings.*/
    private final Settings settings;
    
    /**
     * Constructor.
     * @param connector connection to the database.
     * @param settings The settings.
     */
    public AuditTrailServiceDatabaseMigrator(DBConnector connector, Settings settings) {
        super(connector);
        this.settings = settings;
    }
    
    @Override
    public void migrate() {
        Map<String, Integer> versions = getTableVersions();
        
        if(!versions.containsKey(FILE_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + FILE_TABLE 
                    + "' table as required.");
        }
        if(!versions.containsKey(AUDITTRAIL_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + AUDITTRAIL_TABLE 
                    + "' table as required.");
        }
        if(!versions.containsKey(CONTRIBUTOR_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + CONTRIBUTOR_TABLE 
                    + "' table as required.");
        }
        if(!versions.containsKey(ACTOR_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + AUDITTRAIL_TABLE 
                    + "' table as required.");
        }
        
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 2) {
            migrateVersion1To2();
        }
    }
    
    /**
     * Migrate from version 1 to version 2 with the appropriate script.
     * 
     * The script contains a placeholder for the default collection (e.g. first defined collection). 
     * This placeholder must be replaced according to the script.
     */
    private void migrateVersion1To2() {
        String defaultCollection = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        log.warn("Database outdated. Updating from version 1 to version 2, where all entries are set as belonging to "
                + "the collection '" + defaultCollection + "'");

        // How to do this?
        throw new IllegalStateException("Not implemented!!!");
    }
}
