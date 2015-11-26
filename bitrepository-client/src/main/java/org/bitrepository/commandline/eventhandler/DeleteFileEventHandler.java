package org.bitrepository.commandline.eventhandler;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.modify.deletefile.conversation.DeleteFileCompletePillarEvent;

/**
 * Complete event awaiter for Getfile.
 * Prints out checksum results, if any.
 */
public class DeleteFileEventHandler extends CompleteEventAwaiter {

    /**
     * Constructor.
     * @param settings The {@link Settings}
     * @param outputHandler The {@link OutputHandler} for handling output
     */
    public DeleteFileEventHandler(Settings settings, OutputHandler outputHandler) {
        super(settings, outputHandler);
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if(!(event instanceof DeleteFileCompletePillarEvent)) {
            output.warn("DeleteFileEventHandler received a component complete, which is not a "
                    + DeleteFileCompletePillarEvent.class.getName());
        }
        
        DeleteFileCompletePillarEvent pillarEvent = (DeleteFileCompletePillarEvent) event;
        if(pillarEvent.getChecksums() != null) {
            output.resultLine(pillarEvent.getContributorID() + " \t " + Base16Utils.decodeBase16(pillarEvent.getChecksums().getChecksumValue()));
        }
    }

}
