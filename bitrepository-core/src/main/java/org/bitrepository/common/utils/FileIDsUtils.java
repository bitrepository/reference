package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Utility functions for the FileIDs object.
 */
public class FileIDsUtils {
    /** The constant for all file ids.*/
    private static final String ALL_FILE_IDS = "true";
    
    /**
     * @return The FileIDs for all file ids.
     */
    public static FileIDs getAllFileIDs() {
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs(ALL_FILE_IDS);
        return fileids;

    }
    
    /**
     * @return The FileIDs for a specific file id.
     */
    public static FileIDs getSpecificFileIDs(String fileId) {
        FileIDs fileids = new FileIDs();
        fileids.setFileID(fileId);
        return fileids;

    }
}
