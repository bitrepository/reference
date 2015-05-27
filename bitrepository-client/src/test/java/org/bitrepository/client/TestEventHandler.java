/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.jaccept.TestEventManager;
import org.testng.Assert;

import static org.testng.Assert.assertNull;

/** Used to listen for operation event and store them for later retrieval by a test. */
public class TestEventHandler implements EventHandler {

    /** The <code>TestEventManager</code> used to manage the event for the associated test. */
    private final TestEventManager testEventManager;
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> eventQueue = new LinkedBlockingQueue<OperationEvent>();

    /** The default time to wait for events */
    private static final long DEFAULT_WAIT_SECONDS = 3;

    /** The constructor.
     * 
     * @param testEventManager The <code>TestEventManager</code> used to manage the event for the associated test.
     */
    public TestEventHandler(TestEventManager testEventManager) {
        super();
        this.testEventManager = testEventManager;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        testEventManager.addResult("Received event: "+ event);
        eventQueue.add(event);
    }

    /**
     * Wait for an event for the DEFAULT_WAIT_SECONDS amount of time.
     * @return The next event if any, else null 
     */
    public OperationEvent waitForEvent() throws InterruptedException {
        return waitForEvent(DEFAULT_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    public OperationEvent waitForEvent(long timeout, TimeUnit unit) throws InterruptedException {
        return eventQueue.poll(timeout, unit);
    }

    public void clearEvents() {
        eventQueue.clear();
    }

    public void verifyNoEventsAreReceived() throws InterruptedException {
        assertNull(waitForEvent(1, TimeUnit.SECONDS));
    }
}
