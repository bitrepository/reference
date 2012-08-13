package org.bitrepository.client.eventhandler;

import java.util.LinkedList;
import java.util.List;

/**
 * Eventhandler wrapper enabling blocking behavior for clients. Can pass events on to a supplied eventhandler.
 */
public class BlockingEventHandler implements EventHandler {
    private final EventHandler eventHandler;
    private OperationEvent finishEvent;
    private List<ContributorEvent> componentCompleteEvents = new LinkedList();
    private List<ContributorFailedEvent> componentFailedEvents = new LinkedList();
    private boolean operationFailed = false;

    public BlockingEventHandler() {
        eventHandler = null;
    }

    /**
     * @param eventHandler The eventhandler to pass event on to.
     */
    public BlockingEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public synchronized void handleEvent(OperationEvent event) {
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }

        if (event.getType() ==  OperationEvent.OperationEventType.COMPONENT_COMPLETE) {
            componentCompleteEvents.add((ContributorEvent)event);
        } else if (event.getType() == OperationEvent.OperationEventType.COMPONENT_FAILED) {
            componentFailedEvents.add((ContributorFailedEvent)event);
        } else if(event.getType() == OperationEvent.OperationEventType.COMPLETE ||
                event.getType() == OperationEvent.OperationEventType.FAILED    ) {
            finishEvent = event;
            this.notify();
        }
    }

    /**
     * Will block until a <code>COMPLETE</code> or <code>FAILED</code> event is received.
     */
    public synchronized OperationEvent awaitFinished() {
        try {
            this.wait();
        } catch (InterruptedException e) {
        }
        return finishEvent;
    }

    public boolean hasFailed() {
        return componentFailedEvents.size() > 0;
    }

    public List<ContributorFailedEvent> getFailures() {
        return componentFailedEvents;
    }
    public List<ContributorEvent> getResults() {
        return componentCompleteEvents;
    }
}
