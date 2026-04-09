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

import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * Container for the extraction of data from the alarm database.
 */
public class AlarmDatabaseExtractionModel {

    /**
     * @see #getComponentId().
     */
    private String componentId;

    /**
     * @see #getAlarmCode().
     */
    private AlarmCode alarmCode;

    /**
     * @see #getStartDateInstant() ().
     */
    private Instant startDate;

    /**
     * @see #getEndDateInstant() ().
     */
    private Instant endDate;

    /**
     * @see #getFileID(). .
     */
    private String fileID;

    /**
     * @see #getMaxCount().
     */
    private int maxCount;

    /**
     * @see #getAscending().
     */
    private boolean ascending;

    /**
     * @see #getCollectionID().
     */
    private String collectionID;

    /**
     * Constructor.
     *
     * @param collectionID The id of the collection, may be null.
     * @param componentId  The id of the component.
     * @param alarmCode    The alarm code.
     * @param startDate    The earliest date to restrict the extraction.
     * @param endDate      The latest date to restrict the extraction.
     * @param fileID       The id of the file.
     * @param maxCount     The maximum count of alarms to extract. If null, then set to maximum value for Integer.
     * @param ascending    if true sorted in ascending order, otherwise descending
     */
    public AlarmDatabaseExtractionModel(String collectionID, String componentId, AlarmCode alarmCode, Instant startDate, Instant endDate,
                                        String fileID, Integer maxCount, boolean ascending) {
        this.collectionID = collectionID;
        this.componentId = componentId;
        this.alarmCode = alarmCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileID = fileID;
        this.ascending = ascending;

        this.maxCount = Objects.requireNonNullElse(maxCount, Integer.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param collectionID The id of the collection, may be null.
     * @param componentId  The id of the component.
     * @param alarmCode    The alarm code.
     * @param startDate    The earliest date to restrict the extraction.
     * @param endDate      The latest date to restrict the extraction.
     * @param fileID       The id of the file.
     * @param maxCount     The maximum count of alarms to extract. If null, then set to maximum value for Integer.
     * @param ascending    if true sorted in ascending order, otherwise descending
     * @deprecated Use {@link #AlarmDatabaseExtractionModel(String, String, AlarmCode, Instant, Instant, String, Integer, boolean)} instead
     */
    @Deprecated(forRemoval = true)
    public AlarmDatabaseExtractionModel(String collectionID, String componentId, AlarmCode alarmCode, Date startDate, Date endDate,
                                        String fileID, Integer maxCount, boolean ascending) {
        this(collectionID, componentId, alarmCode,
                startDate != null ? startDate.toInstant() : null,
                endDate != null ? endDate.toInstant() : null,
                fileID, maxCount, ascending);
    }

    /**
     * Returns the id to identify this component.
     *
     * @return The componentId
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Sets the id to identify this component.
     *
     * @param componentId The new component id.
     * @see #getComponentId()
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * Returns the alarm code.
     *
     * @return The alarmCode
     */
    public AlarmCode getAlarmCode() {
        return alarmCode;
    }

    /**
     * @param alarmCode The new alarm code.
     * @see #getAlarmCode()
     */
    public void setAlarmCode(AlarmCode alarmCode) {
        this.alarmCode = alarmCode;
    }

    /**
     * Return the starting date for this alarm.
     *
     * @return The startDate;
     */
    public Instant getStartDateInstant() {
        return startDate;
    }

    /**
     * Return the starting date for this alarm.
     *
     * @return The startDate;
     * @deprecated Use {@link #getStartDateInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getStartDate() {
        return startDate != null ? Date.from(startDate) : null;
    }

    /**
     * @param startDate The startDate.
     * @see #getStartDateInstant() ()
     */
    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    /**
     * @param startDate The startDate.
     * @see #getStartDate()
     * @deprecated Use {@link #setStartDate(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? startDate.toInstant() : null;
    }

    /**
     * Returns the end date for the alarm.
     *
     * @return The endDate;
     */
    public Instant getEndDateInstant() {
        return endDate;
    }

    /**
     * Returns the end date for the alarm.
     *
     * @return The endDate;
     * @deprecated Use {@link #getEndDateInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getEndDate() {
        return endDate != null ? Date.from(endDate) : null;
    }

    /**
     * Sets the end date for the alarm.
     *
     * @param endDate The endDate.
     * @see #getEndDateInstant ()
     */
    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    /**
     * Sets the end date for the alarm.
     *
     * @param endDate The endDate.
     * @see #getEndDate()
     * @deprecated Use {@link #setEndDate(Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? endDate.toInstant() : null;
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
     * @param fileID The new file id.
     * @see #getFileID()
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    /**
     * Return the maximum count possible.
     *
     * @return The maxCount;
     */
    public Integer getMaxCount() {
        return maxCount;
    }

    /**
     * @param maxCount The new max count.
     * @see #getMaxCount()
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
     * @param ascending Whether the results should be ascending (or alternatively descending).
     * @see #getAscending()
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
     * @param collectionID The ID of the collection.
     * @see #getCollectionID()
     */
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }
}
