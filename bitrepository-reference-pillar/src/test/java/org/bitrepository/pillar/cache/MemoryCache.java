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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;

/**
 * Very simple memory based implementation of the ChecksumCache.
 * Everything is kept within a map between the file ids and their checksum.
 */
public class MemoryCache implements ChecksumStore {
    
    /**
     * The checksum mapping between the file ids and their checksum.
     */
    private Map<String, ChecksumEntry> checksumMap = new HashMap<String, ChecksumEntry>();
    
    public MemoryCache() {}
    
    @Override
    public String getChecksum(String fileId) {
        if(checksumMap.containsKey(fileId)) {
            return checksumMap.get(fileId).getChecksum();            
        }
        return null;
    }
    
    @Override
    public Collection<String> getFileIDs() {
        return checksumMap.keySet();
    }
    
    @Override
    public void deleteEntry(String fileId) {
        checksumMap.remove(fileId);
    }
    
    @Override
    public boolean hasFile(String fileId) {
        return checksumMap.containsKey(fileId);
    }
    
    /**
     * Removes every entry in the cache.
     * Used for cleaning up between tests.
     */
    public void cleanUp() {
        checksumMap.clear();
    }

    @Override
    public Date getCalculationDate(String fileId) {
        return checksumMap.get(fileId).getCalculationDate();
    }

    @Override
    public ChecksumEntry getEntry(String fileId) {
        return checksumMap.get(fileId);
    }

    @Override
    public Collection<ChecksumEntry> getAllEntries() {
        return checksumMap.values();
    }

    @Override
    public void insertChecksumCalculation(String fileId, String checksum, Date calculationDate) {
        checksumMap.put(fileId, new ChecksumEntry(fileId, checksum, calculationDate));
    }

    @Override
    public void close() {
        cleanUp();
    }
}