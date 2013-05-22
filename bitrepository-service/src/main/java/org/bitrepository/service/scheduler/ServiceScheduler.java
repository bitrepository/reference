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

import java.util.Collection;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowID;
import org.bitrepository.service.workflow.WorkflowTimerTask;

/**
 * Interface for scheduling workflows for the services.
 */
public interface ServiceScheduler {
    /**
     * Adds a workflow for the scheduler to schedule. 
     * @param workflow The workflow to schedule.
     * @param interval The interval for how often the workflow should be triggered.
     */
    void scheduleWorkflow(Workflow workflow, Long interval);
    
    /**
     * Cancels the workflow with the given name.
     * 
     * @param workflowId The ID of the workflow to cancel.
     * @return Whether the workflow was successfully found and removed. 
     */
    boolean cancelWorkflow(WorkflowID workflowId);
    
    /**
     * @return The list of all workflows currently scheduled by the scheduler.
     */
    Collection<WorkflowTimerTask> getScheduledWorkflows();
}
