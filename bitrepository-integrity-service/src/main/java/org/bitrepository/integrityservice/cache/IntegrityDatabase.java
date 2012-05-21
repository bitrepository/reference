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

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage of configuration information that is backed by a database.
 */
public class IntegrityDatabase implements IntegrityModel {
    /** The settings for the database cache. */
    private final Settings settings;
    
    /** The database store. The interface to the functions of the database. */
    private final IntegrityDAO store;
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Initialize storage.
     *
     * @param storageConfiguration Contains configuration for storage. Currently URL, user and pass for database.
     */
    public IntegrityDatabase(Settings settings) {
        this.settings = settings;
        this.settings.getReferenceSettings().getIntegrityServiceSettings();
        
        Connection dbConnection = initDatabaseConnection();
        this.store = new IntegrityDAO(dbConnection);
    }
    
    /**
     * Retrieve the access to the database. If it cannot be done, then it is automatically attempted to instantiate 
     * the database based on the SQL script.
     * @return The connection to the database.
     */
    private Connection initDatabaseConnection() {
        // TODO make a better instantiation, which is not depending on Derby.
        DBConnector dbConnector = new DerbyDBConnector();
        
        try {
            Connection dbConnection = dbConnector.getEmbeddedDBConnection(
                    settings.getReferenceSettings().getIntegrityServiceSettings().getDatabaseUrl());
            return dbConnection;
        } catch (Exception e) {
            log.warn("Could not instantiate the database with the url '"
                    + settings.getReferenceSettings().getIntegrityServiceSettings().getDatabaseUrl() + "'", e);
        }
        
        log.info("Trying to instantiate the database.");
        // TODO handle this!
//        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//                "src/main/resources/integrityDB.sql");
        File sqlDatabaseFile = new File("src/main/resources/integrityDB.sql");
        dbConnector.createDatabase(sqlDatabaseFile);
        
        // Connect to the new instantiated database.
        try { 
            Connection dbConnection = dbConnector.getEmbeddedDBConnection(
                    settings.getReferenceSettings().getIntegrityServiceSettings().getDatabaseUrl());
            return dbConnection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database with the url '"
                    + settings.getReferenceSettings().getIntegrityServiceSettings().getDatabaseUrl() + "'", e);
        }
    }
    
    /**
     * Retrieve the interface to the database, which stores the integrity data.
     * @return The database store.
     */
    public IntegrityDAO getStore() {
        return store;
    }
    
    @Override
    public void addFileIDs(FileIDsData data, FileIDs expectedFileIDs, String pillarId) {
        store.updateFileIDs(data, pillarId);
        
        List<String> missingFiles = retrieveMissingFileIDs(data, expectedFileIDs);
        for(String fileId : missingFiles) {
            setFileMissing(fileId, Arrays.asList(pillarId));
        }
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
    
    /**
     * Find the difference between the expected file ids and the delivered ones.
     * 
     * @param data The delivered file ids data.
     * @param expectedFileIDs The expected file ids.
     * @return The list containing the difference between expected and received data items.
     */
    private List<String> retrieveMissingFileIDs(FileIDsData data, FileIDs expectedFileIDs) {
        List<String> unfoundFiles;
        
        if(expectedFileIDs.isSetAllFileIDs()) {
            unfoundFiles = store.getAllFileIDs();
        } else {
            unfoundFiles = new ArrayList<String>();
            unfoundFiles.add(expectedFileIDs.getFileID());
        }
        
        for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
            unfoundFiles.remove(dataItem.getFileID());
        }
        
        return unfoundFiles;
    }
}
