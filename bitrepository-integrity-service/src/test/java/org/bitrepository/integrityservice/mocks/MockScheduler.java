package org.bitrepository.integrityservice.mocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.scheduler.Workflow;
import org.bitrepository.service.scheduler.WorkflowTimerTask;

public class MockScheduler implements ServiceScheduler {
    
    Map<String, WorkflowTimerTask> workflows = new HashMap<String, WorkflowTimerTask>();
    
    private int callsForScheduleWorkflow = 0;
    @Override
    public void scheduleWorkflow(Workflow workflow, String workflowId, Long interval) {
        callsForScheduleWorkflow++;
        workflows.put(workflowId, new WorkflowTimerTask(interval, workflowId, workflow));
    }
    public int getCallsForScheduleWorkflow() {
        return callsForScheduleWorkflow;
    }
    
    private int callsForCancelWorkflow = 0;
    @Override
    public boolean cancelWorkflow(String workflowId) {
        callsForCancelWorkflow++;
        return workflows.remove(workflowId) != null;
    }
    public int getCallsForCancelWorkflow() {
        return callsForCancelWorkflow;
    }
    
    private int callsForGetScheduledWorkflows = 0;
    @Override
    public Collection<WorkflowTimerTask> getScheduledWorkflows() {
        callsForGetScheduledWorkflows++;
        return workflows.values();
    }
    public int getCallsForGetScheduledWorkflows() {
        return callsForGetScheduledWorkflows;
    }
}
