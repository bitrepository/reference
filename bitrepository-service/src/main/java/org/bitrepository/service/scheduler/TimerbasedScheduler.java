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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.JobTimerTask;
import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.service.workflow.WorkflowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler that uses Timer to run workflows.
 */
public class TimerbasedScheduler implements JobScheduler {
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The timer that schedules events. */
    private final Timer timer;
    /** The map between the running timertasks and their names. */
    private Map<JobID, JobTimerTask> intervalTasks = new HashMap<JobID, JobTimerTask>();
    public static final long SCHEDULE_INTERVAL = 60000;

    /** The name of the timer.*/
    private static final String TIMER_NAME = "Service Scheduler";
    /** Whether the timer is a daemon.*/
    private static final boolean TIMER_IS_DAEMON = true;
    /** A timer delay of 0 seconds.*/
    private static final Long NO_DELAY = 0L;
    private List<JobEventListener> jobListeners = new LinkedList<JobEventListener>();

    /** Setup a timer task for running the workflows at requested interval.
     */
    public TimerbasedScheduler() {
        timer = new Timer(TIMER_NAME, TIMER_IS_DAEMON);
    }

    @Override
    public void schedule(SchedulableJob workflow, Long interval) {
        log.info("Scheduling job : " + workflow.getJobID() + " to run every " 
                + TimeUtils.millisecondsToHuman(interval));
        
        JobTimerTask task = new JobTimerTask(interval, workflow, Collections.unmodifiableList(jobListeners));
        timer.scheduleAtFixedRate(task, NO_DELAY, SCHEDULE_INTERVAL);
        intervalTasks.put(workflow.getJobID(), task);
    }

    @Override
    public String startJob(SchedulableJob job) {
        if(job.currentState() != WorkflowState.NOT_RUNNING) {
            log.info("Cannot schedule job,'" + job.getJobID() + "', which is in state '" 
                    + job.currentState() + "'");
            return "Already running";
        }
        long timeBetweenRuns = -1;
        JobTimerTask oldTask = cancelJob(job.getJobID());
        if (oldTask != null) {
            timeBetweenRuns = oldTask.getIntervalBetweenRuns();
        }

        JobTimerTask task = new JobTimerTask(timeBetweenRuns, job, jobListeners);
        timer.scheduleAtFixedRate(task, NO_DELAY, SCHEDULE_INTERVAL);
        intervalTasks.put(job.getJobID(), task);
        return null;
    }

    @Override
    public Date getNextRun(JobID jobId) {
        if (intervalTasks.containsKey(jobId)) {
            return intervalTasks.get(jobId).getNextRun();
        } else return null;
    }

    @Override
    public long getRunInterval(JobID jobId) {
        if (intervalTasks.containsKey(jobId)) {
            return intervalTasks.get(jobId).getIntervalBetweenRuns();
        } else return -1;
    }

    @Override
    public void addJobEventListener(JobEventListener listener) {
        jobListeners.add(listener);
    }

    @Override
    public JobTimerTask cancelJob(JobID jobID) {
        JobTimerTask task = intervalTasks.remove(jobID);
        if(task == null) {
            return null;
        }
        task.cancel();

        return task;
    }
}
