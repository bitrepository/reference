package org.bitrepository.pillar.cache;

import java.util.Date;

import org.bitrepository.common.ArgumentValidator;

/**
 * Container for the information about the checksum of a file.
 */
public class ChecksumEntry {
    /** The id of the file.*/
    private final String fileId;
    /** The checksum of the file.*/
    private String checksum;
    /** The calculation date for the checksum of the file.*/
    private Date calculationDate;
    
    /**
     * Constructor.
     * @param fileId The id of the file.
     * @param checksum The checksum of the file.
     * @param calculationDate The calculation date for the checksum of the file.
     */
    public ChecksumEntry(String fileId, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        
        this.fileId = fileId;
        this.checksum = checksum;
        this.calculationDate = calculationDate;
    }
    
    /**
     * Set a new value for the checksum.
     * @param checksum The new checksum value.
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /**
     * Set a new timestamp for the calculation date of the checksum.
     * @param date The new date for the calculation of the checksum.
     */
    public void setCalculationDate(Date date) {
        this.calculationDate = date;
    }
    
    /**
     * @return The id of the file.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The checksum of the file.
     */
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * @return The calculation date for the checksum of the file.
     */
    public Date getCalculationDate() {
        return calculationDate;
    }
}
