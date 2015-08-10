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
package org.bitrepository.audittrails.store;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.DATABASE_VERSION_ENTRY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;

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
public class AuditTrailServiceDatabaseMigrator extends DatabaseMigrator {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    
    /** The name of the update script for version 1 to 2.*/
    private static final String UPDATE_SCRIPT_VERSION_1_TO_2 = "sql/derby/auditTrailServiceDBUpdate1to2.sql";
    /** The name of the update script for version 2 to 3.*/
    private static final String UPDATE_SCRIPT_VERSION_2_TO_3 = "sql/derby/auditTrailServiceDBUpdate2to3.sql";
    /** The name of the update script for version 3 to 4.*/
    private static final String UPDATE_SCRIPT_VERSION_3_TO_4 = "sql/derby/auditTrailServiceDBUpdate3to4.sql";
    /** The name of the update script for version 4 to 5.*/
    private static final String UPDATE_SCRIPT_VERSION_4_TO_5 = "sql/derby/auditTrailServiceDBUpdate4to5.sql";
    /** The current version of the database. */
    private final Integer currentVersion = 5;
    
    /**
     * Constructor.
     * @param connector connection to the database.
     */
    public AuditTrailServiceDatabaseMigrator(DBConnector connector) {
        super(connector);
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
            throw new IllegalStateException("The database does not contain '" + ACTOR_TABLE 
                    + "' table as required.");
        }
        
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 2) {
            log.warn("Migrating AuditServiceDB from version 1 to 2.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_1_TO_2);
        }
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 3) {
            log.warn("Migrating AuditServiceDB from version 2 to 3.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_2_TO_3);
        }
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 4) {
            log.warn("Migrating AuditServiceDB from version 3 to 4.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_3_TO_4);
        }
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 5) {
            log.warn("Migrating AuditServiceDB from version 4 to 5.");
            migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_4_TO_5);
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
