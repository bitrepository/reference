package org.bitrepository.integrityservice.scheduler.workflow.step;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for collecting a single file.
 */
public class CollectChecksumsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The settings.*/
    private final Settings settings;
    /** The client for retrieving the checksums.*/
    private final GetChecksumsClient client;
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The id of the file to collect.*/
    private final String fileid;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param client The client for collecting the checksums.
     * @param store The storage for the integrity data.
     * @param fileid The id of the file to collect.
     */
    public CollectChecksumsStep(Settings settings, GetChecksumsClient client, IntegrityModel store, String fileid) {
        this.settings = settings;
        this.client = client;
        this.store = store;
        this.fileid = fileid;
    }
    
    @Override
    public String getName() {
        return "Collecting checksums for file '" + fileid + "'.";
    }

    @Override
    public synchronized void performStep() {
        log.debug("Begin collecting the checksums.");
        
        ChecksumsEventHandler eventHandler = new ChecksumsEventHandler();
        
        client.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), getFileIds(), 
                getChecksumSpec(), null, eventHandler, "IntegrityService: " + getName());
        while(eventHandler.isRunning()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                log.debug("Integrrupted while waiting for finish.", e);
            }
        }
    }
    
    /**
     * @return The FileIDs object for the specific file.
     */
    private FileIDs getFileIds() {
        FileIDs fileids = new FileIDs();
        fileids.setFileID(fileid);
        return fileids;
    }
    
    /**
     * @return The checksum specification for the collecting of the checksums.
     */
    private ChecksumSpecTYPE getChecksumSpec() {
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.fromValue(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        return csType;
    }
    
    /**
     * Handles the results of the GetChecksums conversation.
     * Sends a notify when everything is complete or failed.
     */
    private class ChecksumsEventHandler implements EventHandler {
        /** Tells whether the event has finished.*/
        private boolean isFinished = false;
        
        @Override
        public void handleEvent(OperationEvent event) {
            if(event.getType() == OperationEventType.COMPONENT_COMPLETE) {
                handleResult((ChecksumsCompletePillarEvent) event);
            } else if(event.getType() == OperationEventType.COMPLETE) {
                log.debug("Complete: " + event.toString());
                isFinished = true;
                notify();
            } else if(event.getType() == OperationEventType.FAILED) {
                log.warn("Failure: " + event.toString());
                isFinished = true;
                notify();
            }
        }
        
        /**
         * Handle the results of the GetChecksums operation at a single pillar.
         * @param event The event for the completion of a GetChecksums for a single pillar.
         */
        private void handleResult(ChecksumsCompletePillarEvent event) {
            store.addChecksums(event.getChecksums().getChecksumDataItems(), event.getChecksumType(), 
                    event.getContributorID());
        }
        
        /**
         * @return Whether the event is still running
         */
        public boolean isRunning() {
            return !isFinished;
        }        
    }
}
