package org.bitrepository.integrityclient.cache;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.ArgumentValidator;

/**
 * Class for containing the information about a given file at a given pillar.
 */
public class FileIDInfo {
    /** The id of the file.*/
    private final String fileID;
    /** The date for the last check for the file ID.*/
    private XMLGregorianCalendar fileLastCheck;
    /** The checksum of the file.*/
    private String checksum;
    /** The type of checksum, e.g. algorithm and optional salt*/
    private ChecksumSpecTYPE checksumType;
    /** The date for the last check of the checksum.*/
    private XMLGregorianCalendar checksumLastCheck;
    /** The id of the pillar.*/
    private final String pillarId;
    
    /**
     * Constructor for all data.
     * @param fileID The id of the file.
     * @param fileLastCheck The date for the last check of the file id.
     * @param checksum The checksum of the file.
     * @param checksumLastCheck The date for the last check of the checksum.
     * @param pillarId The id of the pillar.
     */
    public FileIDInfo(String fileID, XMLGregorianCalendar fileLastCheck, String checksum, 
            ChecksumSpecTYPE checksumType, XMLGregorianCalendar checksumLastCheck, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        this.fileID = fileID;
        this.fileLastCheck = fileLastCheck;
        this.checksum = checksum;
        this.checksumType = checksumType;
        this.checksumLastCheck = checksumLastCheck;
        this.pillarId = pillarId;
    }
    
    /**
     * Constructor for only file id and pillar id.
     * @param fileID The id of the file.
     * @param pillarId The id of the pillar.
     */
    public FileIDInfo(String fileID, String pillarId) {
        this(fileID, null, null, null, null, pillarId);
    }
    
    /**
     * @return The id of the file.
     */
    public String getFileID() {
        return fileID;
    }
    
    /**
     * @return The date for the last check of the file. 
     */
    public XMLGregorianCalendar getDateForLastFileIDCheck() {
        return fileLastCheck;
    }
    
    /**
     * @param dateForLastFielIDCheck The new date for the last check of the file.
     */
    public void setDateForLastFileIDCheck(XMLGregorianCalendar dateForLastFielIDCheck) {
        this.fileLastCheck = dateForLastFielIDCheck;
    }
    
    /**
     * @return Checksum of the file. This can be null, if the checksum has not been retrieved yet.
     */
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * @param checksum The new checksum.
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /**
     * @return The type of checksum, e.g. algorithm and optional salt.
     */
    public ChecksumSpecTYPE getChecksumType() {
        return checksumType;
    }
    
    /**
     * @param checksumType The new type for the checksum, e.g. algorithm and optional salt.
     */
    public void setChecksumType(ChecksumSpecTYPE checksumType) {
        this.checksumType = checksumType;
    }
    
    /**
     * @return The date for the last check of the checksum.
     */
    public XMLGregorianCalendar getDateForLastChecksumCheck() {
        return checksumLastCheck;
    }
    
    /**
     * @param dateForLastChecksumCheck The new date for the last checksum of the checksum.
     */
    public void setDateForLastChecksumCheck(XMLGregorianCalendar dateForLastChecksumCheck) {
        this.checksumLastCheck = dateForLastChecksumCheck;
    }
    
    /**
     * @return The id of the pillar.
     */
    public String getPillarId() {
        return pillarId;
    }
    
    @Override
    public String toString() {
        return "Pillar id: " + pillarId + ", File id: " + fileID + " (date: " + fileLastCheck + "), Checksum: " 
                + checksum + " (date: " + checksumLastCheck + ")";
    }
}
