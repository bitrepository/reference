/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;

import java.util.List;

/**
 * DAO class for read access to the AuditTrail database for test purposes  
 */
public class AuditTrailReadDAO {

    private DBConnector dbConnector;
    
    public AuditTrailReadDAO(DatabaseManager databaseManager) {
        dbConnector = databaseManager.getConnector();
    }
    
    public List<String> getCollectionIDs() {
        String selectSql = "SELECT " + COLLECTION_ID + " FROM " + COLLECTION_TABLE;
        
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }
    
    public List<String> getFileIDs(String collectionID) {
        String selectSql = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE
                + " JOIN " + COLLECTION_TABLE 
                + " ON " + FILE_TABLE + "." + FILE_COLLECTION_KEY + " = " + COLLECTION_TABLE + "." + COLLECTION_KEY
                + " WHERE " + COLLECTION_ID + " = ?";

        return DatabaseUtils.selectStringList(dbConnector, selectSql, collectionID);
    }
    
    public List<String> getContributorIDs() {
        String selectSql = "SELECT " + CONTRIBUTOR_ID + " FROM " + CONTRIBUTOR_TABLE;
        
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }
    
    public List<String> getActorNames() {
        String selectSql = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE;
        
        return DatabaseUtils.selectStringList(dbConnector, selectSql, new Object[0]);
    }
}
