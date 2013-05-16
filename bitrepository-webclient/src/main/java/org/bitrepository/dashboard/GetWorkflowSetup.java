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
