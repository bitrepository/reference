package org.bitrepository.client.eventhandler;
/*
 * #%L
 * Bitrepository Client
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
