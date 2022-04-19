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

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileinfos.GetFileInfosClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.GetFileInfosEventHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetFileInfosOutputFormatter;
import org.bitrepository.commandline.resultmodel.GetFileInfosResultModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Wrapper class for GetChecksumClient to handle paging through large result sets
 */
public class PagingGetFileInfosClient {
    private final GetFileInfosClient client;
    private GetFileInfosResultModel model;
    private final GetFileInfosOutputFormatter outputFormatter;
    private final OutputHandler outputHandler;
    private final long timeout;
    private final int pageSize;

    public PagingGetFileInfosClient(GetFileInfosClient client, long timeout, int pageSize, GetFileInfosOutputFormatter outputFormatter,
                                    OutputHandler outputHandler) {
        this.client = client;
        this.timeout = timeout;
        this.pageSize = pageSize;
        this.outputFormatter = outputFormatter;
        this.outputHandler = outputHandler;
    }

    public boolean getFileInfos(String collectionID, String fileID, List<String> pillarIDs, ChecksumSpecTYPE checksumSpec) {
        model = new GetFileInfosResultModel(pillarIDs);
        List<String> pillarsToGetFrom = pillarIDs;
        outputFormatter.formatHeader();

        while (!pillarsToGetFrom.isEmpty()) {
            GetFileInfosEventHandler eventHandler = new GetFileInfosEventHandler(model, timeout, outputHandler);
            ContributorQuery[] queries = makeQuery(pillarsToGetFrom);
            client.getFileInfos(collectionID, queries, fileID, checksumSpec, null, eventHandler, null);
            OperationEvent event = eventHandler.getFinish();
            if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                outputFormatter.formatResult(model.getUncompletedResults());
                return false;
            }
            pillarsToGetFrom = eventHandler.getPillarsWithPartialResults();
            outputFormatter.formatResult(model.getCompletedResults());
        }
        outputFormatter.formatResult(model.getUncompletedResults());
        return true;
    }


    private ContributorQuery[] makeQuery(List<String> pillars) {
        List<ContributorQuery> res = new ArrayList<>();
        for (String pillar : pillars) {
            Date latestResult = model.getLatestContribution(pillar);
            res.add(new ContributorQuery(pillar, latestResult, null, pageSize));
        }
        return res.toArray(new ContributorQuery[pillars.size()]);
    }
}