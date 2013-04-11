package org.bitrepository.commandline.eventhandler;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.GetChecksumsResultModel;

/**
 * Event handler for paging through GetChecksums results 
 */
public class GetChecksumsEventHandler extends PagingEventHandler {

    private GetChecksumsResultModel model;
    
    public GetChecksumsEventHandler(GetChecksumsResultModel model, Long timeout, OutputHandler outputHandler) {
        super(timeout, outputHandler);
        this.model = model;
    }

    protected void handleResult(OperationEvent event) {
        if(event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent pillarEvent = (ChecksumsCompletePillarEvent) event;
            if(pillarEvent.isPartialResult()) {
                pillarsWithPartialResults.add(pillarEvent.getContributorID());
            }
            model.addResults(pillarEvent.getContributorID(), pillarEvent.getChecksums());
        } 
    }
}
