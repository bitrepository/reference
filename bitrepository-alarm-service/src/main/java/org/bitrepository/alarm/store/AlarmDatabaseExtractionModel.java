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

import org.bitrepository.bitrepositoryelements.AlarmCode;

import java.util.Date;

/**
 * Container for the extraction of data from the alarm database.
 */
public class AlarmDatabaseExtractionModel {

    /** @see #getComponentId().  */
    private String componentId;

    /** @see #getAlarmCode().  */
    private AlarmCode alarmCode;

    /** @see #getStartDate(). */
    private Date startDate;

    /** @see #getEndDate(). */
    private Date endDate;

    /** @see #getFileID(). . */
    private String fileID;

    /** @see #getMaxCount(). */
    private int maxCount;

    /** @see #getAscending().*/
    private boolean ascending;

    /** @see #getCollectionID(). */
    private String collectionID;
    
    /**
     * Constructor.
     * @param collectionID The id of the collection, may be null.
     * @param componentId The id of the component.
     * @param alarmCode The alarm code.
     * @param startDate The earliest date to restrict the extraction.
     * @param endDate The latest date to restrict the extraction.
     * @param fileID The id of the file.
     * @param maxCount The maximum count of alarms to extract. If null, then set to maximum value for Integer.
     * @param ascending if true sorted in ascending order, otherwise descending
     */
    public AlarmDatabaseExtractionModel(String collectionID, String componentId, AlarmCode alarmCode, Date startDate, Date endDate, 
            String fileID, Integer maxCount, boolean ascending) {
        this.collectionID = collectionID;
        this.componentId = componentId;
        this.alarmCode = alarmCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileID = fileID;
        this.ascending = ascending;
        
        if(maxCount != null) {
            this.maxCount = maxCount;
        } else {
            this.maxCount = Integer.MAX_VALUE;
        }
    }
    
    /** Returns the id to identify this component.
     * @return The componentId
     */
    public String getComponentId() {
        return componentId;
    }
    
    /**
     * Sets the id to identify this component.
     * @see #getComponentId()
     * @param componentId The new component id.
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    
    /**
     * Returns the alarm code.
     * @return The alarmCode
     */
    public AlarmCode getAlarmCode() {
        return alarmCode;
    }
    
    /**
     * @see #getAlarmCode()
     * @param alarmCode The new alarm code.
     */
    public void setAlarmCode(AlarmCode alarmCode) {
        this.alarmCode = alarmCode;
    }
   
    /**
     * Return the starting date for this alarm.
     * @return The startDate;
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * @see #getStartDate()
     * @param startDate The startDate.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * Returns the end date for the alarm.
     * @return The endDate;
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * Sets the end date for the alarm.
     * @see #getEndDate()
     * @param endDate The endDate.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }    
    
    /**
     * Returns the file id for this alarm.
     *
     * @return The fileID;
     */
    public String getFileID() {
        return fileID;
    }
    
    /**
     * @see #getFileID()
     * @param fileID The new file id.
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }
    
    /**
     * Return the maximum count possible.
     * @return The maxCount;
     */
    public Integer getMaxCount() {
        return maxCount;
    }
    
    /**
     * @see #getMaxCount()
     * @param maxCount The new max count.
     */
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }
    
    /**
     * @return Whether the results are delivered ascending (alternatively descending).
     */
    public boolean getAscending() {
        return ascending;
    }
    
    /**
     * @see #getAscending()
     * @param ascending Whether the results should be ascending (or alternatively descending).
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }   
    
    /**
     * @return The ID of the collection. 
     */
    public String getCollectionID() {
        return collectionID;
    }
    
    /**
     * @see #getCollectionID()
     * @param collectionID The ID of the collection.
     */
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }
}
