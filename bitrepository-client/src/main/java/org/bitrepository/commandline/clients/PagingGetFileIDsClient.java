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
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.eventhandler.GetFileIDsEventHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetFileIDsOutputFormatter;
import org.bitrepository.commandline.resultmodel.GetFileIDsResultModel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper class for GetFileIDsClient to handle paging through large result sets
 */
public class PagingGetFileIDsClient {
    private final GetFileIDsClient client;
    private GetFileIDsResultModel model;
    private final GetFileIDsOutputFormatter outputFormatter;
    private final OutputHandler outputHandler;
    private final Duration timeout;
    private final int pageSize;

    public PagingGetFileIDsClient(GetFileIDsClient client, Duration timeout, int pageSize, GetFileIDsOutputFormatter outputFormatter,
                                  OutputHandler outputHandler) {
        this.client = client;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.pageSize = pageSize;
        this.outputFormatter = outputFormatter;
        this.outputHandler = outputHandler;
    }

    public boolean getFileIDs(String collectionID, String fileID, List<String> pillarIDs) {
        model = new GetFileIDsResultModel(pillarIDs);
        List<String> pillarsToGetFrom = pillarIDs;
        outputFormatter.formatHeader();

        while (!pillarsToGetFrom.isEmpty()) {
            GetFileIDsEventHandler eventHandler = new GetFileIDsEventHandler(model, timeout, outputHandler);
            ContributorQuery[] queries = makeQuery(pillarsToGetFrom);
            client.getFileIDs(collectionID, queries, fileID, null, eventHandler);
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
