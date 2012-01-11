package org.bitrepository.protocol.client;

/**
 * Defines the general functionality of Bit Repository reference client classes.
 */
public interface BitrepositoryClient {
    /**
     * Method to perform a graceful shutdown of the client.
     */
    void shutdown();
}
