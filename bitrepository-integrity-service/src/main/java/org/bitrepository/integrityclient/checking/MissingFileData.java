package org.bitrepository.integrityclient.checking;

import java.util.Collection;

/**
 * Container for the information about a missing file.
 * Contains the id of the file, and the id of the pillars, where the file is missing. 
 */
public class MissingFileData {
    /** @See getPillarIds */
    private final Collection<String> pillarIds;
    /** @See getFileId() */
    private final String fileId;
    
    /**
     * Constructor.
     * @param fileId The id of the file which is missing.
     * @param pillarIds The id of the pillars where the file is missing.
     */
    public MissingFileData(String fileId, Collection<String> pillarIds) {
        this.pillarIds = pillarIds;
        this.fileId = fileId;
    }
    
    /**
     * @return The id of the file which is missing.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The id of pillars where the file is missing.
     */
    public Collection<String> getPillarIds() {
        return pillarIds;
    }

    @Override
    public String toString() {
       return "The file '" + fileId + "' is missing at the pillars '" + pillarIds + "'";
    }
}
