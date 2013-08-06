/*
 * #%L
 * Bitrepository Service
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
package org.bitrepository.service.audit;

import java.util.Map;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_COLLECTIONID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.DATABASE_VERSION_ENTRY;

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
            upgradeFromVersion1To2();
        }
        
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 3) {
            upgradeFromVersion2To3();
        }
    }
    
    /**
     * Migrate the 'file' table from version 1 to 2.
     * Just adds the column 'collectionid', which will be set to the current (or first) collection id.
     */
    private void upgradeFromVersion1To2() {
        log.warn("Migrating the " + FILE_TABLE + " table from version 1 to 2 in the AuditTrailContributorDatabase.");
        
        String alterSql = "ALTER TABLE " + FILE_TABLE + " ADD COLUMN " + FILE_COLLECTIONID + " VARCHAR(255)";
        updateTable(FILE_TABLE, 2, alterSql, new Object[0]);
        
        String updateAfterwards = "UPDATE " + FILE_TABLE + " SET " + FILE_COLLECTIONID + " = ? WHERE " 
                + FILE_COLLECTIONID + " IS NULL";
        DatabaseUtils.executeStatement(connector, updateAfterwards, settings.getCollections().get(0).getID());
    }
    
    /**
     * Method for upgrading the database from version 2 to 3.
     */
    private void upgradeFromVersion2To3() {
        throw new IllegalStateException("The database needs to be updated to version 3. This must be done manually.");
    }
}
