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

import java.util.Arrays;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityChecker that systematically goes through the requested files and validates their integrity.
 */
public class SimpleIntegrityChecker implements IntegrityChecker {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The settings.*/
    private final Settings settings;

    /**
     * Constructor.
     */
    public SimpleIntegrityChecker(Settings settings, IntegrityModel cache) {
        this.cache = cache;
        this.settings = settings;
    }
    
    @Override
    public IntegrityReport checkFileIDs(FileIDs fileIDs) {
        log.info("Validating the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        IntegrityReport report = new IntegrityReport();
        FileExistenceValidator fValidator = new FileExistenceValidator(cache, settings);
        for(String fileId : requestedFileIDs) {
            report.combineWithReport(fValidator.validateFile(fileId));
        }
        
        if(report.hasIntegrityIssues()) {
            log.warn("Found errors in the integrity check: " + report.generateReport());
        }
        
        return report;
    }
    
    @Override
    public IntegrityReport checkChecksum(FileIDs fileIDs) {
        log.info("Validating the checksum for the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        IntegrityReport report = new IntegrityReport();
        
        for(String fileId : requestedFileIDs) {
            ChecksumValidator checksumValidator = new ChecksumValidator(cache, fileId);
            report.combineWithReport(checksumValidator.validateChecksum());
        }
        
        if(report.hasIntegrityIssues()) {
            log.warn("Found errors in the integrity check: " + report.generateReport());
        }
        
        return report;
    }
    
    /**
     * Retrieves the collection of requested file ids.
     * @param fileIDs The file ids requested.
     * @return The collection of requested file ids.
     */
    private Collection<String> getRequestedFileIDs(FileIDs fileIDs) {
        if(fileIDs.getAllFileIDs() != null) {
            return cache.getAllFileIDs();
        } 
        return Arrays.asList(fileIDs.getFileID());
    }
}
