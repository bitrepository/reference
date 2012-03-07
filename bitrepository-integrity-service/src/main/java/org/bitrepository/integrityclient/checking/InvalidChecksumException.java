/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityclient.checking;

import java.util.Map;

/**
 * Exception to wrap the content of a failed checksum validation.
 */
@SuppressWarnings("serial")
public class InvalidChecksumException extends Exception {
    /** The id of the file involved.*/
    private final String fileId;
    /** The map of the different checksums and their count.*/
    private final Map<String, Integer> checksumsCount;
    
    /**
     * Constructor.
     * @param fileId The id of the file with a consistent checksum.
     * @param checksumsCount The different checksums and their count.
     */
    public InvalidChecksumException(String fileId, Map<String, Integer> checksumsCount) {
        this.fileId = fileId;
        this.checksumsCount = checksumsCount;
    }
    
    @Override
    public String toString() {
        return "File id: " + fileId + ", checksum counts: " + checksumsCount;
    }
    
    /**
     * @return The id of the file involved.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * @return The map of the different checksums and their count.
     */
    public Map<String, Integer> getChecksumsCount() {
        return checksumsCount;
    }
}
