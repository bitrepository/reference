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
package org.bitrepository.integrityclient.scheduler.triggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityclient.cache.FileInfo;
import org.bitrepository.integrityclient.cache.IntegrityModel;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects the checksums from the pillars when their latest update has exceeded the interval.
 * The messages will be sent individually for each file.
 */
public class CollectObsoleteChecksumsTrigger extends IntervalTrigger {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The audit trail for this trigger.*/
    private final String AUDIT_TRAIL_INFORMATION = "IntegrityService Scheduling GetChecksums collector";
    
    /** The informationCollector.*/
    private final IntegrityInformationCollector informationCollector;
    /** The type of checksum for the calculation, e.g. the algorithm and optional salt.*/
    private final ChecksumSpecTYPE checksumType;
    /** The time to exceed. */
    private final long maxTimeToLastUpdate;
    /** The cache with the information about the previously collected integrity information.*/
    private final IntegrityModel cache;
    
    /**
     * Constructor.
     * @param triggerInterval The interval between each collecting of all the checksums.
     * @param maxTimeToLastUpdate The interval for the latest checksum update to exceed.
     * @param checksumType The type of checksum to request being calculated.
     * @param informationCollector The initiator of the GetChecksums conversation.
     * @param cache The cache with integrity information.
     */
    public CollectObsoleteChecksumsTrigger(long triggerInterval, long maxTimeToLastUpdate, 
            ChecksumSpecTYPE checksumType, IntegrityInformationCollector informationCollector, IntegrityModel cache) {
        super(triggerInterval);
        this.informationCollector = informationCollector;
        this.checksumType = checksumType;
        this.maxTimeToLastUpdate = maxTimeToLastUpdate;
        this.cache = cache;
    }

    @Override
    public void run() {
        Date latestUpdate = new Date(new Date().getTime() - maxTimeToLastUpdate);
        
        // Handle each file id individually.
        for(String fileid : cache.getAllFileIDs()) {
            Collection<FileInfo> fileIDInfos = cache.getFileInfos(fileid);
            List<String> pillarsToUpdate = new ArrayList<String>();
            
            // Find the pillars with exceeded update time.
            for(FileInfo fileinfo : fileIDInfos) {
                if(fileinfo.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis() 
                        < latestUpdate.getTime()) {
                    pillarsToUpdate.add(fileinfo.getPillarId());
                }
            }
            
            // If any such pillars then collect the checksum of the file from them.
            if(!pillarsToUpdate.isEmpty()) {
                log.info("Updating obesolete checksum for the file '" + fileid + "' from the pillars '"
                        + pillarsToUpdate + "'");
                FileIDs fileIDs = new FileIDs();
                fileIDs.setFileID(fileid);
                informationCollector.getChecksums(pillarsToUpdate, fileIDs, checksumType, 
                        AUDIT_TRAIL_INFORMATION);
            }
        }
    }
}
