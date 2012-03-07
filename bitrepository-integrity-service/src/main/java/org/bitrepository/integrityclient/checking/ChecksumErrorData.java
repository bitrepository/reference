package org.bitrepository.integrityclient.checking;

import java.util.Collection;

/**
 * Container for a checksum error on a given file.
 * Contains the id of the file, and the id of the pillars, which has an erroneous checksum. 
 */
public class ChecksumErrorData {
    /** @See getPillarIds */
    private final Collection<String> pillarIds;
    /** @See getFileId() */
    private final String fileId;
    
    /**
     * Constructor.
     * @param fileId The id of the file with checksum issues.
     * @param pillarIds The id of the pillars where the file has checksum errors.
     */
    public ChecksumErrorData(String fileId, Collection<String> pillarIds) {
        this.pillarIds = pillarIds;
        this.fileId = fileId;
    }
    
    /**
     * @return The id of the file with checksum error.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The id of pillars where the file has a checksum error.
     */
    public Collection<String> getPillarIds() {
        return pillarIds;
    }
    
   @Override
    public String toString() {
       return "Checksum error for file '" + fileId + "' at the pillars '" + pillarIds + "'";
    }
}
