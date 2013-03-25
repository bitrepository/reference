package org.bitrepository.commandline.clients;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.GetFileIDsEventHandler;
import org.bitrepository.commandline.resultmodel.GetFileIDsResultModel;

public class PagingGetFileIDsClient {

    private final GetFileIDsClient client;
    private GetFileIDsResultModel model;
    private GetFileIDsEventHandler eventHandler;
    private final static Integer PAGE_SIZE = 10000;
    private long timeout;
      
    public PagingGetFileIDsClient(GetFileIDsClient client, long timeout) {
        this.client = client;
        this.timeout = timeout;
    }
    
    public boolean getFileIDs(String collectionID, String fileID, List<String> pillarIDs) throws InterruptedException {
        model = new GetFileIDsResultModel(pillarIDs);
        List<String> pillarsToGetFrom = pillarIDs;
        
        while(!pillarsToGetFrom.isEmpty()) {
            eventHandler = new GetFileIDsEventHandler(model, timeout);
            ContributorQuery[] queries = makeQuery(pillarsToGetFrom);
            client.getFileIDs(collectionID, queries, fileID, null, eventHandler);
            OperationEvent event = eventHandler.getFinish();
            if(event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                return false;
            }
            pillarsToGetFrom = eventHandler.getPillarsWithPartialResults();
            // model.getCompletedResults() // Do something with the completed results
        }
        // model.getUncompletedResults() // Do something with the uncompleted results
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
