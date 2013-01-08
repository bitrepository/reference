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
    /** The result from the pillar.*/
    private final ResultingFileIDs result;
    private final boolean isPartialResult;

    /**
     * @param result The result returned by the pillar.
     * @param pillarID The pillar which generated the result
     */
    public FileIDsCompletePillarEvent(String pillarID, ResultingFileIDs result, boolean isPartialResult) {
        super(pillarID);
        this.result = result;
        this.isPartialResult = isPartialResult;
    }

    /**
     * @return The results from the pillar, which has completed the GetFileIDs operation.
     */
    public ResultingFileIDs getFileIDs() {
        return result;
    }

    public boolean isPartialResult() {
        return isPartialResult;
    }

    @Override
    public String additionalInfo() {
        StringBuilder infoSB = new StringBuilder(super.additionalInfo());

        if (result != null && result.getFileIDsData() != null) {
            infoSB.append(", NumberOfFileIDs=" +
                    result.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
        }

        infoSB.append(", PartialResult=" + isPartialResult);
        return infoSB.toString();
    }
}
