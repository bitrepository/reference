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

import java.time.Instant;
import java.util.Date;

/**
 * Class to handle the information about previous, current and future
 * timing and schedule of a timed task - e.g. audit-trail-service's AuditTrailCollectionTimerTask.
 * Next run is at first scheduled as the interval after the current run of the task.
 * When a task has been finished, the next run is updated so the interval is after a task has finished.
 */
public class TimerTaskSchedule {
    private Instant nextRun;
    private Instant lastStart = null;
    private Instant lastFinish = null;
    private Instant currentStart = null;
    private final long schedulingInterval;

    /**
     * Constructor to create the TaskSchedule.
     *
     * @param schedulingInterval The interval at which to schedule a new run of the task.
     * @param gracePeriod        The grace period to wait before the first scheduling.
     */
    public TimerTaskSchedule(long schedulingInterval, int gracePeriod) {
        this.schedulingInterval = schedulingInterval;
        nextRun = Instant.now().plusMillis(gracePeriod);
    }

    /**
     * @return The date of the next scheduled task.
     * @deprecated Use {@link #getNextRunInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getNextRun() {
        return nextRun != null ? Date.from(nextRun) : null;
    }

    public Instant getNextRunInstant() {
        return nextRun;
    }

    /**
     * @return The date of the last finished task, or the current run if none have finished yet.
     * May return null, if the first run has not yet been started.
     * @deprecated Use {@link #getLastStartInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getLastStart() {
        return lastStart != null ? Date.from(lastStart) : null;
    }

    public Instant getLastStartInstant() {
        return lastStart;
    }

    /**
     * @return The date of the last finished task. Returns null if no run has finished yet.
     * @deprecated Use {@link #getLastFinishInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getLastFinish() {
        return lastFinish != null ? Date.from(lastFinish) : null;
    }

    public Instant getLastFinishInstant() {
        return lastFinish;
    }

    /**
     * @return The date of the currently running task. Returns null, if no task is currently running.
     * @deprecated Use {@link #getCurrentStartInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getCurrentStart() {
        return currentStart != null ? Date.from(currentStart) : null;
    }

    public Instant getCurrentStartInstant() {
        return currentStart;
    }

    /**
     * Indicate that a task has been started.
     * Updates the next scheduled run of the task.
     */
    public void start() {
        currentStart = Instant.now();
        if (lastStart == null) {
            lastStart = currentStart;
        }
        nextRun = currentStart.plusMillis(schedulingInterval);
    }

    /**
     * Indicate that a task has finished.
     * Updates the next scheduled run.
     */
    public void finish() {
        lastFinish = Instant.now();
        lastStart = currentStart;
        currentStart = null;
        nextRun = lastFinish.plusMillis(schedulingInterval);
    }

}
