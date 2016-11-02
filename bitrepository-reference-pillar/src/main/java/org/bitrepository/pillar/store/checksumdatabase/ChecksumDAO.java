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

import java.util.Collection;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;

/**
 * The checksum store backed by a database.
 * Mediates the operations of the database to either the extractor or the ingestor.
 */
public class ChecksumDAO implements ChecksumStore {
    /** The ingestors for the database.*/
    private final ChecksumIngestor ingestor;
    /** The extractor for the database.*/
    private final ChecksumExtractor extractor;
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param databaseManager FIXME
     */
    public ChecksumDAO(DatabaseManager databaseManager) {
        synchronized(this) {
            connector = databaseManager.getConnector();
            this.ingestor = new ChecksumIngestor(connector);
            this.extractor = new ChecksumExtractor(connector);
        }
    }
    
    @Override
    public void insertChecksumCalculation(String fileID, String collectionID, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");
        
        if(extractor.hasFile(fileID, collectionID)) {
            ingestor.updateEntry(fileID, collectionID, checksum, calculationDate);
        } else {
            ingestor.insertNewEntry(fileID, collectionID, checksum, calculationDate);
        }
    }

    @Override
    public void deleteEntry(String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");

        if(!extractor.hasFile(fileID, collectionID)) {
            throw new IllegalStateException("No entry for file '" + fileID + "' to delete.");
        }
        ingestor.removeEntry(fileID, collectionID);
    }

    @Override
    public ChecksumEntry getEntry(String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");
        
        return extractor.extractSingleEntry(fileID, collectionID);
    }
    
    @Override
    public ExtractedChecksumResultSet getChecksumResults(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionID) {
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");
        return extractor.extractEntries(minTimeStamp, maxTimeStamp, maxNumberOfResults, collectionID);
    }
    
    @Override
    public Date getCalculationDate(String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");

        Date res = extractor.extractDateForFile(fileID, collectionID);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileID + "' to delete.");
        }
        return res;
        
    }
    
    @Override
    public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");
        return extractor.getFileIDs(minTimeStamp, maxTimeStamp, maxNumberOfResults, fileID, collectionID);
    }

    @Override
    public boolean hasFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");

        return extractor.hasFile(fileID, collectionID);
    }

    @Override
    public String getChecksum(String fileID, String collectionID) {
        ArgumentValidator.checkNotNull(fileID, "String fileID");
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");

        String res = extractor.extractChecksumForFile(fileID, collectionID);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileID + "' to delete.");
        }
        return res;
    }

    @Override
    public java.util.Collection<String> getAllFileIDs(String collectionID) {
        ArgumentValidator.checkNotNull(collectionID, "String collectionID");
        return extractor.extractAllFileIDs(collectionID);
    }

    @Override
    public void close() {
        connector.destroy();
    }

    @Override
    public ExtractedChecksumResultSet getChecksumResult(XMLGregorianCalendar minTimeStamp, 
            XMLGregorianCalendar maxTimeStamp, String fileID, String collectionID) {
        ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();        
        ChecksumEntry entry = extractor.extractSingleEntryWithRestrictions(minTimeStamp, maxTimeStamp, fileID, 
                collectionID);
        if(entry != null) {
            res.insertChecksumEntry(entry);
        }
        return res;
    }

    @Override
    public Collection<String> getFileIDsWithOldChecksums(Date checksumDate, String collectionID) {
        ArgumentValidator.checkNotNull(checksumDate, "Date checksumDate");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        return extractor.extractFileIDsWithMaxChecksumDate(checksumDate.getTime(), collectionID);
    }
}
