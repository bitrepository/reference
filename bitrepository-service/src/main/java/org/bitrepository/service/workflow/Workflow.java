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
package org.bitrepository.service.workflow;


/**
 * Interface for defining a workflow.
 * A workflow performs a given task for the respective service. 
 */
public interface Workflow {
    /**
     * Start the workflow.
     */
    void start();
    
    /**
     * @return A human readable text telling the current state of the workflow.
     */
    String currentState();

    WorkflowStatistic getWorkflowStatistics();

    /**
     * @return Provides a human readable description of the workflow.
     */
    String getDescription();
    
    /**
     *  @return Provides an ID to identify the workflow on
     */
    String getWorkflowID();
}
