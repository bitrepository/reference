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
     * @param settings The settings.
     */
    public ChecksumDAO(DatabaseManager databaseManager) {
        synchronized(this) {
            connector = databaseManager.getConnector();
            this.ingestor = new ChecksumIngestor(connector);
            this.extractor = new ChecksumExtractor(connector);
        }
    }
    
    @Override
    public void insertChecksumCalculation(String fileId, String collectionId, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        
        if(extractor.hasFile(fileId, collectionId)) {
            ingestor.updateEntry(fileId, collectionId, checksum, calculationDate);
        } else {
            ingestor.insertNewEntry(fileId, collectionId, checksum, calculationDate);
        }
    }

    @Override
    public void deleteEntry(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        if(!extractor.hasFile(fileId, collectionId)) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        ingestor.removeEntry(fileId, collectionId);
    }

    @Override
    public ChecksumEntry getEntry(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        
        return extractor.extractSingleEntry(fileId, collectionId);
    }
    
    @Override
    public ExtractedChecksumResultSet getChecksumResults(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractor.extractEntries(minTimeStamp, maxTimeStamp, maxNumberOfResults, collectionId);
    }
    
    @Override
    public Date getCalculationDate(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        Date res = extractor.extractDateForFile(fileId, collectionId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
        
    }
    
    @Override
    public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractor.getFileIDs(minTimeStamp, maxTimeStamp, maxNumberOfResults, collectionId);
    }

    @Override
    public boolean hasFile(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        return extractor.hasFile(fileId, collectionId);
    }

    @Override
    public String getChecksum(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        String res = extractor.extractChecksumForFile(fileId, collectionId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
    }

    @Override
    public java.util.Collection<String> getAllFileIDs(String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractor.extractAllFileIDs(collectionId);
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
}
