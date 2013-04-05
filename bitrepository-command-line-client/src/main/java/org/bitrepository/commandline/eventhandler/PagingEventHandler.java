package org.bitrepository.commandline.eventhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.output.OutputHandler;

/**
 * Event handler for operations that need paging functionality. 
 */
public abstract class PagingEventHandler implements EventHandler {

    /** The amount of milliseconds before the results are required.*/
    private final Long timeout;
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<OperationEvent>(1);
    
    protected List<String> pillarsWithPartialResults = new ArrayList<String>();
    
    private final OutputHandler outputHandler;

    public PagingEventHandler(Long timeout, OutputHandler outputHandler) {
        this.timeout = timeout;
        this.outputHandler = outputHandler;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getEventType() == OperationEventType.COMPONENT_COMPLETE) {
            handleResult(event);
        } else if(event.getEventType() == OperationEventType.COMPLETE) {
            finalEventQueue.add(event);
        } else if(event.getEventType() == OperationEventType.FAILED) {
            finalEventQueue.add(event);
        } else {
            outputHandler.debug("Received event: " + event.toString());
        }
    }
    
    /**
     * Retrieves the final event when the operation finishes. The final event is awaited for 'timeout' amount 
     * of milliseconds. If no final events has occurred, then an IllegalStateException is thrown.
     * @return The final event.
     */
    public OperationEvent getFinish() {
        try {
            return finalEventQueue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for the final response.", e);
        }
    }
    
    /**
     * Gets the list of pillars who's latest result was partial, i.e. the pillars that needs to deliver more results
     * @return List<String>, the list of pillarIDs.  
     */
    public List<String> getPillarsWithPartialResults() {
        return pillarsWithPartialResults;
    }
    
    protected abstract void handleResult(OperationEvent event);
}
