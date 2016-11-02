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

/**
 * Interface for the storage of checksums.
 */
public interface ChecksumStore {
    /**
     * Retrieve the calculation date for the checksum of the file.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection for the file.
     * @return The calculation date for the checksum of the file at the collection.
     */
    Date getCalculationDate(String fileID, String collectionID);
    
    /**
     * Retrieve the checksum for the given file.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection for the file.
     * @return The checksum of the file.
     */
    String getChecksum(String fileID, String collectionID);
    
    /**
     * @param fileID The id of the file.
     * @param collectionID The id of the collection for the file.
     * @return The entry with the checksum information about the file.
     */
    ChecksumEntry getEntry(String fileID, String collectionID);
    
    /**
     * Retrieves the checksum result entries for the given restrictions from the store.
     * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entries.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entries.
     * @param maxNumberOfResults The maximum number of results.
     * @param collectionID The id of the collection for the files.
     * @return The checksum entries from the store.
     */
    ExtractedChecksumResultSet getChecksumResults(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String collectionID);

    /**
     * Retrieves the checksum result entry for the given file from the store.
     * @param minTimeStamp The minimum date for the timestamp of the extracted checksum entry.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted checksum entry.
     * @param fileID The id of the file to retrieve the checksum result for.
     * @param collectionID The id of the collection for the file.
     * @return The checksum entries from the store.
     */
    ExtractedChecksumResultSet getChecksumResult(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp,
            String fileID, String collectionID);

    /**
     * Inserts a checksum calculation for a given file.
     * @param fileID The id of the file.
     * @param collectionID The id of the collection for the file.
     * @param checksum The checksum of the file.
     * @param calculationDate The date for the calculation of the checksum for the file.
     */
    void insertChecksumCalculation(String fileID, String collectionID, String checksum, Date calculationDate);
    
    /**
     * Retrieval of file ids.
     * @param minTimeStamp The minimum date for the timestamp of the extracted file ids.
     * @param maxTimeStamp The maximum date for the timestamp of the extracted file ids.
     * @param maxNumberOfResults The maximum number of results.
     * @param fileID The id of the file. If null or empty string, then all files.
     * @param collectionID The id of the collection for the file.
     * @return The file ids in the store within the restrictions.
     */
    ExtractedFileIDsResultSet getFileIDs(XMLGregorianCalendar minTimeStamp, XMLGregorianCalendar maxTimeStamp, 
            Long maxNumberOfResults, String fileID, String collectionID);
    
    /**
     * Retrieval of all the file ids in the store.
     * @param collectionID The id of the collection for the files.
     * @return The collection of file ids in the store.
     */
    Collection<String> getAllFileIDs(String collectionID);
    
    /**
     * Retrieval of the ids for the files where the checksum was calculated earlier than a certain date.
     * @param checksumDate The max calculation date for the checksums to retrieve.
     * @param collectionID The id of the collection for the files.
     * @return The collection of ids for the files with old checksums.
     */
    Collection<String> getFileIDsWithOldChecksums(Date checksumDate, String collectionID);
    
    /**
     * Deletes a given entry from the cache.
     * @param fileID The id of the file, whose entry should be removed from the cache.
     * @param collectionID The id of the collection for the file.
     */
    void deleteEntry(String fileID, String collectionID);
    
    /**
     * Tells whether a given file id can be found in the cache.
     * @param fileID The id of the file to find in the cache.
     * @param collectionID The id of the collection for the file.
     * @return Whether the file could be found.
     */
    boolean hasFile(String fileID, String collectionID);
    
    /**
     * Closes and cleans up the ChecksumStore.
     */
    void close();
}
