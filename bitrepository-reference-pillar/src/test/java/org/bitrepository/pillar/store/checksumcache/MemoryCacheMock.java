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
package org.bitrepository.pillar.store.checksumcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;

/**
 * Very simple memory based implementation of the ChecksumCache.
 * Everything is kept within a map between the file ids and their checksum.
 * TODO: This does not support several collections, even though it should. Thus all collection id arguments are ignored.
 */
public class MemoryCacheMock implements ChecksumStore {
    
    /**
     * The checksum mapping between the file ids and their checksum.
     */
    private Map<String, ChecksumEntry> checksumMap = new HashMap<>();
    
    public MemoryCacheMock() {}
    
    @Override
    public String getChecksum(String fileID, String collectionID) {
        if(checksumMap.containsKey(fileID)) {
            return checksumMap.get(fileID).getChecksum();
        }
        return null;
    }
    
    @Override
    public ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String fileID, String collectionID) {
        ExtractedFileIDsResultSet res = new ExtractedFileIDsResultSet();
        for(String s : checksumMap.keySet()) {
            res.insertFileID(s, new Date(0));
        }
        return res;
    }
    
    @Override
    public void deleteEntry(String fileID, String collectionID) {
        checksumMap.remove(fileID);
    }
    
    @Override
    public boolean hasFile(String fileID, String collectionID) {
        return checksumMap.containsKey(fileID);
    }
    
    /**
     * Removes every entry in the cache.
     * Used for cleaning up between tests.
     */
    public void cleanUp() {
        checksumMap.clear();
    }

    @Override
    public Date getCalculationDate(String fileID, String collectionID) {
        return checksumMap.get(fileID).getCalculationDate();
    }

    @Override
    public ChecksumEntry getEntry(String fileID, String collectionID) {
        return checksumMap.get(fileID);
    }

    @Override
    public ExtractedChecksumResultSet getChecksumResults(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionID) {
        ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
        for(ChecksumEntry cs : checksumMap.values()) {
            res.insertChecksumEntry(cs);
        }

        return res;
    }

    @Override
    public void insertChecksumCalculation(String fileID, String collectionID, String checksum, Date calculationDate) {
        checksumMap.put(fileID, new ChecksumEntry(fileID, checksum, calculationDate));
    }

    @Override
    public void close() {
        cleanUp();
    }

    @Override
    public Collection<String> getAllFileIDs(String collectionID) {
        return checksumMap.keySet();
    }

    @Override
    public ExtractedChecksumResultSet getChecksumResult(XMLGregorianCalendar minTimeStamp,
            XMLGregorianCalendar maxTimeStamp, String fileID, String collectionID) {
        ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
        res.insertChecksumEntry(checksumMap.get(fileID));

        return res;
    }

    @Override
    public List<String> getFileIDsWithOldChecksums(Date checksumDate, String collectionID) {
        List<String> res = new ArrayList<>();
        for(ChecksumEntry ce : checksumMap.values()) {
            if(ce.getCalculationDate().getTime() < checksumDate.getTime()) {
                res.add(ce.getFileId());
            }
        }
        return res;
        
    }
}
