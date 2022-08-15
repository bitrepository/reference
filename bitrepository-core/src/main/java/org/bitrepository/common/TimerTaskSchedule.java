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
package org.bitrepository.common;

import java.util.Date;

/**
 * Class to handle the information about previous, current and future
 * timing and schedule of a timed task - e.g. audit-trail-service's AuditTrailCollectionTimerTask.
 * Next run is at first scheduled as the interval after the current run of the task.
 * When a task has been finished, the next run is updated so the interval is after a task has finished.
 */
public class TimerTaskSchedule {
    private Date nextRun;
    private Date lastStart = null;
    private Date lastFinish = null;
    private Date currentStart = null;
    private final long schedulingInterval;

    /**
     * Constructor to create the TaskSchedule.
     *
     * @param schedulingInterval The interval at which to schedule a new run of the task.
     * @param gracePeriod        The grace period to wait before the first scheduling.
     */
    public TimerTaskSchedule(long schedulingInterval, int gracePeriod) {
        this.schedulingInterval = schedulingInterval;
        nextRun = new Date(System.currentTimeMillis() + gracePeriod);
    }

    /**
     * @return The date of the next scheduled task.
     */
    public Date getNextRun() {
        return nextRun;
    }

    /**
     * @return The date of the last finished task, or the current run if none have finished yet.
     * May return null, if the first run has not yet been started.
     */
    public Date getLastStart() {
        return lastStart;
    }

    /**
     * @return The date of the last finished task. Returns null if no run has finished yet.
     */
    public Date getLastFinish() {
        return lastFinish;
    }

    /**
     * @return The date of the currently running task. Returns null, if no task is currently running.
     */
    public Date getCurrentStart() {
        return currentStart;
    }

    /**
     * Indicate that a task has been started.
     * Updates the next scheduled run of the task.
     */
    public void start() {
        currentStart = new Date(System.currentTimeMillis());
        if (lastStart == null) {
            lastStart = currentStart;
        }
        nextRun = new Date(currentStart.getTime() + schedulingInterval);
    }

    /**
     * Indicate that a task has finished.
     * Updates the next scheduled run.
     */
    public void finish() {
        lastFinish = new Date(System.currentTimeMillis());
        lastStart = currentStart;
        currentStart = null;
        nextRun = new Date(lastFinish.getTime() + schedulingInterval);
    }

}
