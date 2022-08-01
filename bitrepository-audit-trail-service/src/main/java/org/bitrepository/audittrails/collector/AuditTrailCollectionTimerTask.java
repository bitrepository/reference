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

import org.bitrepository.common.TimerTaskSchedule;
import org.bitrepository.common.utils.SettingsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;

public class AuditTrailCollectionTimerTask extends TimerTask {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final IncrementalCollector collector;
    private final TimerTaskSchedule schedule;

    /**
     * @param collector   The collector doing the actual work.
     * @param interval    The interval between running this timer task.
     * @param gracePeriod The period that should pass before the first scheduled collection
     */
    public AuditTrailCollectionTimerTask(IncrementalCollector collector, long interval, int gracePeriod) {
        this.schedule = new TimerTaskSchedule(interval, gracePeriod);
        this.collector = collector;
        log.info("Scheduled next collection of audit trails for {}", schedule.getNextRun());
    }

    public Date getNextScheduledRun() {
        return schedule.getNextRun();
    }

    public Date getLastCollectionStart() {
        return schedule.getLastStart();
    }

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
        collector.performCollection(SettingsUtils.getAuditContributorsForCollection(collector.getCollectionID()));
        schedule.finish();
        log.info("Scheduled next collection of audit trails from {} for {}", collector.getCollectionID(), schedule.getNextRun());
    }

    @Override
    public void run() {
        if (schedule.getNextRun().getTime() < System.currentTimeMillis()) {
            runCollection();
        }
    }
}
