/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling outstanding files. This will handle the identification of a file.
 */
public class OutstandingFileID {
    /** The log.*/
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The file id. Contains both id of file and SLA.*/
    private final FileIdInstance fileId;
    /** The GetFileClient which is connected to this outstanding entry.*/
    private final GetFileClientExternalAPI client;
    /** The number of outstanding replies. */
    private int outstandingReplies;
    /** The time for each pillar to deliver this oustanding file. Maps between
     * pillar id and the time for the pillar to deliver.*/
    private Map<String, Long> pillarTime = Collections.synchronizedMap(new HashMap<String, Long>());
    
    /** Whether the timer is a deamon.*/
    private static final Boolean TIMER_IS_DEAMON = true;
    /** The timer. Is shared between all the instances of OutstandingFileID.*/
    private static Timer timer = new Timer(TIMER_IS_DEAMON);
    /** The timer task for this instance.*/
    private final TimerTask task;
    /** The amount of milliseconds to wait before executing the timertask. TODO replace with configuration.*/
    private static final Long TIMER_TASK_DELAY = 100000L;
    
    /**
     * Constructor. 
     * Creates and inserts a timer task for sending request to the fastest pillar after a given time, since it is not
     * certain to receive answer from all the pillars.
     * 
     * @param id The instance of FileId connected to be outstanding.
     * @param client The client which this instance is connected to. Is required for performing the request for 
     * retrieving the file from the fastest pillar.
     * @param expectedReplies The number of pillars expected to reply for the identification message.
     */
    public OutstandingFileID(FileIdInstance id, GetFileClientExternalAPI client, int expectedReplies) {
        this.fileId = id;
        this.client = client;
        this.outstandingReplies = expectedReplies;
        
        // add this as a task for the timer.
        task = new OutstandingTimerTask();
        timer.schedule(task, TIMER_TASK_DELAY);
    }
    
    /**
     * Inserts the response from a specific pillar for this file. If no more pillars are awaited, then make the request
     * to retrieve the file from the fastest pillar.
     * 
     * @param pillarId The id of the pillar.
     * @param deliveryTime The time to deliver the file for the pillar.
     */
    public void pillarReply(String pillarId, Long deliveryTime) {
        // validate if we have received a delivery time from this pillar.
        if(pillarTime.containsKey(pillarId)) {
            // If it is a identical delivery time for the pillar as already noted, then just stop.  
            if(deliveryTime == pillarTime.get(pillarId)) {
                log.debug("Received a identical delivery time for file '" + fileId + "' from pillar '" + pillarId 
                        + "' on '" + deliveryTime + "' milliseconds. Keeping old value.");
                return;
            } else {
                log.warn("Receive another delivery time for file '" + fileId + "' from pillar '" + pillarId + "'. "
                        + "Replacing old value '" + pillarTime.get(pillarId) + "' with the new value '" 
                        + deliveryTime + "'");
            }
        } else {
            outstandingReplies--;
        }
        pillarTime.put(pillarId, deliveryTime);
        
        if(outstandingReplies == 0) {
            // stop the timer task for this outstand instance, and then
            task.cancel();
            getFileFromFastest();
        }
    }
    
    /**
     * Method for making the client send a request for the file to the fastest pillar.
     */
    private void getFileFromFastest() {
        String pillarId = null;
        Long timeToDeliver = Long.MAX_VALUE;
        for(Map.Entry<String, Long> entry : pillarTime.entrySet()) {
            if(entry.getValue() < timeToDeliver) {
                pillarId = entry.getKey();
                timeToDeliver = entry.getValue();
            }
        }
        
        if(pillarId == null) {
            throw new AccessException("Cannot request entry for file '" + fileId + "' since the no pillar with a "
                    + "valid time has replied. Number of replies: '" + pillarTime.size() + "'");
        }
        
        // make the client perform the request for the file from the fastest pillar.
        client.getFile(fileId.getFileId(), fileId.getSlaId(), pillarId);
    }
    
    /**
     * The timer task class for the outstanding files. When the time is reached the fastest pillar should be called 
     * requested for the delivery of the file.
     */
    private class OutstandingTimerTask extends TimerTask {
        @Override
        public void run() {
            log.debug("Time has run out for identifying the fastest pillar for file '" + fileId + "'.");
            getFileFromFastest();
        }
    }
}
