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

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.database.DatabaseStoragedCache;

/**
 * A storage of configuration information that is backed by a database.
 */
public class DatabaseBasedIntegrityCached implements IntegrityCache {
    /** The settings for the database cache. */
    private final Settings settings;
    
    private final DatabaseStoragedCache store;
    
    /**
     * Initialize storage.
     *
     * @param storageConfiguration Contains configuration for storage. Currently URL, user and pass for database.
     */
    public DatabaseBasedIntegrityCached(Settings settings, Connection dbConnection) {
        this.settings = settings;
        this.settings.getReferenceSettings().getIntegrityServiceSettings();
        
        // TODO load connection from settings.
        this.store = new DatabaseStoragedCache(dbConnection);
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        store.updateFileIDs(data, pillarId);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, 
            String pillarId) {
        store.updateChecksumData(data, checksumType, pillarId);
    }

    @Override
    public Collection<FileInfo> getFileInfos(String fileId) {
        return store.getFileInfosForFile(fileId);
    }

    @Override
    public Collection<String> getAllFileIDs() {
        return store.getAllFileIDs();
    }

    @Override
    public long getNumberOfFiles(String pillarId) {
        return store.getNumberOfExistingFilesForAPillar(pillarId);
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        return store.getNumberOfMissingFilesForAPillar(pillarId);
    }

    @Override
    public Date getLatestFileUpdate(String pillarId) {
        return store.getLastFileListUpdate(pillarId);
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        return store.getNumberOfChecksumErrorsForAPillar(pillarId);
    }

    @Override
    public Date getLatestChecksumUpdate(String pillarId) {
        return store.getLastChecksumUpdate(pillarId);
    }
}
