package org.bitrepository.clienttest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.jaccept.TestEventManager;

public class TestEventHandler implements EventHandler {

	private final TestEventManager testEventManager;
	private final BlockingQueue<OperationEvent<?>> eventQueue = new LinkedBlockingQueue<OperationEvent<?>>();

	/** The default time to wait for events */
	private static final long DEFAULT_WAIT_SECONDS = 3;  

	public TestEventHandler(TestEventManager testEventManager) {
		super();
		this.testEventManager = testEventManager;
	}

	@Override
	public void handleEvent(OperationEvent event) {
		eventQueue.add(event);
	}

	/**
	 * Wait for an event for the DEFAULT_WAIT_SECONDS amaount of time.
	 * @return The next event if any, else null 
	 */
	public OperationEvent<?> waitForEvent() throws InterruptedException {
		return waitForEvent(DEFAULT_WAIT_SECONDS, TimeUnit.SECONDS);
	}

	public OperationEvent<?> waitForEvent(long timeout, TimeUnit unit) throws InterruptedException {
		return eventQueue.poll(timeout, unit);
	}
}
