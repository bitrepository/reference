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
     * @return  the date of the next scheduled collection
     */
    public Date getNextRun() {
        return nextRun;
    }
    
    /**
     * @return  the date of the last finished collection, or the current collection if non has finished yet.
     * May return null, if the first collection has not yet been started.
     */
    public Date getLastStart() {
        return lastStart;
    }
    
    /**
     * @return  the date of the last finished collection. Returns null if no collection has finished yet.
     */
    public Date getLastFinish() {
        return lastFinish;
    }
    
    /**
     * @return  the date of the currently running collection. Returns null, if no collection is currently running.
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
