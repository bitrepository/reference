package org.bitrepository.pillar.cache;

import java.util.Collection;
import java.util.Date;

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
     * Retrieves all the entries from the store.
     * @return All the checksum entries from the store.
     */
    Collection<ChecksumEntry> getAllEntries();
    
    /**
     * Inserts a checksum calculation for a given file.
     * @param fileId The id of the file.
     * @param checksum The checksum of the file.
     * @param calculationDate The date for the calculation of the checksum for the file.
     */
    void insertChecksumCalculation(String fileId, String checksum, Date calculationDate);
    
    /**
     * Retrieval of all file ids.
     * @return All the file ids in the store.
     */
    Collection<String> getFileIDs();
    
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
}
