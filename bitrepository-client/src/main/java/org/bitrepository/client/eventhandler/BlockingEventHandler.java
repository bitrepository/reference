package org.bitrepository.client.eventhandler;
/*
 * #%L
 * BitRepository Client
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
 * EventHandler wrapper enabling blocking behavior for clients. Can pass events on to a supplied EventHandler.
 */
public class BlockingEventHandler implements EventHandler {
    private final EventHandler eventHandler;
    private OperationEvent finishEvent;
    private final List<ContributorEvent> componentCompleteEvents = new LinkedList<>();
    private final List<ContributorFailedEvent> componentFailedEvents = new LinkedList<>();

    /**
     * @param eventHandler The EventHandler to pass event on to.
     */
    public BlockingEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public synchronized void handleEvent(OperationEvent event) {
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }

        if (event.getEventType() == OperationEvent.OperationEventType.COMPONENT_COMPLETE) {
            componentCompleteEvents.add((ContributorEvent) event);
        } else if (event.getEventType() == OperationEvent.OperationEventType.COMPONENT_FAILED) {
            componentFailedEvents.add((ContributorFailedEvent) event);
        } else if (event.getEventType() == OperationEvent.OperationEventType.COMPLETE ||
                event.getEventType() == OperationEvent.OperationEventType.FAILED) {
            finishEvent = event;
            this.notify();
        }
    }

    /**
     * Will block until a <code>COMPLETE</code> or <code>FAILED</code> event is received.
     *
     * @return the operationEvent
     */
    public synchronized OperationEvent awaitFinished() {
        try {
            this.wait();
        } catch (InterruptedException ignored) {
        }
        return finishEvent;
    }

    public List<ContributorFailedEvent> getFailures() {
        return componentFailedEvents;
    }

    public List<ContributorEvent> getResults() {
        return componentCompleteEvents;
    }
}
