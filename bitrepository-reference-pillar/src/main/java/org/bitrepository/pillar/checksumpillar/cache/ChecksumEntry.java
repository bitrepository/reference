package org.bitrepository.pillar.checksumpillar.cache;

/**
 * Container for the entries in the checksum archive.
 */
public class ChecksumEntry {
    /** @see getFileId() */
    private final String fileId;
    /** @see getChecksum() */
    private final String checksum;
    
    /**
     * Constructor.
     * @param fileid The id of the file for this entry.
     * @param checksum The checksum of the file.
     */
    public ChecksumEntry(String fileid, String checksum) {
        this.fileId = fileid;
        this.checksum = checksum;
    }
    
    /**
     * @return The id of the file for this entry.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The checksum for this entry.
     */
    public String getChecksum() {
        return checksum;
    }
    
    @Override
    public String toString() {
        return fileId + "##" + checksum;
    }
}
