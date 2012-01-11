package org.bitrepository.access.audittrails.client;

import java.util.List;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.protocol.client.BitrepositoryClient;
import org.bitrepository.protocol.eventhandler.EventHandler;

/**
 * Provides functionality for retrieving the list of available audit trail contributors at a given time.
 */
public interface AuditTrailIdentificator extends BitrepositoryClient {
    /**
     * Returns the contributors currently available. A identify contributors request is used to lookup the contributors 
     * each time this method is called.
     */
    void getAvailableContributors(EventHandler eventHandler, String auditTrailInformation);
    
    /**
     * @return Returns the list of audit trail contributors defined for this collection.
     */
    List<String> getDefinedContributors();
}
