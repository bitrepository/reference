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
