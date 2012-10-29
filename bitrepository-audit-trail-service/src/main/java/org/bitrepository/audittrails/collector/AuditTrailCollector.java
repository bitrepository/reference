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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.audittrails.store.AuditTrailStore;
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
    private final AuditTrailCollectionTimerTask collectorTask;
    /** The timer for keeping track of the collecting task.*/
    private Timer timer;
    /** The settings for this collector.*/
    private final Settings settings;

    /** Initial graze period in milliseconds after startup to allow the system to finish startup. */
    private static final int DEFAULT_GRACE_PERIOD = 0;

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

        IncrementalCollector collector = new IncrementalCollector(
            settings.getReferenceSettings().getAuditTrailServiceSettings().getID(),
            client, store,
            settings.getReferenceSettings().getAuditTrailServiceSettings().getMaxNumberOfEventsInRequest());
        this.settings = settings;
        this.timer = new Timer();
        collectorTask = new AuditTrailCollectionTimerTask(
                collector,
                settings.getReferenceSettings().getAuditTrailServiceSettings().getCollectAuditInterval());
        timer.scheduleAtFixedRate(collectorTask, getGracePeriod(),
            settings.getReferenceSettings().getAuditTrailServiceSettings().getTimerTaskCheckInterval());
    }
    
    /**
     * Instantiates a collection of all the newest audit trails.
     */
    public void collectNewestAudits() {
        collectorTask.runCollection();
    }

    private int getGracePeriod() {
        int gracePeriod = (settings.getReferenceSettings().getAuditTrailServiceSettings().isSetGracePeriod()) ?
            settings.getReferenceSettings().getAuditTrailServiceSettings().getGracePeriod().intValue() : DEFAULT_GRACE_PERIOD;
        return gracePeriod;
    }
    
    /**
     * Closes the AuditTrailCollector.
     */
    public void close() {
        collectorTask.cancel();
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
        private final IncrementalCollector collector;
        
        /**
         * Constructor.
         * @param interval The interval between running this timer task.
         */
        private AuditTrailCollectionTimerTask(IncrementalCollector collector, long interval) {
            this.collector = collector;
            this.interval = interval;
            nextRun = new Date(System.currentTimeMillis() + interval);
        }
        
        /**
         * Run the operation and the reset the date for next run and then r.
         */
        public synchronized void runCollection() {
            collector.performCollection(settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs());
            nextRun = new Date(System.currentTimeMillis() + interval);
        }

        @Override
        public void run() {
            if(nextRun.getTime() < System.currentTimeMillis()) {
                runCollection();
            }
        }
    }
}
