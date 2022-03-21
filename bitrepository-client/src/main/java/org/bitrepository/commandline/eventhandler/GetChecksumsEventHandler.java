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
package org.bitrepository.commandline.eventhandler;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.resultmodel.GetChecksumsResultModel;

/**
 * Event handler for paging through GetChecksums results
 */
public class GetChecksumsEventHandler extends PagingEventHandler {

    private final GetChecksumsResultModel model;

    public GetChecksumsEventHandler(GetChecksumsResultModel model, Long timeout, OutputHandler outputHandler) {
        super(timeout, outputHandler);
        this.model = model;
    }

    protected void handleResult(OperationEvent event) {
        if (event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent pillarEvent = (ChecksumsCompletePillarEvent) event;
            if (pillarEvent.isPartialResult()) {
                pillarsWithPartialResults.add(pillarEvent.getContributorID());
            }
            model.addResults(pillarEvent.getContributorID(), pillarEvent.getChecksums());
        }
    }
}
