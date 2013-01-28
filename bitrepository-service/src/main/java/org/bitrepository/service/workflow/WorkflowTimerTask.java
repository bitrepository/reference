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

import java.util.Date;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timer task encapsulating a workflow.
 * Used for scheduling workflows to run continuously at a given interval.
 */
public class WorkflowTimerTask extends TimerTask {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The date for the next run of the workflow.*/
    private Date nextRun;
    private Date lastRun;
    /** The name of the workflow.*/
    private final String name;
    /** The interval between triggers. */
    private final long interval;
    private final Workflow workflow;

    /**
     * Initialise trigger.
     * @param interval The interval between triggering events in milliseconds.
     * @param name The name of this workflow.
     */
    public WorkflowTimerTask(long interval, String name, Workflow workflow) {
        this.interval = interval;
        this.name = name;
        this.workflow = workflow;
        nextRun = new Date();
        lastRun = null;
    }

    /**
     * @return The date for the next time the encapsulated workflow should run.
     */
    public Date getNextRun() {
        return new Date(nextRun.getTime());
    }

    public Date getLastRun() {
        return lastRun;
    }

    /**
     * @return The interval between the runs in millis.
     */
    public long getIntervalBetweenRuns() {
        return interval;
    }

    /**
     * Trigger the workflow.
     * Resets the date for the next run of the workflow.
     */
    public void runWorkflow() {
        try {
            //FixMe Should be generalize to work with the general workflow interface.
            if (workflow.currentState().equals(StepBasedWorkflow.NOT_RUNNING)) {
                log.info("Starting the workflow: " + getName());
                nextRun = new Date(System.currentTimeMillis() + interval);
                workflow.start();
                lastRun = new Date();
            } else {
                log.warn("Ignoring start request for " + getName() + " the workflow is already running");
            }
        } catch (Throwable e) {
            log.error("Fault barrier for '" + getName() + "' caught unexpected exception.", e);
        }
    }

    /**
     * @return The name of the workflow.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The current state of the workflow.
     */
    public String currentState() {
        StringBuffer sb = new StringBuffer(workflow.currentState());
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            if(getNextRun().getTime() <= System.currentTimeMillis()) {
                runWorkflow();
            }
        } catch (Exception e) {
            log.error("Failed to run workflow", e);
        }
    }
}
