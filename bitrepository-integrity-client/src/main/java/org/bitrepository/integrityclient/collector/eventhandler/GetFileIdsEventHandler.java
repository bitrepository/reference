package org.bitrepository.integrityclient.collector.eventhandler;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the event from the GetFileIds operations and sends the results into the cache.
 */
public class GetFileIdsEventHandler implements EventHandler {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The storage to send the results.*/
    private CachedIntegrityInformationStorage informationCache;
    
    /**
     * 
     * @param informationCache
     */
    public GetFileIdsEventHandler(CachedIntegrityInformationStorage informationCache) {
        this.informationCache = informationCache;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType().equals(OperationEventType.Failed)) {
            handleFailure(event);
            return;
        }
        
        if(event.getType().equals(OperationEventType.Complete)) {
            handleComplete(event);
            return;
        }
        
        if(event instanceof FileIDsCompletePillarEvent) {
            handleFileIDsComplete((FileIDsCompletePillarEvent) event);
        } else {
            // TODO handle differently if special case (e.g. PillarFailure).
            log.debug(event.toString());
        }
    }
    
    /**
     * Handles the results of a operation.
     * @param event The event for the completed pillar.
     */
    private void handleFileIDsComplete(FileIDsCompletePillarEvent event) {
        informationCache.addFileIDs(event.getFileIDs().getFileIDsData(), event.getState()); 
    }
    
    /**
     * Method for handling a failure.
     * @param event The event that failed.
     */
    private void handleFailure(OperationEvent event) {
        // TODO implement
    }
    
    /**
     * Method for handling a complete. Thus notifying the relevant instance.
     * @param event The event that has completed.
     */
    private void handleComplete(OperationEvent event) {
       // TODO implement.
    }
}
