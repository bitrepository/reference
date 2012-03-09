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
package org.bitrepository.integrityclient.workflow.scheduler;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.protocol.eventhandler.EventHandler;

/**
 * Collects all the fileids from a given pillar.
 */
/**
 * Collects all the file ids from a given pillar, put them into the IntegrityCache, and validate them.
 * It is performed by having the integrity collector updating the integrity cache, and then performing 
 * the integrity check upon the results.
 */
public class CollectAllFileIDsWorkflow extends IntervalWorkflow {
    /** Sets all the file ids to true.*/
    private final String SET_ALL_FILE_IDS_TRUE = "true";
    /** The audit trail for this trigger.*/
    private final String AUDIT_TRAIL_INFORMATION = "IntegrityService Scheduling GetFileIDs collector";
    
    /** The informationCollector.*/
    private final IntegrityInformationCollector informationCollector;
    /** The settings for this workflow.*/
    private final Settings settings;
    /** The eventhandler for handling the results of collecting in this workflow.*/
    private final EventHandler eventHandler;
    
    /**
     * Constructor.
     * @param interval The interval between each collecting of all the fileids.
     * @param pillarId The id of the pillar.
     * @param informationCollector The initiator of the GetFileIDs conversation.
     * @param eventHandler The eventhandler for handling the results of collecting all the file ids.
     */
    public CollectAllFileIDsWorkflow(long interval, Settings settings, 
            IntegrityInformationCollector informationCollector, EventHandler eventHandler) {
        super(interval);
        this.informationCollector = informationCollector;
        this.settings = settings;
        this.eventHandler = eventHandler;
    }

    @Override
    public void runWorkflow() {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs(SET_ALL_FILE_IDS_TRUE);

        informationCollector.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), fileIDs, 
                AUDIT_TRAIL_INFORMATION, eventHandler);
    }
}
