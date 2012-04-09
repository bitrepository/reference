package org.bitrepository.access.getstatus;

import org.bitrepository.protocol.eventhandler.EventHandler;

public interface GetStatusClient {
    
    /**
     * Method for retrieving statuses for all components in the system. 
     * 
     * The method will return as soon as the communication has been setup.
     *
     * @param eventHandler The handler which should receive notifications of the progress events. 
     */
    void getStatus(EventHandler eventHandler);
    
    /**
     * Method to perform a graceful shutdown of the client.
     */
    void shutdown();
}
