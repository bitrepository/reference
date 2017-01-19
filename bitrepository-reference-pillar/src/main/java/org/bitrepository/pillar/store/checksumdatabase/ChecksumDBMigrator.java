/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.pillar.store.checksumdatabase;

import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_COLLECTION_ID;

/**
 * Migration class for the ChecksumDatabase of the ReferencePillar and ChecksumPillar.
 */
public class ChecksumDBMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    /** The settings.*/
    private final Settings settings;
    /** The current version of the database. */
    private final Integer currentVersion = 4;
    /** The name of the update script for version 2 to 3.*/
    private static final String UPDATE_SCRIPT_VERSION_2_TO_3 = "sql/derby/checksumDB2to3Migration.sql";
    /** The name of the update script for version 3 to 4.*/
    private static final String UPDATE_SCRIPT_VERSION_3_TO_4 = "sql/derby/checksumDB3to4Migration.sql";
    
    /**
     * @param connector The connection to the database.
     * @param settings The settings.
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

        if(!versions.containsKey(CHECKSUM_TABLE) || versions.get(CHECKSUM_TABLE) < 3) {
            log.warn("Migrating ChecksumDB from version 2 to 3.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_2_TO_3);
        }
        if(!versions.containsKey(CHECKSUM_TABLE) || versions.get(CHECKSUM_TABLE) < 4) {
            log.warn("Migrating ChecksumDB from version 3 to 4.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_3_TO_4);
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

    @Override
    public boolean needsMigration() {
        Map<String, Integer> versions = getTableVersions();
        
        if(!versions.containsKey(CHECKSUM_TABLE)) {
            throw new IllegalStateException("The database does not contain '" + CHECKSUM_TABLE 
                    + "' table as required.");
        }
                
        return versions.get(CHECKSUM_TABLE) < currentVersion;
    }
}
