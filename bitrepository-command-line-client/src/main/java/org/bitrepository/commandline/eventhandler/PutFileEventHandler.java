package org.bitrepository.commandline.eventhandler;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;

/**
 * Complete event awaiter for Getfile.
 * Prints out checksum results, if any.
 */
public class PutFileEventHandler extends CompleteEventAwaiter {

    /**
     * Constructor.
     * @param settings
     * @param outputHandler
     */
    public PutFileEventHandler(Settings settings, OutputHandler outputHandler) {
        super(settings, outputHandler);
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if(!(event instanceof PutFileCompletePillarEvent)) {
            output.warn("PutFileEventHandler received a component complete, which is not a "
                    + PutFileCompletePillarEvent.class.getName());
        }
        
        PutFileCompletePillarEvent pillarEvent = (PutFileCompletePillarEvent) event;
        if(pillarEvent.getChecksums() != null) {
            output.resultLine(pillarEvent.getContributorID() + " \t " + Base16Utils.decodeBase16(pillarEvent.getChecksums().getChecksumValue()));
        }
    }

}
