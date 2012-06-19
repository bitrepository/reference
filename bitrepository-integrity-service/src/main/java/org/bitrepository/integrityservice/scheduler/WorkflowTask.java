package org.bitrepository.integrityservice.scheduler;

import java.util.Date;

public interface WorkflowTask {
    public Date getNextRun();
    public long getTimeBetweenRuns();
    public void trigger();
    public String getName();
    public String currentState();
}
