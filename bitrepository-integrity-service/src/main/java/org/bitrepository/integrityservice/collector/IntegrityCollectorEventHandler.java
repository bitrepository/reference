package org.bitrepository.integrityservice.collector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The eventhandler for the integrity collector.
 * 
 * Notifies the monitor 
 */
public class IntegrityCollectorEventHandler implements EventHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    private final IntegrityAlerter alerter;
    /** The amount of */
    private final long timeout;
    
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> eventQueue = new LinkedBlockingQueue<OperationEvent>();

    
    /**
     * Constructor.
     * @param model The integrity model, where the results of GetChecksums or GetFileIDs are to be delivered.
     * @param alerter The alerter for sending failures.
     * @param timeout The maximum amount of millisecond to wait for an result.
     */
    public IntegrityCollectorEventHandler(IntegrityModel model, IntegrityAlerter alerter, long timeout) {
        this.store = model;
        this.alerter = alerter;
        this.timeout = timeout;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType() == OperationEventType.COMPONENT_COMPLETE) {
            handleResult(event);
        } else if(event.getType() == OperationEventType.COMPLETE) {
            log.debug("Complete: " + event.toString());
            eventQueue.add(event);
        } else if(event.getType() == OperationEventType.FAILED) {
            log.warn("Failure: " + event.toString());
            alerter.operationFailed("Failed integrity operation: " + event.toString());
            eventQueue.add(event);
        } else {
            log.debug("Received event: " + event.toString());
        }
    }
    
    public OperationEvent getFinish() throws InterruptedException {
        return eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle the results of the GetChecksums operation at a single pillar.
     * @param event The event for the completion of a GetChecksums for a single pillar.
     */
    private void handleResult(OperationEvent event) {
        if(event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent checksumEvent = (ChecksumsCompletePillarEvent) event;
            store.addChecksums(checksumEvent.getChecksums().getChecksumDataItems(), checksumEvent.getContributorID());
        } else if(event instanceof FileIDsCompletePillarEvent) {
            FileIDsCompletePillarEvent fileidEvent = (FileIDsCompletePillarEvent) event;
            store.addFileIDs(fileidEvent.getFileIDs().getFileIDsData(), FileIDsUtils.getAllFileIDs(), 
                    fileidEvent.getContributorID());
        } else {
            log.warn("Unexpected component complete event: " + event.toString());
        }
    }
}
