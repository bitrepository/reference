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
        if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 3) {
            migrateVersion2To3();
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
        throw new IllegalStateException("Must update database from version 1 to 2, but this has "
                + "not been implemented!!!");
    }
    
    /**
     * Migrate from version 2 to version 3 with the appropriate script.
     * ...
     */
    private void migrateVersion2To3() {
        // How to do this?
        throw new IllegalStateException("Must update database from version 2 to 3, but this has "
                + "not been implemented!!!");
    }
}
