/*
 * #%L
 * Bitrepository Access
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfileids.conversation;

import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.ContributorCompleteEvent;

/**
 * Contains the result of a GetFileIDs request sent to a single pillar.
 */
public class FileIDsCompletePillarEvent extends ContributorCompleteEvent {
    private final ResultingFileIDs result; // The Result from the pillar.
    private final boolean isPartialResult;

    /**
     * @param pillarID        The pillar which generated the result
     * @param collectionID    The ID of the collection
     * @param result          The result returned by the pillar.
     * @param isPartialResult Indication if the event contains partial results
     */
    public FileIDsCompletePillarEvent(String pillarID, String collectionID, ResultingFileIDs result, boolean isPartialResult) {
        super(pillarID, collectionID);
        this.result = result;
        this.isPartialResult = isPartialResult;
    }

    /**
     * @return The results from the pillar, which has completed the GetFileIDs operation.
     */
    public ResultingFileIDs getFileIDs() {
        return result;
    }

    /**
     * @return Indication if the results are partial
     */
    public boolean isPartialResult() {
        return isPartialResult;
    }

    @Override
    public String additionalInfo() {
        StringBuilder infoSB = new StringBuilder(super.additionalInfo());

        if (result != null && result.getFileIDsData() != null) {
            infoSB.append(", NumberOfFileIDs=").append(result.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
        }

        infoSB.append(", PartialResult=").append(isPartialResult);
        return infoSB.toString();
    }
}
