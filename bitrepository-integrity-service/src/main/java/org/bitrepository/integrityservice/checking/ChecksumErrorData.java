/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.checking;

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
