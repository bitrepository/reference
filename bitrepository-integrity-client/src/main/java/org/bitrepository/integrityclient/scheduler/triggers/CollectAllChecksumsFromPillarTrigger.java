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

import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;

/**
 * Collects all the checksums from a given pillar, put them into the IntegrityCache, and validate the checksum.
 * It is performed by having the integrity collector updating the integrity cache, and then performing 
 * the integrity check upon the results.
 */
public class CollectAllChecksumsFromPillarTrigger extends IntervalTrigger {
    /** Sets all the file ids to true.*/
    private final String SET_ALL_FILE_IDS_TRUE = "true";
    /** The audit trail for this trigger.*/
    private final String AUDIT_TRAIL_INFORMATION = "IntegrityService Scheduling GetChecksums collector.";
    
    /** The informationCollector.*/
    private final IntegrityInformationCollector informationCollector;
    /** The id for the pillar, where all the checksums should be collected.*/
    private final String pillarId;
    /** The type of checksum for the calculation, e.g. the algorithm and optional salt.*/
    private final ChecksumSpecTYPE checksumType;
    
    /**
     * Constructor.
     * @param interval The interval between each collecting of all the checksums.
     * @param pillarId The id of the pillar.
     * @param informationCollector The initiator of the GetChecksums conversation.
     */
    public CollectAllChecksumsFromPillarTrigger(long interval, String pillarId, ChecksumSpecTYPE checksumType, 
            IntegrityInformationCollector informationCollector) {
        super(interval);
        this.informationCollector = informationCollector;
        this.pillarId = pillarId;
        this.checksumType = checksumType;
    }

    @Override
    public void run() {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs(SET_ALL_FILE_IDS_TRUE);
        
        informationCollector.getChecksums(Arrays.asList(new String[]{pillarId}), fileIDs, checksumType, 
                AUDIT_TRAIL_INFORMATION);
    }
}
