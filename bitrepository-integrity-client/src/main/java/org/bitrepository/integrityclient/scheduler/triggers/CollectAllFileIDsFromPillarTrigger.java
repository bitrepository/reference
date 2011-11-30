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

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;

/**
 * Collects all the fileids from a given pillar.
 */
public class CollectAllFileIDsFromPillarTrigger extends IntervalTrigger {
    /** The informationCollector.*/
    IntegrityInformationCollector informationCollector;
    /** The id for the pillar, where all the file ids should be collected.*/
    String pillarId;
    
    /**
     * Constructor.
     * @param interval The interval between each collecting of all the fileids.
     * @param pillarId The id of the pillar.
     * @param informationCollector The initiator of the GetFileIDs conversation.
     */
    public CollectAllFileIDsFromPillarTrigger(long interval, String pillarId, IntegrityInformationCollector informationCollector) {
        super(interval);
        this.informationCollector = informationCollector;
        this.pillarId = pillarId;
    }

    @Override
    public void run() {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        
        informationCollector.getFileIDs(Arrays.asList(new String[]{pillarId}), fileIDs, 
                "IntegrityService Scheduling GetFileIDs collector");
    }
}
