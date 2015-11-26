/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.service.scheduler;

import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.JobTimerTask;
import org.bitrepository.service.workflow.SchedulableJob;

import java.util.Date;

/**
 * Interface for scheduling jobs for services.
 */
public interface JobScheduler {
    /**
     * Adds a job for the scheduler to schedule.
     * @param job The job to schedule.
     * @param interval The interval for how often the job should be triggered.
     */
    void schedule(SchedulableJob job, Long interval);

    /**
     * Cancels the job with the given name.
     *
     * @param jobId The ID of the job to cancel
     * @return The canceled JobTimerTask.
     */
    JobTimerTask cancelJob(JobID jobId);


    /**
     * Reschedules the job to start now,
     * @param job the jobn to start
     * @return A string indicating the result of the attempt to start the job.
     */
    String startJob(SchedulableJob job);

    /**
     * @param jobId the indicated job
     * @return the date for the next run of the indicated job. Return null if the job isn't scheduled.
     */
    Date getNextRun(JobID jobId);

    /**
     * @param jobId the indicated job
     * @return the interval between runs for the indicated job. The interval is in milliseconds.
     */
    long getRunInterval(JobID jobId);

    /**
     * Enables other objects to listen for job events.
     * @param listener The callback listener to receive the events.
     */
    void addJobEventListener(JobEventListener listener);
}
