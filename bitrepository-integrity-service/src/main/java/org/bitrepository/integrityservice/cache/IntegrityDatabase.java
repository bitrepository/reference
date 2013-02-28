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
        this.store = new IntegrityDAO(new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID());
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        store.updateFileIDs(data, pillarId);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId) {
        store.updateChecksumData(data, pillarId);
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
    public long getNumberOfChecksumErrors(String pillarId) {
        return store.getNumberOfChecksumErrorsForAPillar(pillarId);
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds) {
        log.info("Setting file '" + fileId + "' to be missing at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setFileMissing(fileId, pillarId);
        }
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds) {
        log.info("Setting file '" + fileId + "' to have a bad checksum at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setChecksumError(fileId, pillarId);
        }
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds) {
        log.info("Setting file '" + fileId + "' to have a valid checksum at '" + pillarIds + "'.");
        for(String pillarId : pillarIds) {
            store.setChecksumValid(fileId, pillarId);
        }
    }

    @Override
    public void deleteFileIdEntry(String fileId) {
        log.warn("Removing all entries for the file with id '" + fileId + "'.");
        store.removeFileId(fileId);
    }
    
    @Override
    public List<String> findMissingChecksums() {
        return store.findMissingChecksums();
    }

    @Override
    public List<String> findMissingFiles() {
        return store.findMissingFiles();
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
    public List<String> getPillarsMissingFile(String fileId) {
        return store.getMissingAtPillars(fileId);
    }

    @Override
    public Collection<String> findChecksumsOlderThan(Date date, String pillarID) {
        return store.findFilesWithOldChecksum(date, pillarID);
    }

    @Override
    public List<String> getFilesWithInconsistentChecksums() {
        Date maxDate = new Date(System.currentTimeMillis() - 
                settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck());
        return store.findFilesWithInconsistentChecksums(maxDate);
    }

    @Override
    public void setFilesWithConsistentChecksumToValid() {
        store.setFilesWithConsistentChecksumsToValid();
    }

    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId) {
        return store.getDateForNewestFileEntryForPillar(pillarId);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId) {
        return store.getDateForNewestChecksumEntryForPillar(pillarId);
    }

    @Override
    public void setAllFilesToUnknownFileState() {
        store.setAllFileStatesToUnknown();
    }

    @Override
    public void setOldUnknownFilesToMissing() {
        Date minDate = new Date(System.currentTimeMillis() 
                - settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck());
        store.setOldUnknownFilesToMissing(minDate);
    }

    @Override
    public void close() {
        store.close();
    }

    @Override
    public List<String> getFilesOnPillar(String pillarId, long minId, long maxId) {
        return store.getFilesOnPillar(pillarId, minId, maxId);
    }

    @Override
    public List<String> getMissingFilesAtPillar(String pillarId, long minId, long maxId) {
        return store.getMissingFilesOnPillar(pillarId, minId, maxId);
    }

    @Override
    public List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId) {
        return store.getFilesWithChecksumErrorsOnPillar(pillarId, minId, maxId);
    }

}
