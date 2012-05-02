/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.store;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.AlarmCode;

/**
 * Container for the extraction of data from the alarm database.
 */
public class AlarmDatabaseExtractionModel {
    /** @see getFileId(). */
    private String componentId;
    /** @see getContributorId(). */
    private AlarmCode alarmCode;
    /** @see getStartDate(). */
    private Date startDate;
    /** @see getEndDate(). */
    private Date endDate;
    /** @see getActorName(). */
    private String fileID;
    /** @see getOperation(). */
    private int maxCount;
    
    /**
     * Constructor.
     * @param componentId The id of the component.
     * @param alarmCode The alarm code.
     * @param startDate The earliest date to restrict the extraction.
     * @param endDate The latest date to restrict the extraction.
     * @param fileID The id of the file.
     * @param maxCount The maximum count of alarms to extract. If null, then set to maximum value for Integer.
     */
    public AlarmDatabaseExtractionModel(String componentId, AlarmCode alarmCode, Date startDate, Date endDate, 
            String fileID, Integer maxCount) {
        this.componentId = componentId;
        this.alarmCode = alarmCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileID = fileID;
        
        if(maxCount != null) {
            this.maxCount = maxCount;
        } else {
            this.maxCount = Integer.MAX_VALUE;
        }
    }
    
    /**
     * @return The componentId;
     */
    public String getComponentId() {
        return componentId;
    }
    
    /**
     * @See getComponentId();
     * @param componentId The new component id.
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    
    /**
     * @return The alarmCode;
     */
    public AlarmCode getAlarmCode() {
        return alarmCode;
    }
    
    /**
     * @See getAlarmCode();
     * @param alarmCode The new alarm code.
     */
    public void setAlarmCode(AlarmCode alarmCode) {
        this.alarmCode = alarmCode;
    }
   
    /**
     * @return The startDate;
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * @See getStartDate();
     * @param startDate The startDate.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * @return The endDate;
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * @See getEndDate();
     * @param endDate The endDate.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }    
    
    /**
     * @return The fileID;
     */
    public String getFileID() {
        return fileID;
    }
    
    /**
     * @See getFileID();
     * @param fileID The new file id.
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }
    
    /**
     * @return The maxCount;
     */
    public Integer getMaxCount() {
        return maxCount;
    }
    
    /**
     * @See getMaxCount();
     * @param maxCount The new max count.
     */
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }    
}
