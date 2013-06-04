/*
 * #%L
 * Bitrepository Command Line
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.commandline.clients;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.GetChecksumsEventHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetChecksumsOutputFormatter;
import org.bitrepository.commandline.resultmodel.GetChecksumsResultModel;

/**
 * Wrapper class for GetChecksumClient to handle paging through large result sets 
 */
public class PagingGetChecksumsClient {

    private final GetChecksumsClient client;
    private GetChecksumsResultModel model;
    private GetChecksumsEventHandler eventHandler;
    private final GetChecksumsOutputFormatter outputFormatter;
    private final OutputHandler outputHandler;
    private final static Integer PAGE_SIZE = 10000;
    private long timeout;
    
    public PagingGetChecksumsClient(GetChecksumsClient client, long timeout, GetChecksumsOutputFormatter outputFormatter,
            OutputHandler outputHandler) {
        this.client = client;
        this.timeout = timeout;
        this.outputFormatter = outputFormatter;
        this.outputHandler = outputHandler;
    }
    
    public boolean getChecksums(String collectionID, String fileID, List<String> pillarIDs, ChecksumSpecTYPE checksumSpec) {
        model = new GetChecksumsResultModel(pillarIDs);
        List<String> pillarsToGetFrom = pillarIDs;
        outputFormatter.formatHeader();
        
        while(!pillarsToGetFrom.isEmpty()) {
            eventHandler = new GetChecksumsEventHandler(model, timeout, outputHandler);
            ContributorQuery[] queries = makeQuery(pillarsToGetFrom);
            client.getChecksums(collectionID, queries, fileID, checksumSpec, null, eventHandler, "");
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
