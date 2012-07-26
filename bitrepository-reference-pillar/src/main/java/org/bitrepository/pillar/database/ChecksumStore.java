package org.bitrepository.pillar.database;

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
}
