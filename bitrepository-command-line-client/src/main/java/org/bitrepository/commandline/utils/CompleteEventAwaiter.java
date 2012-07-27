/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EventHandler for awaiting an operation to be complete.
 * Just use the 'getFinish()' for awaiting the final event (either FAILURE or COMPLETE).
 */
public class CompleteEventAwaiter implements EventHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The amount of milliseconds before the results are required.*/
    private final Long timeout;
    
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<OperationEvent>(1);

    /**
     * Constructor.
     * @param settings The settings.
     */
    public CompleteEventAwaiter(Settings settings) {
        this.timeout = settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue() 
                + settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType() == OperationEventType.COMPLETE) {
            log.debug("Complete: " + event.toString());
            finalEventQueue.add(event);
        } else if(event.getType() == OperationEventType.FAILED) {
            log.warn("Failure: " + event.toString());
            finalEventQueue.add(event);
        } else {
            log.debug("Received event: " + event.toString());
        }
    }
    
    /**
     * Retrieves the final event when the operation finishes. The final event is awaited for 'timeout' amount 
     * of milliseconds. If no final events has occurred, then an InterruptedException is thrown.
     * @return The final event.
     */
    public OperationEvent getFinish() {
        try {
            return finalEventQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for the final response.", e);
        }
    }
}
