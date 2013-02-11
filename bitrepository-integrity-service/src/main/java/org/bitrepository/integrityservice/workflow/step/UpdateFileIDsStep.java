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
import java.util.Date;
import java.util.List;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityCollectorEventHandler;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for collecting of all file ids from all pillars.
 */
public class UpdateFileIDsStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The collector for retrieving the file ids.*/
    private final IntegrityInformationCollector collector;
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The integrity alerter.*/
    private final IntegrityAlerter alerter;
    /** The pillar ids.*/
    private final List<String> pillarIds;
    /** The timeout for waiting for the results of the GetFileIDs operation.*/
    private final Long timeout;
    /** The maximum number of results for each conversation.*/
    private final Integer maxNumberOfResultsPerConversation;
    
    /** The default value for the maximum number of results for each conversation. Is case the setting is missing.*/
    private final Integer DEFAULT_MAX_RESULTS = 10000;
    
    /**
     * Constructor.
     * @param collector The client for collecting the checksums.
     * @param store The storage for the integrity data.
     * @param alerter The alerter for sending failures.
     * @param settings The settings to use.
     */
    public UpdateFileIDsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            Settings settings) {
        this.collector = collector;
        this.store = store;
        this.alerter = alerter;
        this.pillarIds = settings.getCollectionSettings().getClientSettings().getPillarIDs();
        this.timeout = settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue()
                + settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
        if(settings.getReferenceSettings().getIntegrityServiceSettings().getMaximumNumberOfResultsPerConversation() 
                != null) {
            this.maxNumberOfResultsPerConversation = settings.getReferenceSettings().getIntegrityServiceSettings()
                    .getMaximumNumberOfResultsPerConversation().intValue();
        } else {
            this.maxNumberOfResultsPerConversation = DEFAULT_MAX_RESULTS;
        }
    }
    
    @Override
    public String getName() {
        return "Collect all fileIDs from pillars";
    }

    @Override
    public synchronized void performStep() {
        store.setAllFilesToUnknownFileState();

        try {
            List<String> pillarsToCollectFrom = new ArrayList<String>(pillarIds);
            while (!pillarsToCollectFrom.isEmpty()) {
                IntegrityCollectorEventHandler eventHandler = new IntegrityCollectorEventHandler(store, alerter, timeout);
                ContributorQuery[] queries = getQueries(pillarsToCollectFrom);
                collector.getFileIDs(pillarsToCollectFrom, "IntegrityService: " + getName(), queries, eventHandler);
                
                OperationEvent event = eventHandler.getFinish();
                log.debug("Collection of file ids had the final event: " + event);
                pillarsToCollectFrom = new ArrayList<String>(eventHandler.getPillarsWithPartialResult());
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while collecting file ids.", e);
        }
    }
    
    /**
     * Define the queries for the collection of FileIDs for the given pillars.
     * @param pillars The pillars to collect from.
     * @return The queries for the pillars for collecting the file ids.
     */
    private ContributorQuery[] getQueries(List<String> pillars) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        for(String pillar : pillars) {
            Date latestFileIDEntry = store.getDateForNewestFileEntryForPillar(pillar);
            res.add(new ContributorQuery(pillar, latestFileIDEntry, null, maxNumberOfResultsPerConversation));
        }
        
        return res.toArray(new ContributorQuery[pillars.size()]);
    }

    public static String getDescription() {
        return "Contacts all pillar to retrieve the full list of files from the pillars";
    }
}
