/*
 * #%L
 * Bitrepository Service
 * 
 * $Id$
 * $HeadURL$
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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * States for a workflow.
 */
@XmlRootElement
public enum WorkflowState {
    /** When the workflow is not running.*/
    NOT_RUNNING("Not running"),
    /** When the workflow is waiting to be run.*/
    WAITING("Waiting"),
    /** When the workflow is running.*/
    RUNNING("Running"),
    /** When an execution of the workflow has been aborted */
    ABORTED("Aborted"),
    /** When the workflow have finished */
    SUCCEEDED("Succeeded");
    
    
    WorkflowState(String humanName) {
        this.humanName = humanName;
    }
    
    private String humanName;
    
    @Override 
    public String toString() {
        return humanName;
    }
}
