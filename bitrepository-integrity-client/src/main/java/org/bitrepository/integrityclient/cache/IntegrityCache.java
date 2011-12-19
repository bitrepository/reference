/*
 * #%L
 * Bitrepository Integrity Client
 * *
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
package org.bitrepository.integrityclient.cache;

import java.util.Collection;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;

/**
 * Store of cached integrity information.
 */
public interface IntegrityCache {
    /**
     * Add file ID data to cache.
     * @param data The received data.
     * @param pillarId The id of the pillar the received data comes from.
     */
    void addFileIDs(FileIDsData data, String pillarId);

    /**
     * Add checksum data to cache.
     * @param data The received data.
     * @param pillarId The id of the pillar the received data comes from.
     */
    void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, String pillarId);

    /**
     * Retrieves the information of a given file id for all pillars.
     * @param fileId The id of the file. 
     * @return The collection of information about this file.
     */
    Collection<FileInfo> getFileInfos(String fileId);
    
    /**
     * Retrieves all the file ids in the collection.
     * @return The collection of file ids.
     */
    Collection<String> getAllFileIDs();
}
