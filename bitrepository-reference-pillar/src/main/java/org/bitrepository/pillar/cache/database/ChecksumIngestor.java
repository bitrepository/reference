/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.cache.database;

import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_FILE_ID;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_CHECKSUM;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_DATE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_COLLECTION_ID;

import java.util.Date;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

/**
 * Ingests data to the checksum database. And also deals with the deletion of entries.
 * This will only handle the ingest operations for one collection id.
 */
public class ChecksumIngestor {
    /** The connector for the database.*/
    private final DBConnector connector;
    /** The collection id for this ingestor.*/
    private final String collectionId;
    
    /**
     * Constructor.
     * @param connector The connector for the database.
     */
    public ChecksumIngestor(DBConnector connector, String collectionId) {
        ArgumentValidator.checkNotNull(connector, "DBConnector connector");
        ArgumentValidator.checkNotNullOrEmpty("String collectionId", collectionId);
        
        this.connector = connector;
        this.collectionId = collectionId;
    }
    
    /**
     * Inserts a new entry into the database.
     * @param fileId The id of the file for the entry.
     * @param checksum The checksum of the file.
     * @param date The calculation timestamp for the file.
     */
    public synchronized void insertNewEntry(String fileId, String checksum, Date date) {
        String sql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE 
                + " , " + CS_COLLECTION_ID + " ) VALUES ( ? , ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, sql, fileId, checksum, date, collectionId);
    }
    
    /**
     * Updates an existing entry in the database.
     * @param fileId The id of the file to update.
     * @param checksum The new checksum for the file.
     * @param date The new date for the calculation of the checksum of the file.
     */
    public void updateEntry(String fileId, String checksum, Date date) {
        String sql = "UPDATE " + CHECKSUM_TABLE + " SET " + CS_CHECKSUM + " = ? , " + CS_DATE + " = ? WHERE " 
                + CS_FILE_ID + " = ? AND " + CS_COLLECTION_ID + " = ?";
        DatabaseUtils.executeStatement(connector, sql, checksum, date, fileId, collectionId);
    }
    
    /**
     * Removes an entry from the database.
     * @param fileId The id of the file whose entry should be removed.
     */
    public void removeEntry(String fileId) {
        String sql = "DELETE FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND " + CS_COLLECTION_ID + " = ?";
        DatabaseUtils.executeStatement(connector, sql, fileId, collectionId);
    }
}
