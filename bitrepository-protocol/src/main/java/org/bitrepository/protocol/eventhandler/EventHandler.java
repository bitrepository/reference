package org.bitrepository.protocol.eventhandler;

/**
 * Defines the interface for clients to follow the sequence of events unfolding after a operation on the bitrepository 
 * has been requested.
 * 
 * A user interested in following the status a of request should create an <code>EventHandler</code> and supply the 
 * EventHandler when call the relevant method on the client.
 */
public interface EventHandler {
    /**
     * Called by the client each time a event is detected relevant for the active operation
     * @param event Contains information about the concrete event.
     */
    void handleEvent(OperationEvent event);
}
