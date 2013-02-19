package org.bitrepository.pillar.cache;

import java.util.Map;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_COLLECTION_ID;

/**
 * Migration class for the ChecksumDatabase of the ReferencePillar and ChecksumPillar.
 */
public class ChecksumDBMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    /** The settings.*/
    private final Settings settings;
    
    /**
     * Constructor.
     * @param connector
     * @param settings
     */
    public ChecksumDBMigrator(DBConnector connector, Settings settings) {
        super(connector);
        this.settings = settings;
    }
    
    @Override
    public void migrate() {
        Map<String, Integer> versions = getTableVersions();
        
        if(!versions.containsKey(CHECKSUM_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + CHECKSUM_TABLE 
                    + "' table as required.");
        }
        if(versions.get(CHECKSUM_TABLE) == 1) {
            migrateChecksumsTableFromVersion1To2();
        }
    }
    
    /**
     * Migrate the ChecksumTable from version 1 to 2.
     * Just adds the column 'collectionid', which is set to the current (or first) collection id.
     */
    private void migrateChecksumsTableFromVersion1To2() {
        log.warn("Migrating the " + CHECKSUM_TABLE + " table from version 1 to 2 in the ChecksumDatabase.");
        
        String alterSql = "ALTER TABLE " + CHECKSUM_TABLE + " ADD COLUMN " + CS_COLLECTION_ID + " VARCHAR(255)";
        updateTable(CHECKSUM_TABLE, 2, alterSql, new Object[0]);
        
        String updateAfterwards = "UPDATE " + CHECKSUM_TABLE + " SET " + CS_COLLECTION_ID + " = ? WHERE " 
                + CS_COLLECTION_ID + " IS NULL";
        DatabaseUtils.executeStatement(connector, updateAfterwards, settings.getCollections().get(0).getID());
    }
}
