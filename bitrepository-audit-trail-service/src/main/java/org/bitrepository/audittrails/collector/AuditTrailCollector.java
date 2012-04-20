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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.settings.Settings;

/**
 * Manages the retrieval of of AuditTrails from contributors.
 */
public class AuditTrailCollector {
    /** The task for collecting the audits.*/
    private final TimerTask auditCollector;
    /** The timer for keeping track of the collecting task.*/
    private final Timer timer;
    
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
    
    /**
     * Constructor.
     * @param settings The settings for this collector.
     * @param client The client for handling the conversation for collecting the audit trails.
     * @param store The storage of the audit trails data.
     */
    public AuditTrailCollector(Settings settings, AuditTrailClient client, AuditTrailStore store) {
        this.client = client;
        this.settings = settings;
        this.store = store;
        this.timer = new Timer();
        
        auditCollector = new AuditTimerTask();
        timer.schedule(auditCollector, 3600000);
    }
    
    /**
     * Instantiates a collection of all the newest audit trails.
     */
    public void collectNewestAudits() {
        timer.purge();
        auditCollector.cancel();
        timer.schedule(auditCollector, 3600000);        
        
        List<AuditTrailQuery> queries = new ArrayList<AuditTrailQuery>();
        
        for(String contributorId : settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors()) {
            int seq = store.largestSequenceNumber(contributorId);
            queries.add(new AuditTrailQuery(contributorId, seq));
        }
        
        EventHandler handler = new AuditCollectorEventHandler();
        
        client.getAuditTrails((AuditTrailQuery[]) queries.toArray(), NO_FILE_ID, NO_DELIVERY_URL, handler, 
                settings.getReferenceSettings().getAuditTrailServiceSettings().getID());
    }
    
    /**
     * Timer task for keeping track of the automated collecting of audit trails.
     */
    private class AuditTimerTask extends TimerTask {
        @Override
        public void run() {
            collectNewestAudits();
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
            }
        }
    }
}
