package org.bitrepository.commandline.eventhandler;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;

/**
 * Complete event awaiter for Getfile.
 * Nothing to add. 
 */
public class GetFileEventHandler extends CompleteEventAwaiter {

    /**
     * Constructor.
     * @param settings
     * @param outputHandler
     */
    public GetFileEventHandler(Settings settings, OutputHandler outputHandler) {
        super(settings, outputHandler);
    }

    @Override
    public void handleComponentComplete(OperationEvent event) { }
}
