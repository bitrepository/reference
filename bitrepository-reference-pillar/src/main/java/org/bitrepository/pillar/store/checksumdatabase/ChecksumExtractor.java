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
package org.bitrepository.pillar.store.checksumdatabase;

import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_CHECKSUM;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_COLLECTION_ID;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_DATE;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_FILE_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts data from the checksum database.
 */
public class ChecksumExtractor {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The default amount of results to fetch out of the database for each call.*/
    protected static final int DEFAULT_FETCH_SIZE = 100;
    
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param connector The connector for the database.
     */
    public ChecksumExtractor(DBConnector connector) {
        ArgumentValidator.checkNotNull(connector, "DBConnector connector");
        
        this.connector = connector;
    }
    
    /**
     * Extracts the date for a given file.
     * @param fileID The id of the file to extract the date for.
     * @param collectionID The collection id for the file.
     * @return The date for the given file.
     */
    public Date extractDateForFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        String sql = "SELECT " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND " 
                + CS_COLLECTION_ID + " = ?";
        Long dateInMillis = DatabaseUtils.selectFirstLongValue(connector, sql, fileID, collectionID);
        if(dateInMillis == null) {
            return null;
        }
        return new Date(dateInMillis);
    }
    
    /**
     * Extracts the checksum for a given file.
     * @param fileID The id of the file to extract the checksum for.
     * @param collectionID The collection id for the file.
     * @return The checksum for the given file.
     */
    public String extractChecksumForFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        String sql = "SELECT " + CS_CHECKSUM + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND " 
                + CS_COLLECTION_ID + " = ?";
        return DatabaseUtils.selectStringValue(connector, sql, fileID, collectionID);
    }
    
    /**
     * Extracts whether a given file exists.
     * @param fileID The id of the file to extract whose existence is in question.
     * @param collectionID The collection id for the file.
     * @return Whether the given file exists.
     */
    public boolean hasFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        String sql = "SELECT COUNT(*) FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ? AND " 
                + CS_COLLECTION_ID + " = ?";
        return DatabaseUtils.selectIntValue(connector, sql, fileID, collectionID) != 0;
    }
    
    /**
     * Extracts the checksum entry for a single file.
     * @param fileID The id of the file whose checksum entry should be extracted.
     * @param collectionID The collection id for the extraction.
     * @return The checksum entry for the file.
     */
    public ChecksumEntry extractSingleEntry(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        String sql = "SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE 
                + " WHERE " + CS_FILE_ID + " = ? AND " + CS_COLLECTION_ID + " = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql, fileID, collectionID)) {
            try (ResultSet res = ps.executeQuery()) {
                if(!res.next()) {
                    throw new IllegalStateException("No entry for the file '" + fileID + "'.");
                }
                return extractChecksumEntry(res);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the ChecksumEntry for '" + fileID + "'", e);
        }
    }

    /**
     * Extracts the checksum entry for a single file, with restrictions on time interval.
     * @param minTimeStamp The minimum timestamp for the checksum calculation date.
     * @param maxTimeStamp The maximum timestamp for the checksum calculation date.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection.
     * @return The entry for the file, or null if it is not within the restrictions.
     */
    public ChecksumEntry extractSingleEntryWithRestrictions(XMLGregorianCalendar minTimeStamp, 
            XMLGregorianCalendar maxTimeStamp, String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        List<Object> args = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , "  + CS_DATE + " FROM " + CHECKSUM_TABLE 
                + " WHERE " + CS_FILE_ID + " = ? AND " + CS_COLLECTION_ID + " = ?");

        args.add(fileID);
        args.add(collectionID);
        
        if(minTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " > ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp).getTime());
        }
        if(maxTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " <= ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp).getTime());
        }
        sql.append(" ORDER BY " + CS_DATE + " ASC ");

        try (Connection conn = connector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray())) {
            try (ResultSet res = ps.executeQuery()) {
                if(!res.next()) {
                    log.debug("No checksum entry found for file '" + fileID + "' at collection '" + collectionID
                            + "', with calculation date interval: [" + minTimeStamp + " , " + maxTimeStamp + "]");
                    return null;
                }
                return extractChecksumEntry(res);
            } 
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the ChecksumEntry for '" + fileID + "'", e);
        }
    }

    /**
     * Extracts the file ids within the given optional limitations.
     *
     * @param minTimeStamp       The minimum date for the timestamp of the extracted file ids.
     * @param maxTimeStamp       The maximum date for the timestamp of the extracted file ids.
     * @param fileID             The ID of the file to retrieve. Null if all file-ids.
     * @param maxNumberOfResults The maximum number of results.
     * @param collectionID       The collection id for the extraction.
     * @return The requested collection of file ids.
     */
    public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String fileID, String collectionID) {
        List<Object> args = new ArrayList<Object>(); 
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + CS_FILE_ID + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " 
                + CS_COLLECTION_ID + " = ?");
        args.add(collectionID);
        
        if(minTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " >= ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp).getTime());
        }
        if(maxTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " <= ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp).getTime());
        }
        if(fileID != null) {
            sql.append(" AND " + CS_FILE_ID + " = ? ");
            args.add(fileID);
        }
        sql.append(" ORDER BY " + CS_DATE + " ASC ");
        
        ExtractedFileIDsResultSet results = new ExtractedFileIDsResultSet();
        try (Connection conn = connector.getConnection();
            PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray())){
            conn.setAutoCommit(false);
            ps.setFetchSize(DEFAULT_FETCH_SIZE);
            try (ResultSet res = ps.executeQuery()){
                int i = 0;
                while(res.next() && (maxNumberOfResults == null || i < maxNumberOfResults)) {
                    results.insertFileID(res.getString(DatabaseConstants.CS_FILE_ID), 
                            new Date(res.getLong(DatabaseConstants.CS_DATE)));
                    i++;
                }
                
                if(maxNumberOfResults != null && i >= maxNumberOfResults) {
                    results.reportMoreEntriesFound();
                }
            } finally {
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the file ids with the arguments, minTimestamp = '" 
                    + minTimeStamp + "', maxTimestamp = '"+ maxTimeStamp + "', maxNumberOfResults = '" 
                    + maxNumberOfResults + "'", e);
        }
        
        return results;
    }
    
    /**
     * Retrieves all the file ids for a given collection id within the database.
     * @param collectionID The collection id for the extraction.
     * @return The list of file ids extracted from the database.
     */
    public List<String> extractAllFileIDs(String collectionID) {
        String sql = "SELECT " + CS_FILE_ID + " FROM " + CHECKSUM_TABLE 
                + " WHERE " + CS_COLLECTION_ID + " = ?";
        return DatabaseUtils.selectStringList(connector, sql, collectionID);
    }
    
    /**
     * Extracts the checksum entries within the given optional limitations.
     * 
     * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entries.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entries.
     * @param maxNumberOfResults The maximum number of results.
     * @param collectionID The collection id for the extraction.
     * @return The requested collection of file ids.
     */
    public ExtractedChecksumResultSet extractEntries(XMLGregorianCalendar minTimeStamp, 
            XMLGregorianCalendar maxTimeStamp, Long maxNumberOfResults, String collectionID) {
        List<Object> args = new ArrayList<Object>(); 
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE + " FROM " + CHECKSUM_TABLE 
                + " WHERE " + CS_COLLECTION_ID + " = ?");
        args.add(collectionID);
        
        if(minTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " >= ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(minTimeStamp).getTime());
        }
        if(maxTimeStamp != null) {
            sql.append(" AND " + CS_DATE + " <= ? ");
            args.add(CalendarUtils.convertFromXMLGregorianCalendar(maxTimeStamp).getTime());
        }
        sql.append(" ORDER BY " + CS_DATE + " ASC ");
        
        ExtractedChecksumResultSet results = new ExtractedChecksumResultSet();
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray())){
            conn.setAutoCommit(false);
            ps.setFetchSize(DEFAULT_FETCH_SIZE);
            try (ResultSet res = ps.executeQuery()) {
                int i = 0;
                while(res.next() && (maxNumberOfResults == null || i < maxNumberOfResults)) {
                    results.insertChecksumEntry(extractChecksumEntry(res));
                    i++;
                }
                
                if(maxNumberOfResults != null && i >= maxNumberOfResults) {
                    results.reportMoreEntriesFound();
                }
            } finally {
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the checksum entries with the arguments, minTimestamp = '" 
                    + minTimeStamp + "', maxTimestamp = '"+ maxTimeStamp + "', maxNumberOfResults = '" 
                    + maxNumberOfResults + "'", e);
        }
        
        return results;
    }
    
    /**
     * Extracts the file ids of the entries where the checksum calculation date is lower than a given date.
     * 
     * @param maxTimeStamp The maximum date for the checksum calculation date.
     * @param collectionID The collection id for the extraction.
     * @return The requested collection of file ids.
     */
    public List<String> extractFileIDsWithMaxChecksumDate(Long maxTimeStamp, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNull(maxTimeStamp, "Long maxTimeStamp");
        List<Object> args = new ArrayList<Object>(); 
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + CS_FILE_ID + " FROM " + CHECKSUM_TABLE + " WHERE " + CS_COLLECTION_ID + " = ? AND " 
                + CS_DATE + " <= ? " + " ORDER BY " + CS_DATE + " ASC ");
        args.add(collectionID);
        args.add(maxTimeStamp);
        
        List<String> results = new ArrayList<String>();
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql.toString(), args.toArray())) {
            conn.setAutoCommit(false);
            ps.setFetchSize(DEFAULT_FETCH_SIZE);
            try (ResultSet res = ps.executeQuery()) {
                while(res.next() ) {
                    if(!res.wasNull()) {
                        results.add(res.getString(DatabaseConstants.CS_FILE_ID));
                    }
                }
            } finally {
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the checksum entries with the arguments, maxTimestamp = '"
                    + maxTimeStamp + "'", e);
        }
        
        return results;
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
        String fileID = resSet.getString(DatabaseConstants.CS_FILE_ID);
        String checksum = resSet.getString(DatabaseConstants.CS_CHECKSUM);
        Date date = new Date(resSet.getLong(DatabaseConstants.CS_DATE));
        return new ChecksumEntry(fileID, checksum, date);
    }
}
