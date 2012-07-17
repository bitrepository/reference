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

import java.util.Date;
import java.util.HashSet;

import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReport;

/**
 * Finds obsolete checksums.
 */
public class ObsoleteChecksumFinder {
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    
    /**
     * Constructor.
     * @param cache The cache with the integrity model.
     */
    public ObsoleteChecksumFinder(IntegrityModel cache) {
        this.cache = cache;
    }
    
    /**
     * Performs the obsolete checksum report check and delivers the report.
     * @param timeout The amount of milliseconds for a checksum to become obsolete.
     * @return The report for the obsolete checksums check.
     */
    public ObsoleteChecksumReport generateReport(long timeout) {
        ObsoleteChecksumReport report = new ObsoleteChecksumReport();
        Long outDated = System.currentTimeMillis() - timeout;
        HashSet<String> filesWithOldChecksum = new HashSet<String>(cache.findChecksumsOlderThan(new Date(outDated)));
        for(String fileId : filesWithOldChecksum) {
            for(FileInfo fileinfo : cache.getFileInfos(fileId)) {
                if(fileinfo.getFileState() != FileState.EXISTING) {
                    continue;
                }
                
                if(CalendarUtils.convertFromXMLGregorianCalendar(fileinfo.getDateForLastChecksumCheck()).getTime()
                        < outDated) {
                    report.reportMissingChecksum(fileId, fileinfo.getPillarId(), 
                            fileinfo.getDateForLastChecksumCheck());
                }
            }
        }
        
        return report;
    }
}
