/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.dashboard;

import javax.xml.bind.annotation.XmlRootElement;

/*
 * This class should be defined in org.bitrepository.common.webobjects for reuse just like the integrity objects
 * 
 */
@XmlRootElement
public class GetWorkflowSetup {

	private String workflowID;
	private String workflowDescription;
	private String nextRun;
	private String currentState;
	private String lastRun;
	private String executionInterval;
	private String lastRunDetails;
	
	public String getWorkflowID() {
		return workflowID;
	}
	public void setWorkflowID(String workflowID) {
		this.workflowID = workflowID;
	}
	public String getWorkflowDescription() {
		return workflowDescription;
	}
	public void setWorkflowDescription(String workflowDescription) {
		this.workflowDescription = workflowDescription;
	}
	public String getNextRun() {
		return nextRun;
	}
	public void setNextRun(String nextRun) {
		this.nextRun = nextRun;
	}
	public String getCurrentState() {
		return currentState;
	}
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}
	public String getLastRun() {
		return lastRun;
	}
	public void setLastRun(String lastRun) {
		this.lastRun = lastRun;
	}
	public String getExecutionInterval() {
		return executionInterval;
	}
	public void setExecutionInterval(String executionInterval) {
		this.executionInterval = executionInterval;
	}
	public String getLastRunDetails() {
		return lastRunDetails;
	}
	public void setLastRunDetails(String lastRunDetails) {
		this.lastRunDetails = lastRunDetails;
	}
	
}
