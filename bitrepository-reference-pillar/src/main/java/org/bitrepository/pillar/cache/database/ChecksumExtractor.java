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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;

/**
 * Extracts data from the checksum database.
 */
public class ChecksumExtractor {
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param connector The connector for the database.
     */
    public ChecksumExtractor(DBConnector connector) {
        this.connector = connector;
    }
    
    /**
     * Extracts the date for a given file.
     * @param fileId The id of the file to extract the date for.
     * @return The date for the given file.
     */
    public Date extractDateForFile(String fileId) {
        String sql = "SELECT " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ?";
        return DatabaseUtils.selectDateValue(connector, sql, fileId);
    }
    
    /**
     * Extracts the checksum for a given file.
     * @param fileId The id of the file to extract the checksum for.
     * @return The checksum for the given file.
     */
    public String extractChecksumForFile(String fileId) {
        String sql = "SELECT " + CS_CHECKSUM + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ?";
        return DatabaseUtils.selectStringValue(connector, sql, fileId);
    }
    
    /**
     * Extracts whether a given file exists.
     * @param fileId The id of the file to extract whose existence is in question.
     * @return Whether the given file exists.
     */
    public boolean hasFile(String fileId) {
        String sql = "SELECT COUNT(*) FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ?";
        return DatabaseUtils.selectIntValue(connector, sql, fileId) != 0;
    }
    
    /**
     * Extracts all the file ids in the table.
     * @return The list of file ids.
     */
    public List<String> getAllFileIDs() {
        String sql = "SELECT " + CS_FILE_ID + " FROM " + CHECKSUM_TABLE;
        return DatabaseUtils.selectStringList(connector, sql, new Object[0]);
    }
    
    /**
     * Extracts the checksum entry for a single file.
     * @param fileId The id of the file whose checksum entry should be extracted.
     * @return The checksum entry for the file.
     */
    public ChecksumEntry extractSingleEntry(String fileId) {
        String sql = "SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE 
                + " WHERE " + CS_FILE_ID + " = ?";
        try {
            Connection con = null;
            try {
                con = connector.getConnection();
                ResultSet res = DatabaseUtils.selectObject(con, sql, fileId);
                if(!res.next()) {
                    throw new IllegalStateException("No entry for the file '" + fileId + "'.");
                }
                return extractChecksumEntry(res);
            } finally {
                if(con != null) {
                    con.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the ChecksumEntry for '" + fileId + "'", e);
        }
    }

    /**
     * Extracts all the checksum entries in the database.
     * @return The checksum entry for the file.
     */
    public List<ChecksumEntry> extractAllEntries() {
        String sql = "SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE;
        try {
            Connection con = null;
            List<ChecksumEntry> res = new ArrayList<ChecksumEntry>();
            try {
                con = connector.getConnection();
                ResultSet resSet = DatabaseUtils.selectObject(con, sql, new Object[0]);
                while(resSet.next()) {
                    res.add(extractChecksumEntry(resSet));
                }
                return res;
            } finally {
                if(con != null) {
                    con.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract all the ChecksumEntries", e);
        }
    }
    
    /**
     * Extracts a checksum entry from a result set. 
     * The result set needs to have requested the elements in the right order:
     *  - File id.
     *  - Checksum.
     *  - Date.
     * 
     * @param resSet The resultset from the database.
     * @return The checksum entry extracted from the result set.
     */
    private ChecksumEntry extractChecksumEntry(ResultSet resSet) throws SQLException {
        String fileId = resSet.getString(1);
        String checksum = resSet.getString(2);
        Date date = resSet.getTimestamp(3);
        return new ChecksumEntry(fileId, checksum, date);
    }
}
