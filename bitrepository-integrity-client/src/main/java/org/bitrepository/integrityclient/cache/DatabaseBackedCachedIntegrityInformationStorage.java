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
import org.bitrepository.integrityclient.configuration.integrityclientconfiguration.StorageConfiguration;

/**
 * A storage of configuration information that is backed by a database.
 */
public class DatabaseBackedCachedIntegrityInformationStorage implements CachedIntegrityInformationStorage {
    /**
     * Initialise storage.
     *
     * @param storageConfiguration Contains configuration for storage. Currently URL, user and pass for database.
     */
    public DatabaseBackedCachedIntegrityInformationStorage(StorageConfiguration storageConfiguration) {
    }

    @Override
    public void addFileIDs(FileIDsData data, String pillardId) {
        // TODO implement
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, 
            String pillarId) {
        // TODO implement
    }

    @Override
    public Collection<FileIDInfo> getFileInfo(String fileId) {
        // TODO implement
        return null;
    }

    @Override
    public Collection<String> getAllFileIDs() {
        // TODO Auto-generated method stub
        return null;
    }
}
