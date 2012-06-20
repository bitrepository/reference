package org.bitrepository.service.scheduler;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A timer task encapsulating a workflow.
 * Used for scheduling workflows to run continuously at a given interval.
 */
public class WorkflowTask extends TimerTask {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The date for the next run of the workflow.*/
    private Date nextRun;
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
    public WorkflowTask(long interval, String name, Workflow workflow) {
        this.interval = interval;
        this.name = name;
        this.workflow = workflow;
        nextRun = new Date();
    }
    
    /**
     * @return The date for the next time the encapsulated workflow should run.
     */
    public Date getNextRun() {
        return new Date(nextRun.getTime());
    }
    
    /**
     * @return The interval between the runs in millis.
     */
    public long getTimeBetweenRuns() {
        return interval;
    }
    
    /**
     * Trigger the workflow.
     * Resets the date for the next run of the workflow.
     */
    public void trigger() {
        log.info("Starting the workflow: " + getName());
        nextRun = new Date(System.currentTimeMillis() + interval);
        workflow.start();
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
        return workflow.currentState();
    }

    @Override
    public void run() {
        if(getNextRun().getTime() <= System.currentTimeMillis()) {
           trigger();
        }
    }
}
