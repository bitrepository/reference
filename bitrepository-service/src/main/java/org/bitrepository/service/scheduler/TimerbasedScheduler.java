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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.JobTimerTask;
import org.bitrepository.service.workflow.SchedulableJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler that uses Timer to run workflows.
 */
public class TimerbasedScheduler implements JobScheduler {
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The timer that schedules events. */
    private final Timer timer;
    /** The period between testing whether triggers have triggered. */
    private final long schedulerInterval;
    /** The map between the running timertasks and their names.*/
    private Map<JobID, JobTimerTask> intervalTasks = new HashMap<JobID, JobTimerTask>();
    public static final long DEFAULT_SCHEDULER_INTERVAL = 86400000;

    /** The name of the timer.*/
    private static final String TIMER_NAME = "Service Scheduler";
    /** Whether the timer is a deamon.*/
    private static final boolean TIMER_IS_DEAMON = true;
    /** A timer delay of 0 seconds.*/
    private static final Long NO_DELAY = 0L;

    /** Setup a timer task for running the workflows at requested interval.
     *
     * @param interval The interval for checking if workflows should be run.
     */
    public TimerbasedScheduler(long interval) {
        if (interval == -1) {
            schedulerInterval = DEFAULT_SCHEDULER_INTERVAL;
        } else {
            schedulerInterval = interval;
        }
        timer = new Timer(TIMER_NAME, TIMER_IS_DEAMON);
    }

    @Override
    public void schedule(SchedulableJob workflow, Long interval) {
        log.info("Scheduling workflow : " + workflow);

        JobTimerTask task = new JobTimerTask(interval, workflow);
        timer.scheduleAtFixedRate(task, NO_DELAY, schedulerInterval);
        intervalTasks.put(workflow.getJobID(), task);
    }

    @Override
    public String startJob(SchedulableJob workflow) {
        long timeBetweenRuns = -1;
        JobTimerTask oldTask = cancelJob(workflow.getJobID());
        if (oldTask != null) {
            timeBetweenRuns = oldTask.getIntervalBetweenRuns();
        }

        JobTimerTask task = new JobTimerTask(timeBetweenRuns, workflow);
        timer.scheduleAtFixedRate(task, NO_DELAY, schedulerInterval);
        intervalTasks.put(workflow.getJobID(), task);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
    
    @Override
    public List<JobTimerTask> getJobs(String collectionID) {
        List<JobTimerTask> workflows = new ArrayList<JobTimerTask>();
        for(JobTimerTask task : intervalTasks.values()) {
            if(task.getWorkflowID().getCollectionID().equals(collectionID)) {
                workflows.add(task);
            }
        }
        return workflows;
    }
}
