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
package org.bitrepository.integrityservice.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.service.database.DBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of configuration information that is backed by a database.
 */
public class IntegrityDatabase implements IntegrityModel {
    /** The database store. The interface to the functions of the database. */
    private final IntegrityDAO store;

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    /** The settings.*/
    private final Settings settings;
    
    /**
     * Initialize storage.
     *
     * @param settings Contains configuration for storage. Currently URL, user and pass for database.
     */
    public IntegrityDatabase(Settings settings) {
        this.settings = settings;
        //System.setProperty("derby.language.logQueryPlan", "true");
        this.store = new IntegrityDAO(new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                settings.getRepositorySettings().getCollections());
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId, String collectionId) {
        store.updateFileIDs(data, pillarId, collectionId);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
        store.updateChecksumData(data, pillarId, collectionId);
    }

    @Override
    public Collection<FileInfo> getFileInfos(String fileId, String collectionId) {
        return store.getFileInfosForFile(fileId, collectionId);
    }

    @Override
    public Collection<String> getAllFileIDs(String collectionId) {
        return store.getAllFileIDs(collectionId);
    }
    
    @Override 
    public long getNumberOfFilesInCollection(String collectionId) {
        return store.getNumberOfFilesInCollection(collectionId);
    }

    @Override
    public long getNumberOfFiles(String pillarId, String collectionId) {
        return store.getNumberOfExistingFilesForAPillar(pillarId, collectionId);
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId, String collectionId) {
        return store.getNumberOfMissingFilesForAPillar(pillarId, collectionId);
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId, String collectionId) {
        return store.getNumberOfChecksumErrorsForAPillar(pillarId, collectionId);
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId) {
        log.info("Setting file '" + fileId + "' in collection '" + collectionId + "' to be missing at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setFileMissing(fileId, pillarId, collectionId);
        }
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId) {
        log.info("Setting file '" + fileId + "' in collection '" + collectionId + "'to have a bad checksum at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setChecksumError(fileId, pillarId, collectionId);
        }
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId) {
        log.info("Setting file '" + fileId + "' in collection '" + collectionId + "' to have a valid checksum at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setChecksumValid(fileId, pillarId, collectionId);
        }
    }

    @Override
    public void deleteFileIdEntry(String fileId, String collectiondId) {
        log.warn("Removing all entries for the file with id '" + fileId + "'.");
        store.removeFileId(fileId, collectiondId);
    }
    
    @Override
    public List<String> findMissingChecksums(String collectionId) {
        return store.findMissingChecksums(collectionId);
    }

    @Override
    public List<String> findMissingFiles(String collectionId) {
        return store.findMissingFiles(collectionId);
    }

    /*
     * TODO make the following container:
     * class FileIDOnPillars {
     *   String fileID;
     *   List<String> pillarIDs.
     *   ......
     * }
     */
    @Override
    public List<String> getPillarsMissingFile(String fileId, String collectionId) {
        return store.getMissingAtPillars(fileId, collectionId);
    }

    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
        return store.findFilesWithOldChecksum(date, pillarID, collectionId);
    }

    @Override
    public List<String> getFilesWithInconsistentChecksums(String collectionId) {
        Date maxDate = new Date(System.currentTimeMillis() - 
                settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck());
        return store.findFilesWithInconsistentChecksums(maxDate, collectionId);
    }

    @Override
    public void setFilesWithConsistentChecksumToValid(String collectionId) {
        store.setFilesWithConsistentChecksumsToValid(collectionId);
    }

    @Override
    public Date getDateForNewestFileEntryForCollection(String collectionId) {
        return store.getDateForNewestFileEntryForCollection(collectionId);
    }
    
    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
        return store.getDateForNewestFileEntryForPillar(pillarId, collectionId);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
        return store.getDateForNewestChecksumEntryForPillar(pillarId, collectionId);
    }

    @Override
    public void setAllFilesToUnknownFileState(String collectionId) {
        store.setAllFileStatesToUnknown(collectionId);
    }

    @Override
    public void setOldUnknownFilesToMissing(String collectionId) {
        Date minDate = new Date(System.currentTimeMillis() 
                - settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck());
        store.setOldUnknownFilesToMissing(minDate, collectionId);
    }

    @Override
    public void close() {
        store.close();
    }

    @Override
    public List<String> getFilesOnPillar(String pillarId, long minId, long maxId, String collectionId) {
        return store.getFilesOnPillar(pillarId, minId, maxId, collectionId);
    }

    @Override
    public List<String> getMissingFilesAtPillar(String pillarId, long minId, long maxId, String collectionId) {
        return store.getMissingFilesOnPillar(pillarId, minId, maxId, collectionId);
    }

    @Override
    public List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId, String collectionId) {
        return store.getFilesWithChecksumErrorsOnPillar(pillarId, minId, maxId, collectionId);
    }
    
    @Override
    public Long getCollectionFileSize(String collectionId) {
        return store.getCollectionFileSize(collectionId);
    }

    @Override
    public CollectionStat getLatestCollectionStat(String collectionID) {
        return store.getLatestCollectionStats(collectionID);
    }

}
