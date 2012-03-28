/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator for whether a file is missing at some pillars.
 */
public class FileExistenceValidator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The settings for the system.*/
    private final Settings settings;
    /** The ids of the pillars containing this file.*/
    private final List<String> pillarIds;
    
    /**
     * Constructor.
     * @param cache The cache with the integrity data.
     * @param settings The settings for the system.
     */
    public FileExistenceValidator(IntegrityModel cache, Settings settings) {
        this.cache = cache;
        this.settings = settings;
        this.pillarIds = settings.getCollectionSettings().getClientSettings().getPillarIDs();
    }
    
    /**
     * Validates which pillars have the given file id.
     * It is first validated if the date of the file is too new.
     * 
     * 
     * @param fileId The id of the file to validate.
     * @return The report for the results of the file validation.
     */
    public IntegrityReport validateFile(String fileId) {
        Collection<FileInfo> fileInfos = cache.getFileInfos(fileId);
        IntegrityReport report = new IntegrityReport();

        // validate the timestamp.
        Date date = getEarliestDate(fileInfos);
        if(!validateTimestamp(date)) {
            report.addTooNewFile(fileId);
            return report;
        }
        
        // Find the pillars which are missing the 
        List<String> pillarsMissingTheFile = findPillarsMissingTheFile(fileInfos);
        if(pillarsMissingTheFile.isEmpty()) {
            report.addFileWithoutIssue(fileId);
        } else {
            cache.setFileMissing(fileId, pillarsMissingTheFile);
            report.addMissingFile(fileId, pillarsMissingTheFile);
        }
        
        return report;
    }
    
    /**
     * Go through the pillars and find those, where the file is missing.
     * 
     * @param fileInfos The information about the given file id for the different pillars.
     * @return The ids of the pillars, where the file id missing.
     */
    private List<String> findPillarsMissingTheFile(Collection<FileInfo> fileInfos) {
        List<String> unfoundPillars = new ArrayList<String>();
        unfoundPillars.addAll(pillarIds);
        
        for(FileInfo fileinfo : fileInfos) {
            if(!unfoundPillars.remove(fileinfo.getPillarId())) {
                log.warn("Not expected pillar '" + fileinfo.getPillarId() + "' for file '" + fileinfo.getFileId() 
                        + "'");
            }
        }
        return unfoundPillars;
    }
    
    /**
     * Finds the earliest date for the file on any pillar.
     * 
     * @param fileInfos The information about the file on the pillars.
     * @return The earliest date for the file.
     */
    private Date getEarliestDate(Collection<FileInfo> fileInfos) {
        Date res = new Date();
        for(FileInfo fileinfo : fileInfos) {
            Date fileDate = CalendarUtils.convertFromXMLGregorianCalendar(fileinfo.getDateForLastFileIDCheck());
            if(res.getTime() > fileDate.getTime()) {
                res = fileDate;
            }
        }
        
        return res;
    }
    
    /**
     * @param date The earliest date for the given file.
     * @return Whether more time has passed since the timestamp than required by the settings.
     */
    private boolean validateTimestamp(Date date) {
        long millisSinceTimestamp = new Date().getTime() - date.getTime();
        return millisSinceTimestamp 
                > settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck();
    }
}
