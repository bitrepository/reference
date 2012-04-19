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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Interface for the cache for storing the data of the checksum pillar.
 */
public interface ChecksumStore {
    /**
     * Retrieves the checksum of a given file id.
     * @param fileId The id of the given file.
     * @return The checksum for the given file id.
     */
    String getChecksum(String fileId);
    
    /**
     * Retrieval of which file ids fitting the request.
     * @param fileIds The file ids to retrieve. 
     * @return The matching file ids in the system.
     */
    Collection<String> getFileIDs(FileIDs fileIds);
    
    /**
     * Retrieves the last modified date for some file ids.
     * @param fileIds The FileIDs to retrieve their last modified date.
     * @return A mapping between the requested file ids and their last modified date.
     */
    Map<String, Date> getLastModifiedDate(FileIDs fileIds);
    
    /**
     * Inserts a new entry into the cache.
     * @param fileId The id of the entry to put into the cache.
     * @param checksum The checksum of the given file id-
     */
    void putEntry(String fileId, String checksum);
    
    /**
     * Deletes a given entry from the cache.
     * @param fileId The id of the file, whose entry should be removed from the cache.
     */
    void deleteEntry(String fileId);
    
    /**
     * Replaces one checksum value of an entry with another.
     * @param fileId The id of the file whose checksum value should be replaced.
     * @param oldChecksum The checksum of the old entry.
     * @param newChecksum The checksum of the new entry.
     */
    void replaceEntry(String fileId, String oldChecksum, String newChecksum);
    
    /**
     * Tells whether a given file id can be found in the cache.
     * @param fileId The id of the file to find in the cache.
     * @return Whether the file could be found.
     */
    boolean hasFile(String fileId);
}
