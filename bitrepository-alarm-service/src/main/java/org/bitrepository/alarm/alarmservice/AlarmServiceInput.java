package org.bitrepository.alarm.alarmservice;

public class AlarmServiceInput {
	private String alarmCode;
	private String collectionID;
	private String fileID;
	private String fromDate;
	private String toDate;
    private String reportingComponent;
	private int maxAlarms;
	private boolean oldestAlarmsFirst;

	public AlarmServiceInput(){
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getCollectionID() {
		return collectionID;
	}

	public void setCollectionID(String collectionID) {
		this.collectionID = collectionID;
	}

	public String getFileID() {
		return fileID;
	}

	public void setFileID(String fileID) {
		this.fileID = fileID;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public int getMaxAlarms() {
		return maxAlarms;
	}

	public void setMaxAlarms(int maxAlarms) {
		this.maxAlarms = maxAlarms;
	}

	public boolean isOldestAlarmsFirst() {
		return oldestAlarmsFirst;
	}

	public void setOldestAlarmsFirst(boolean oldestAlarmsFirst) {
		this.oldestAlarmsFirst = oldestAlarmsFirst;
	}

	public String getReportingComponent() {
		return reportingComponent;
	}

	public void setReportingComponent(String reportingComponent) {
		this.reportingComponent = reportingComponent;
	}

}
