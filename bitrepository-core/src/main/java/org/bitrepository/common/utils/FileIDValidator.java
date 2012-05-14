package org.bitrepository.common.utils;

import org.bitrepository.common.settings.Settings;

public class FileIDValidator {

    public static void validateFileID(Settings settings, String fileID) {
        if(!fileID.matches(settings.getCollectionSettings().getProtocolSettings().getAllowedFileIDPattern())) {
            throw new IllegalArgumentException("The fileID is not allowed. FileID must match: " + 
                    settings.getCollectionSettings().getProtocolSettings().getAllowedFileIDPattern());
        }
    }
}
