/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.integrityservice.cache;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.DATABASE_VERSION_ENTRY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILEINFO_TABLE_VERSION_ENTRY;

import java.util.Map;

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migration class for the AuditTrailDatabase of the AuditTrailService.
 * Will only try to perform the migration on an embedded derby database.
 */
public class IntegrityDatabaseMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    
    /** The name of the update script for version 1 to 2.*/
    private static final String UPDATE_SCRIPT_VERSION_1_TO_2 = "sql/derby/integrityDB1to2migration.sql";
    /** The name of the update script for version 2 to 3.*/
    private static final String UPDATE_SCRIPT_VERSION_2_TO_3 = "sql/derby/integrityDB2to3migration.sql";
    /** The name of the update script for version 3 to 4.*/
    private static final String UPDATE_SCRIPT_VERSION_3_TO_4 = "sql/derby/integrityDB3to4migration.sql";
    /** The current version of the database. */
    private final Integer currentVersion = 4;
    
    /**
     * Constructor.
     * @param connector connection to the database.
     */
    public IntegrityDatabaseMigrator(DBConnector connector) {
        super(connector);
    }
    
    @Override
    public void migrate() {
        Map<String, Integer> versions = getTableVersions();
        
        if(!versions.containsKey(FILEINFO_TABLE_VERSION_ENTRY)) {
            throw new IllegalStateException("The database does not contain '" + FILEINFO_TABLE_VERSION_ENTRY 
                    + "' version entry as required.");
        }
        if(!versions.containsKey(DATABASE_VERSION_ENTRY)) {
            throw new IllegalStateException("The database does not contain '" + DATABASE_VERSION_ENTRY 
                    + "' version entry as required.");
        }
        
        if(versions.get(DATABASE_VERSION_ENTRY) < 2) {
            throw new IllegalStateException("The integrityDB is of version 1, migration is not supported. "
                    + "Create a new database, or handle migration by hand. ");
        }
        if(versions.get(DATABASE_VERSION_ENTRY) < 3) {
            log.warn("Migrating integrityDB from version 2 to 3.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_2_TO_3);
        }
        
        versions = getTableVersions();
        if(versions.get(DATABASE_VERSION_ENTRY) < 4 && versions.get(FILEINFO_TABLE_VERSION_ENTRY).equals(3)) {
            log.warn("Migrating integrityDB from version 3 to 4.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_3_TO_4);
        }
    }

    @Override
    public boolean needsMigration() {
        Map<String, Integer> versions = getTableVersions();
        
        if(!versions.containsKey(DATABASE_VERSION_ENTRY)) {
            throw new IllegalStateException("The database does not contain '" + DATABASE_VERSION_ENTRY 
                    + "' table as required.");
        }
        
        if(versions.get(DATABASE_VERSION_ENTRY) < currentVersion) {
            return true;
        } else {
            return false;
        }
    }
}
