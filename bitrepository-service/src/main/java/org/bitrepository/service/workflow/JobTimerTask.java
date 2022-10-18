/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.workflow;

import org.bitrepository.service.scheduler.JobEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/**
 * A timer task encapsulating a workflow.
 * Used for scheduling workflows to run continuously at a given interval.
 */
public class JobTimerTask extends TimerTask {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Date nextRun;
    private final long interval;
    private final SchedulableJob job;
    private final List<JobEventListener> jobListeners;

    /**
     * Initialise trigger.
     *
     * @param interval     The interval between triggering events in milliseconds.
     * @param job          The job.
     * @param jobListeners event listeners for this job.
     */
    public JobTimerTask(long interval, SchedulableJob job, List<JobEventListener> jobListeners) {
        this.interval = interval;
        this.job = job;
        nextRun = new Date();
        this.jobListeners = jobListeners;
    }

    /**
     * @return The date for the next time the encapsulated workflow should run.
     */
    public Date getNextRun() {
        if (interval > 0) {
            return new Date(nextRun.getTime());
        } else {
            return null;
        }
    }

    /**
     * @return The interval between the runs in millis.
     */
    public long getIntervalBetweenRuns() {
        return interval;
    }

    public String getDescription() {
        return job.getDescription();
    }

    /**
     * Runs the job.
     * Resets the date for the next run of the job.
     */
    public void runJob() {
        try {
            if (job.currentState().equals(WorkflowState.NOT_RUNNING)) {
                log.info("Starting job: {}", job.getJobID());
                job.setCurrentState(WorkflowState.WAITING);
                job.start();
                if (interval > 0) {
                    nextRun = new Date(System.currentTimeMillis() + interval);
                }
                notifyListenersAboutFinishedJob(job);
            } else {
                log.info("Ignoring start request for job '{}'. The job is already running", job.getJobID());
            }
        } catch (Throwable e) {
            log.error("Fault barrier for '{}' caught unexpected exception.", job.getJobID(), e);
        }
    }

    private void notifyListenersAboutFinishedJob(SchedulableJob job) {
        for (JobEventListener listener : jobListeners) {
            listener.jobFinished(job);
        }
    }

    /**
     * @return The name of the job.
     */
    public String getName() {
        return job.getJobID().toString();
    }

    public JobID getWorkflowID() {
        return job.getJobID();
    }

    @Override
    public void run() {
        if (nextRun != null && (getNextRun() == null ||
                getNextRun().getTime() <= System.currentTimeMillis())) {
            runJob();
        }
    }
}
