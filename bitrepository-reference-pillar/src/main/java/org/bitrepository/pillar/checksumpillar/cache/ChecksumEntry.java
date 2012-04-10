/*
 * #%L
 * Bitrepository Reference Pillar
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
