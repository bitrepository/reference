/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.mocks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowTimerTask;

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
