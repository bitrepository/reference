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
package org.bitrepository.integrityservice.workflow.step;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityCollectorEventHandler;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for collecting the checksums of all files from all pillars.
 */
public class GetChecksumForFileStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The collector for retrieving the checksums.*/
    private final IntegrityInformationCollector collector;
    /** The model where the integrity data is stored.*/
    protected final IntegrityModel store;
    /** The checksum spec type.*/
    private final ChecksumSpecTYPE checksumType;
    /** The integrity alerter.*/
    private final IntegrityAlerter alerter;
    /** The timeout for waiting for the results of the GetChecksums operation.*/
    private final Long timeout;
    /** The ID of the file to retrieve the checksum of.*/
    protected final String fileID;
    /** The collectionID */
    protected final String collectionID;
    /** Contributors for collecting information */
    private final IntegrityContributors integrityContributors;
    
    /**
     * Constructor.
     * @param collector The client for collecting the checksums.
     * @param store The storage for the integrity data.
     * @param alerter The alerter for sending failures.
     * @param checksumType The type of checksum to collect.
     */
    public GetChecksumForFileStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            ChecksumSpecTYPE checksumType, String fileID, Settings settings, String collectionID, IntegrityContributors integrityContributors) {
        this.collector = collector;
        this.store = store;
        this.checksumType = checksumType;
        this.alerter = alerter;
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.integrityContributors = integrityContributors;
        this.timeout = settings.getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue()
                + settings.getRepositorySettings().getClientSettings().getOperationTimeout().longValue();
    }
    
    @Override
    public synchronized void performStep() throws WorkflowAbortedException {
        try {
            Set<String> pillarsToCollectFrom =  integrityContributors.getActiveContributors();
            log.debug("Collecting checksums from '" + pillarsToCollectFrom + "' for collection '" 
                    + collectionID + "'.");
            IntegrityCollectorEventHandler eventHandler = new IntegrityCollectorEventHandler(store, 
                    timeout, integrityContributors);
            collector.getChecksums(collectionID, pillarsToCollectFrom, checksumType, fileID, "IntegrityService: "
                    + getName(), null, eventHandler);

            OperationEvent event = eventHandler.getFinish();
            if(event.getEventType() == OperationEventType.FAILED) {
                handleFailureEvent(event);
            }
            log.debug("Collecting of checksums ids had the final event: " + event);
        } catch (InterruptedException e) {
            log.warn("Interrupted while collecting checksums.", e);
        }
    }
    
    /**
     * Handle a failure event. This includes checking if any contributors have failed (if not just retry), 
     * checking to see if the workflow should be aborted, and sending alarms if needed.  
     */
    private void handleFailureEvent(OperationEvent event) {
        if(integrityContributors.getFailedContributors().isEmpty()) {
            log.info("Get failure event, but no contributors marked as failed.");
        } else {
            OperationFailedEvent ofe = (OperationFailedEvent) event;
            log.info("Failure occured collecting fileIDs, continuing collecting checksums. Failure {}", ofe.toString());
            alerter.integrityFailed("Failure while collecting checksums, the check will continue "
                    + "with the information available. The failed contributors were: " 
                    + integrityContributors.getFailedContributors(), collectionID);
        }
    }

    @Override
    public String getName() {
        return "Performing the GetChecksum for file '" + fileID + "' with the algorithm '"+ checksumType + "'";
    }
}
