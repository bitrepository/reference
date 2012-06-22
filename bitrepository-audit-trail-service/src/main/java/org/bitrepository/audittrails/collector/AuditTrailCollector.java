/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.audittrails.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the retrieval of of AuditTrails from contributors.
 */
public class AuditTrailCollector {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The task for collecting the audits.*/
    private final AuditTrailCollectionTimerTask auditCollector;
    /** The timer for keeping track of the collecting task.*/
    private Timer timer;
    
    /** The audit trail client for collecting the audit trails.*/
    private final AuditTrailClient client;
    /** The audit trail store for inserting the collected audit trails and retrieving the largest sequence number 
     * for each contributor.*/
    private final AuditTrailStore store;

    /** The settings for this collector.*/
    private final Settings settings;
    
    /** When no file id is wanted for the collecting of audit trails.*/
    private static final String NO_FILE_ID = null;
    /** When no delivery address is wanted for the collecting of audit trails.*/
    private static final String NO_DELIVERY_URL = null;
    /** The time between checking whether the audits should be collected*/
    private final Long timebetweenCollectChecksum;
    
    /**
     * Constructor.
     * @param settings The settings for this collector.
     * @param client The client for handling the conversation for collecting the audit trails.
     * @param store The storage of the audit trails data.
     */
    public AuditTrailCollector(Settings settings, AuditTrailClient client, AuditTrailStore store) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(client, "AuditTrailClient client");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        
        this.client = client;
        this.settings = settings;
        this.store = store;
        this.timer = new Timer();
        this.timebetweenCollectChecksum 
                = settings.getReferenceSettings().getAuditTrailServiceSettings().getTimerTaskCheckInterval();
        
        auditCollector = new AuditTrailCollectionTimerTask(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getCollectAuditInterval());
        timer.scheduleAtFixedRate(auditCollector, 0, timebetweenCollectChecksum);
    }
    
    /**
     * Instantiates a collection of all the newest audit trails.
     */
    public void collectNewestAudits() {
        auditCollector.performCollection();
    }
    
    /**
     * Closes the AuditTrailCollector.
     */
    public void close() {
        auditCollector.cancel();
        timer.cancel();
    }

    /**
     * Timer task for keeping track of the automated collecting of audit trails.
     */
    private class AuditTrailCollectionTimerTask extends TimerTask {
        /** The interval between running this timer task.*/
        private final long interval;
        /** The date for the next run.*/
        private Date nextRun;
        
        /**
         * Constructor.
         * @param interval The interval between running this timer task.
         */
        private AuditTrailCollectionTimerTask(long interval) {
            this.interval = interval;
            nextRun = new Date(System.currentTimeMillis() + interval);
        }
        
        /**
         * Reset the date for next run and then run the operation.
         */
        public void performCollection() {
            nextRun = new Date(System.currentTimeMillis() + interval);
            performCollectionOfAudits();
        }
        
        /**
         * Setup and initiates the collection of audit trails through the client.
         * Adds one to the sequence number to request only newer audit trails.
         */
        private void performCollectionOfAudits() {
            List<AuditTrailQuery> queries = new ArrayList<AuditTrailQuery>();
            
            for(String contributorId : settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs()) {
                int seq = store.largestSequenceNumber(contributorId);
                queries.add(new AuditTrailQuery(contributorId, seq + 1));
            }
            
            EventHandler handler = new AuditCollectorEventHandler();
            client.getAuditTrails(queries.toArray(new AuditTrailQuery[queries.size()]), NO_FILE_ID, NO_DELIVERY_URL, 
                    handler, settings.getReferenceSettings().getAuditTrailServiceSettings().getID());
        }
        
        @Override
        public void run() {
            if(nextRun.getTime() < System.currentTimeMillis()) {
                performCollection();
            }
        }
    }
    
    /**
     * Event handler for the audit trail collector. The results of an audit trail operation will be ingested into the
     * audit trail store.
     */
    private class AuditCollectorEventHandler implements EventHandler {
        @Override
        public void handleEvent(OperationEvent event) {
            if(event instanceof AuditTrailResult) {
                AuditTrailResult auditEvent = (AuditTrailResult) event;
                store.addAuditTrails(auditEvent.getAuditTrailEvents().getAuditTrailEvents());
            } else if(event.getType() == OperationEventType.COMPONENT_FAILED ||
                    event.getType() == OperationEventType.FAILED ||
                    event.getType() == OperationEventType.IDENTIFY_TIMEOUT ||
                    event.getType() == OperationEventType.NO_COMPONENT_FOUND) {
                log.warn("Event: " + event.toString());
            } else {
                log.debug("Event:" + event.toString());
            }
        }
    }
}
