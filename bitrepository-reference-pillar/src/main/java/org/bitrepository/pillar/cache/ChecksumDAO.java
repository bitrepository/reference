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
package org.bitrepository.pillar.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.cache.database.ChecksumExtractor;
import org.bitrepository.pillar.cache.database.ChecksumIngestor;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.cache.database.ExtractedFileIDsResultSet;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseMigrator;

/**
 * The checksum store backed by a database.
 * This contains one ingestor and one extractor for each collec
 */
public class ChecksumDAO implements ChecksumStore {
    /** The map between ingestors for the database and their respective collection ids.*/
    private final Map<String, ChecksumIngestor> ingestors = new HashMap<String, ChecksumIngestor>();
    /** The map between extractor for the database and their respective collection ids.*/
    private final Map<String, ChecksumExtractor> extractors = new HashMap<String, ChecksumExtractor>();
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param settings The settings.
     */
    public ChecksumDAO(Settings settings) {
        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());
        
        synchronized(this) {
            for(String id : settings.getMyCollectionIDs()) {
                this.ingestors.put(id, new ChecksumIngestor(connector, id));
                this.extractors.put(id, new ChecksumExtractor(connector, id));
            }
        
            DatabaseMigrator migrator = new ChecksumDBMigrator(connector, settings);
            migrator.migrate();
        }
    }
    
    @Override
    public void insertChecksumCalculation(String fileId, String collectionId, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        
        if(extractors.get(collectionId).hasFile(fileId)) {
            ingestors.get(collectionId).updateEntry(fileId, checksum, calculationDate);
        } else {
            ingestors.get(collectionId).insertNewEntry(fileId, checksum, calculationDate);
        }
    }

    @Override
    public void deleteEntry(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        if(!extractors.get(collectionId).hasFile(fileId)) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        ingestors.get(collectionId).removeEntry(fileId);
    }

    @Override
    public ChecksumEntry getEntry(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        
        return extractors.get(collectionId).extractSingleEntry(fileId);
    }
    
    @Override
    public ExtractedChecksumResultSet getEntries(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractors.get(collectionId).extractEntries(minTimeStamp, maxTimeStamp, maxNumberOfResults);
    }
    
    @Override
    public Date getCalculationDate(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        Date res = extractors.get(collectionId).extractDateForFile(fileId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
        
    }
    
    @Override
    public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractors.get(collectionId).getFileIDs(minTimeStamp, maxTimeStamp, maxNumberOfResults);
    }

    @Override
    public boolean hasFile(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        return extractors.get(collectionId).hasFile(fileId);
    }

    @Override
    public String getChecksum(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");

        String res = extractors.get(collectionId).extractChecksumForFile(fileId);
        if(res == null) {
            throw new IllegalStateException("No entry for file '" + fileId + "' to delete.");
        }
        return res;
    }

    @Override
    public java.util.Collection<String> getAllFileIDs(String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        return extractors.get(collectionId).extractAllFileIDs();
    }

    @Override
    public void close() {
        connector.destroy();
    }
}
