/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Utility functions for the FileIDs object.
 */
public class FileIDsUtils {
    /** The constant for all file ids.*/
    private static final String ALL_FILE_IDS = "true";
    
    /** Private constructor to prevent instantiation.*/
    private FileIDsUtils() {}
    
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
