/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
import java.util.TimerTask;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditTrailCollectionTimerTask extends TimerTask {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final IncrementalCollector collector;
    /** The collection schedule */
    private final CollectionSchedule schedule;
    private final Settings settings;

    /**
     * @param collector The collector doing the actual work.
     * @param interval The interval between running this timer task.
     * @param gracePeriod The period that should pass before the first scheduled collection
     * @param settings
     */
    public AuditTrailCollectionTimerTask(IncrementalCollector collector, long interval, int gracePeriod, Settings settings) {
        this.settings = settings;
        this.schedule = new CollectionSchedule(interval, gracePeriod);
        this.collector = collector;
        log.info("Scheduled next collection of audit trails for {}", schedule.getNextRun());
    }
    
    /**
     * @return the next scheduled collection
     */
    public Date getNextScheduledRun() {
        return schedule.getNextRun();
    }
    
    /**
     * @return the date of the last started collection
     */
    public Date getLastCollectionStart() {
        return schedule.getLastStart();
    }
    
    /**
     * @return  the date of the last finished collection
     */
    public Date getLastCollectionFinish() {
        return schedule.getLastFinish();
    }
    
    public long getLastNumberOfCollectedAudits() {
        return collector.getNumberOfCollectedAudits();
    }
    
    /**
     * Run the operation and when finished set the date for the next collection.
     */
    public synchronized void runCollection() {
        log.info("Starting collection of audit trails for collection: '{}'", collector.getCollectionID());
        schedule.start();
        collector.performCollection(SettingsUtils.getAuditContributorsForCollection(settings, collector.getCollectionID()));
        schedule.finish();
        log.info("Scheduled next collection of audit trails from {} for {}", collector.getCollectionID(), schedule.getNextRun());
    }

    @Override
    public void run() {
        if(schedule.getNextRun().getTime() < System.currentTimeMillis()) {
            runCollection();
        }
    }
}
