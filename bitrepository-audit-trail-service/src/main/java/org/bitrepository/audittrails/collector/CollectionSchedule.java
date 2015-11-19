/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.audittrails.collector;

import java.util.Date;

/**
 * Class to handle the information about previous, current and future 
 * timing and schedule of Audittrail collection 
 * Next run is at first scheduled as the interval after the current collection. 
 * When a collection has been finished, the next run is updated so the 
 * interval is after a collection has finished.
 */
public class CollectionSchedule {

    private Date nextRun = null;
    private Date lastStart = null;
    private Date lastFinish = null;
    private Date currentStart = null;
    private final long schedulingInterval;
    
    /**
     * Constructor to create the CollectionSchedule. 
     * @param schedulingInterval The interval at which to schedule collection
     * @param gracePeriod The grace period to wait before the first scheduling the first collection 
     */
    public CollectionSchedule(long schedulingInterval, int gracePeriod) {
        this.schedulingInterval = schedulingInterval;
        nextRun = new Date(System.currentTimeMillis() + gracePeriod);
    }

    /**
     * Get the date of the next scheduled collection 
     */
    public Date getNextRun() {
        return nextRun;
    }
    
    /**
     * Get the date of the last finished collection, or the current collection if non has finished yet. 
     * May return null, if the first collection has not yet been started.  
     */
    public Date getLastStart() {
        return lastStart;
    }
    
    /**
     * Get the date of the last finished collection. Returns null if no collection has finished yet. 
     */
    public Date getLastFinish() {
        return lastFinish;
    }
    
    /**
     * Get the date of the currently running collection. Returns null, if no collection is currently running. 
     */
    public Date getCurrentStart() {
        return currentStart;
    }
    
    /**
     * Indicate that a collection has been started.
     * Updates the next scheduled collection.
     */
    public void start() {
        currentStart = new Date(System.currentTimeMillis());
        if(lastStart == null) {
            lastStart = currentStart;
        }
        nextRun = new Date(currentStart.getTime() + schedulingInterval);
    }
    
    /**
     * Indicate that a collection has finished. 
     * Updates the next scheduled collection.
     */
    public void finish() {
        lastFinish = new Date(System.currentTimeMillis());
        lastStart = currentStart;
        currentStart = null;
        nextRun = new Date(lastFinish.getTime() + schedulingInterval);
    }
    
}
