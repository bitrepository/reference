package org.bitrepository.commandline.clients;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.GetFileIDsEventHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetFileIDsOutputFormatter;
import org.bitrepository.commandline.resultmodel.GetFileIDsResultModel;

/**
 * Wrapper class for GetFileIDsClient to handle paging through large result sets 
 */
public class PagingGetFileIDsClient {

    private final GetFileIDsClient client;
    private GetFileIDsResultModel model;
    private GetFileIDsEventHandler eventHandler;
    private GetFileIDsOutputFormatter outputFormatter;
    private final OutputHandler outputHandler;
    private final static Integer PAGE_SIZE = 10000;
    private long timeout;
      
    public PagingGetFileIDsClient(GetFileIDsClient client, long timeout, GetFileIDsOutputFormatter outputFormatter,
            OutputHandler outputHandler) {
        this.client = client;
        this.timeout = timeout;
        this.outputFormatter = outputFormatter;
        this.outputHandler = outputHandler;
    }
    
    public boolean getFileIDs(String collectionID, String fileID, List<String> pillarIDs) {
        model = new GetFileIDsResultModel(pillarIDs);
        List<String> pillarsToGetFrom = pillarIDs;
        outputFormatter.formatHeader();
        
        while(!pillarsToGetFrom.isEmpty()) {
            eventHandler = new GetFileIDsEventHandler(model, timeout, outputHandler);
            ContributorQuery[] queries = makeQuery(pillarsToGetFrom);
            client.getFileIDs(collectionID, queries, fileID, null, eventHandler);
            OperationEvent event = eventHandler.getFinish();
            if(event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                return false;
            }
            pillarsToGetFrom = eventHandler.getPillarsWithPartialResults();
            outputFormatter.formatResult(model.getCompletedResults());
        }
        outputFormatter.formatResult(model.getUncompletedResults());
        return true;
    }
    
    
    private ContributorQuery[] makeQuery(List<String> pillars) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        for(String pillar : pillars) {
            Date latestResult = model.getLatestContribution(pillar);
            res.add(new ContributorQuery(pillar, latestResult, null, PAGE_SIZE));
        }
        return res.toArray(new ContributorQuery[pillars.size()]);
    }
}
