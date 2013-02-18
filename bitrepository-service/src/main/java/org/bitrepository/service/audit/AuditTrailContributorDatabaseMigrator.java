package org.bitrepository.service.audit;

import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_COLLECTIONID;

import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration class for the ChecksumDatabase of the ReferencePillar and ChecksumPillar.
 */
public class AuditTrailContributorDatabaseMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    /** The settings.*/
    private final Settings settings;
    
    /**
     * Constructor.
     * @param connector connection to the database.
     * @param settings The settings.
     */
    public AuditTrailContributorDatabaseMigrator(DBConnector connector, Settings settings) {
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
        if(versions.get(FILE_TABLE) == 1) {
            migrateFileTableFromVersion1To2();
        }
    }
    
    /**
     * Migrate the 'file' table from version 1 to 2.
     * Just adds the column 'collectionid', which will be set to the current (or first) collection id.
     */
    private void migrateFileTableFromVersion1To2() {
        log.warn("Migrating the " + FILE_TABLE + " table from version 1 to 2 in the AuditTrailContributorDatabase.");
        
        String alterSql = "ALTER TABLE " + FILE_TABLE + " ADD COLUMN " + FILE_COLLECTIONID + " VARCHAR(255)";
        updateTable(FILE_TABLE, 2, alterSql, new Object[0]);
        
        String updateAfterwards = "UPDATE " + FILE_TABLE + " SET " + FILE_COLLECTIONID + " = ? WHERE " 
                + FILE_COLLECTIONID + " IS NULL";
        DatabaseUtils.executeStatement(connector, updateAfterwards, settings.getCollectionID());
    }
}
