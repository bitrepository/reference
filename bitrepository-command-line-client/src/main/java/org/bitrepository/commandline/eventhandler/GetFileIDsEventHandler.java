package org.bitrepository.commandline.eventhandler;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.GetFileIDsResultModel;

public class GetFileIDsEventHandler extends PagingEventHandler {
    private GetFileIDsResultModel model;
    
    public GetFileIDsEventHandler(GetFileIDsResultModel model, Long timeout, OutputHandler outputHandler) {
        super(timeout, outputHandler);
        this.model = model;
    }
    
    protected void handleResult(OperationEvent event) {
        if(event instanceof FileIDsCompletePillarEvent) {
            FileIDsCompletePillarEvent pillarEvent = (FileIDsCompletePillarEvent) event;
            if(pillarEvent.isPartialResult()) {
                pillarsWithPartialResults.add(pillarEvent.getContributorID());
            }
            model.addResults(pillarEvent.getContributorID(), pillarEvent.getFileIDs());
        }
    }
}
