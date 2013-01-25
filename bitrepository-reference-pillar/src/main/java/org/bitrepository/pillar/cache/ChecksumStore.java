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

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.cache.database.ExtractedFileIDsResultSet;

/**
 * Interface for the storage of checksums.
 */
public interface ChecksumStore {
    /**
     * Retrieve the calculation date for the checksum of the file.
     * @param fileId The id of the file.
     * @return The calculation date for the checksum of the file.
     */
    Date getCalculationDate(String fileId);
    
    /**
     * Retrieve the checksum for the given file.
     * @param fileId The id of the file.
     * @return The checksum of the file.
     */
    String getChecksum(String fileId);
    
    /**
     * @param fileId The id of the file.
     * @return The entry with the checksum information about the file.
     */
    ChecksumEntry getEntry(String fileId);
    
    /**
     * Retrieves the entries from the store.
     * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entries.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entries.
     * @param maxNumberOfResults The maximum number of results.
     * @return The checksum entries from the store.
     */
    ExtractedChecksumResultSet getEntries(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults);
    
    /**
     * Inserts a checksum calculation for a given file.
     * @param fileId The id of the file.
     * @param checksum The checksum of the file.
     * @param calculationDate The date for the calculation of the checksum for the file.
     */
    void insertChecksumCalculation(String fileId, String checksum, Date calculationDate);
    
    /**
     * Retrieval of file ids.
     * @param minTimeStamp The minimum date for the timestamp of the extracted file ids.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted file ids.
     * @param maxNumberOfResults The maximum number of results.
     * @return The file ids in the store within the restrictions.
     */
    ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults);
    
    /**
     * Retrieval of all the file ids in the store.
     * @return The collection of file ids in the store.
     */
    Collection<String> getAllFileIDs();
    
    /**
     * Deletes a given entry from the cache.
     * @param fileId The id of the file, whose entry should be removed from the cache.
     */
    void deleteEntry(String fileId);
    
    /**
     * Tells whether a given file id can be found in the cache.
     * @param fileId The id of the file to find in the cache.
     * @return Whether the file could be found.
     */
    boolean hasFile(String fileId);
    
    /**
     * Closes and cleans up the ChecksumStore.
     */
    void close();
}
