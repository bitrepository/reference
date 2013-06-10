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

import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.service.workflow.WorkflowID;
import org.bitrepository.service.workflow.WorkflowTimerTask;

import java.util.List;

/**
 * Interface for scheduling workflows for the services.
 */
public interface WorkflowScheduler {
    /**
     * Adds a workflow for the scheduler to schedule. 
     * @param workflow The workflow to schedule.
     * @param interval The interval for how often the workflow should be triggered.
     */
    void scheduleWorkflow(SchedulableJob workflow, Long interval);
    
    /**
     * Cancels the workflow with the given name.
     * 
     * @param workflowId The ID of the workflow to cancel.
     * @return The canceled WorkflowTimerTask.
     */
    WorkflowTimerTask cancelWorkflow(WorkflowID workflowId);
    
    /**
     * @return The list of workflows currently scheduled for the indicated collection.
     */
    List<WorkflowTimerTask> getWorkflows(String collectionID);

    /**
     * Reschedules the workflow to start now
     * @param workflow
     * @return A string indicating the result of the attempt to start the workflow.
     */
    String startWorkflow(SchedulableJob workflow);
}
