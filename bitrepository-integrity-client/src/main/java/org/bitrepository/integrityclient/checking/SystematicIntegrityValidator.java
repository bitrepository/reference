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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.integrityclient.cache.FileIDInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityChecker that systematically goes through the requested files and validates their integrity.
 */
public class SystematicIntegrityValidator implements IntegrityChecker {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for */
    private final CachedIntegrityInformationStorage cache;
    /** The settings.*/
    private final Settings settings;

    /**
     * Constructor.
     */
    public SystematicIntegrityValidator(Settings settings, CachedIntegrityInformationStorage cache) {
        this.cache = cache;
        this.settings = settings;
    }
    
    @Override
    public boolean checkFileIDs(FileIDs fileIDs) {
        log.info("Validating the file ids for the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        List<String> invalidFileIDs = new ArrayList<String>();
        
        for(String fileId : requestedFileIDs) {
            if(!checkFileID(fileId)) {
                invalidFileIDs.add(fileId);
            }
        }
        
        if(!invalidFileIDs.isEmpty()) {
            // TODO send an Alarm instead.
            StringBuilder errMsg = new StringBuilder("Invalid fileids: " + "\n"); 
            for(String e : invalidFileIDs) {
                errMsg.append(e.toString());
                errMsg.append("\n");
            }
            log.warn(errMsg.toString());
            return false;
//            throw new RuntimeException(errMsg.toString());
        }
        return true;
    }
    
    @Override
    public boolean checkChecksum(FileIDs fileIDs) {
        log.info("Validating the checksum for the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        List<InvalidChecksumException> invalidChecksums = new ArrayList<InvalidChecksumException>();
        
        for(String fileId : requestedFileIDs) {
            try {
                checkChecksum(fileId);
            } catch (InvalidChecksumException e) {
                invalidChecksums.add(e);
            }
        }
        
        if(!invalidChecksums.isEmpty()) {
            // TODO send an Alarm instead.
            StringBuilder errMsg = new StringBuilder("Invalid checksums: " + "\n"); 
            for(InvalidChecksumException e : invalidChecksums) {
                errMsg.append(e.toString());
                errMsg.append("\n");
            }
            log.warn(errMsg.toString());
            return false;
//            throw new RuntimeException(errMsg.toString());
        }
        return true;
    }
    
    /**
     * Retrieves the collection of requested file ids.
     * @param fileIDs The file ids requested.
     * @return The collection of requested file ids.
     */
    private Collection<String> getRequestedFileIDs(FileIDs fileIDs) {
        if(fileIDs.getAllFileIDs() != null) {
            return cache.getAllFileIDs();
        } else if(fileIDs.getParameterAddress() != null){
            // TODO handle the parameter address?
            throw new IllegalStateException("Cannot use the 'ParameterAddress'");
        } 
        return fileIDs.getFileID();
    }
    
    /**
     * Checks whether all pillars contain a given file id.
     * 
     * @param fileId The id of the file to validate.
     * @return Whether all the pillars have the given file.
     */
    private boolean checkFileID(String fileId) {
        List<String> unfoundPillars = settings.getCollectionSettings().getClientSettings().getPillarIDs();
        
        for(FileIDInfo fileinfo : cache.getFileInfo(fileId)) {
            // validate that it is the requested file id 
            if(!fileinfo.getFileID().equals(fileId)) {
                log.warn("Unexpected FileInfo found for file '" + fileId + "': {}", fileinfo);
                continue;
            } 
            
            unfoundPillars.remove(fileinfo.getPillarId());
            
            // TODO check the time.
        }
        
        return unfoundPillars.isEmpty();
    }
    
    /**
     * Validates the checksum for a given file id is identical at all pillars.
     * TODO validate the checksum type (e.g. algorithm and salt).
     * TODO also validate, that all pillars contains the files.
     * 
     * @param fileId The id of the pillar to have its checksums validated. 
     * @throws InvalidChecksumException If any 
     */
    private void checkChecksum(String fileId) throws InvalidChecksumException {
        Map<String, Integer> checksumCount = new HashMap<String, Integer>();
        
        for(FileIDInfo fileinfo : cache.getFileInfo(fileId)) {
            // validate that it is the requested file id 
            if(!fileinfo.getFileID().equals(fileId)) {
                log.warn("Unexpected FileInfo found for file '" + fileId + "': {}", fileinfo);
                continue;
            } 
            
            // Validate that the checksum has been found.
            String checksum = fileinfo.getChecksum();
            if(checksum == null || checksum.isEmpty()) {
                log.warn("The file '" + fileId + "' is missing checksum at '" + fileinfo.getPillarId() 
                        + "'. Ignoring: {}", fileinfo);
                continue;
            }
            
            if(checksumCount.containsKey(checksum)) {
                Integer count = checksumCount.get(checksum);
                checksumCount.put(checksum, count + 1);
            } else {
                checksumCount.put(checksum, 1);                
            }
            
            // TODO check the time.
        }
        
        if(checksumCount.size() != 1) {
            throw new InvalidChecksumException(fileId, checksumCount);
        } 
    }
}
