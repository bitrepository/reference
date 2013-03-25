package org.bitrepository.commandline.eventhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.resultmodel.GetFileIDsResultModel;

public class GetFileIDsEventHandler implements EventHandler {

    /** The amount of milliseconds before the results are required.*/
    private final Long timeout;
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<OperationEvent>(1);
    
    private List<String> pillarsWithPartialResults;
    private GetFileIDsResultModel model;
    
    public GetFileIDsEventHandler(GetFileIDsResultModel model, Long timeout) {
        this.model = model;
        this.timeout = timeout;
        pillarsWithPartialResults = new ArrayList<String>();
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getEventType() == OperationEventType.COMPLETE) {
            finalEventQueue.add(event);
        } else if(event.getEventType() == OperationEventType.FAILED) {
            finalEventQueue.add(event);
        } else {
            //foo
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
    
    public List<String> getPillarsWithPartialResults() {
        return pillarsWithPartialResults;
    }
}
