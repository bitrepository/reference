/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.alarmservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AlarmServiceInput {
    private String alarmCode;
    private String collectionID;
    private String fileID;
    private String fromDate;
    private String toDate;
    private String reportingComponent;
    private int maxAlarms;
    private boolean oldestAlarmsFirst;


    public AlarmServiceInput() {
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
