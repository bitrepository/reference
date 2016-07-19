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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.audittrails.webservice.CollectorInfo;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the retrieval of of AuditTrails from contributors.
 */
public class AuditTrailCollector {
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The task for collecting the audits.*/
    private final Map<String, AuditTrailCollectionTimerTask> collectorTasks = new HashMap<String, 
            AuditTrailCollectionTimerTask>();
    /** The timer for keeping track of the collecting task.*/
    private Timer timer;
    private final Settings settings;

    /** Initial grace period in milliseconds after startup to allow the system to finish startup. */
    private static final int DEFAULT_GRACE_PERIOD = 0;

    /**
     * @param settings The settings for this collector.
     * @param client The client for handling the conversation for collecting the audit trails.
     * @param store The storage of the audit trails data.
     * @param alarmDispatcher the alarm dispatcher. Can be null.
     */
    public AuditTrailCollector(Settings settings, AuditTrailClient client, AuditTrailStore store, 
            AlarmDispatcher alarmDispatcher) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(client, "AuditTrailClient client");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        ArgumentValidator.checkNotNull(alarmDispatcher, "AlarmDispatcher alarmDispatcher");


        this.settings = settings;
        this.timer = new Timer(true);
        long collectionInterval = settings.getReferenceSettings().getAuditTrailServiceSettings().getCollectAuditInterval();
        
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            IncrementalCollector collector = new IncrementalCollector(c.getID(),
                    settings.getReferenceSettings().getAuditTrailServiceSettings().getID(),
                    client, store,
                    SettingsUtils.getMaxClientPageSize(), 
                    alarmDispatcher);
            AuditTrailCollectionTimerTask collectorTask = new AuditTrailCollectionTimerTask( 
                    collector, collectionInterval, getGracePeriod());
            log.info("Will start collection of audit trail every " +
                    TimeUtils.millisecondsToHuman(collectionInterval) + ", " +
                    "after a grace period of " + TimeUtils.millisecondsToHuman(getGracePeriod()));
            timer.scheduleAtFixedRate(collectorTask, getGracePeriod(), collectionInterval/10);
            collectorTasks.put(c.getID(), collectorTask);
        }
    }
    
    public CollectorInfo getCollectorInfo(String collectionID) {
        CollectorInfo info = new CollectorInfo();
        info.setCollectionID(collectionID);
        Date lastStart = collectorTasks.get(collectionID).getLastCollectionStart();
        Date lastFinish = collectorTasks.get(collectionID).getLastCollectionFinish();
        Date nextRun = collectorTasks.get(collectionID).getNextScheduledRun();
        if(lastStart != null) {
            info.setLastStart(TimeUtils.shortDate(lastStart));
            if(lastFinish != null) {
                long duration = lastFinish.getTime() - lastStart.getTime();
                info.setLastDuration(TimeUtils.millisecondsToHuman(duration));
            } else {
                info.setLastDuration("Collection has not finished yet");
            }
        } else {
            info.setLastStart("Audit trail collection have not started");
            info.setLastDuration("Not available");
        }
        info.setNextStart(TimeUtils.shortDate(nextRun));
        info.setCollectedAudits(collectorTasks.get(collectionID).getLastNumberOfCollectedAudits());
        return info;
    }
    
    /**
     * Instantiates a collection of all the newest audit trails.
     * @param collectionID the collectionID
     */
    public void collectNewestAudits(String collectionID) {
        collectorTasks.get(collectionID).runCollection();
    }

    /**
     * @return The time to wait before starting collection of audit trails. This enables the system to have time to
     * finish startup before they have to start delivering/process audit trails.
     */
    private int getGracePeriod() {
        if (settings.getReferenceSettings().getAuditTrailServiceSettings().isSetGracePeriod()) {
            return settings.getReferenceSettings().getAuditTrailServiceSettings().getGracePeriod().intValue();
        } else {
            return DEFAULT_GRACE_PERIOD;
        }
    }
    
    /**
     * Closes the AuditTrailCollector.
     */
    public void close() {
        for(AuditTrailCollectionTimerTask atctt : collectorTasks.values()) {
            atctt.cancel();
        }
        timer.cancel();
    }

}
