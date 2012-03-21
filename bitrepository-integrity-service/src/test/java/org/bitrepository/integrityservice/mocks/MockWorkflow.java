package org.bitrepository.integrityservice.mocks;

import java.util.Date;

import org.bitrepository.integrityservice.workflow.scheduler.IntervalWorkflow;

/**
 * A trigger that triggers every other second, and remembers calls.
 */
public class MockWorkflow extends IntervalWorkflow {
    /**
     * Get the number of times isTriggered() is called.
     * @return The number of times triggered() is called.
     */
    public int getNextRunCount() {
        return getNextRunCount;
    }

    /**
     * Get the number of times trigger() is called.
     * @return The number of times trigger() is called.
     */
    public int getWorkflowCalled() {
        return runWorkflowCount;
    }

    /** Number of times isTriggered() is called. */
    private int getNextRunCount = 0;
    /** Number of times trigger() is called. */
    private int runWorkflowCount = 0;

    @Override
    public Date getNextRun() {
        getNextRunCount++;
        return super.getNextRun();
    }
    
    /**
     * Initialise trigger.
     */
    public MockWorkflow(Long interval, String name) {
        super(interval, name);
    }

    @Override
    public void runWorkflow() {
        runWorkflowCount++;
    }
}
