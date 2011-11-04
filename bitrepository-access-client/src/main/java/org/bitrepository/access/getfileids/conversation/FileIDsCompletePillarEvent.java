/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: ChecksumsCompletePillarEvent.java 372 2011-10-27 15:15:20Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getchecksums/conversation/ChecksumsCompletePillarEvent.java $
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
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

/**
* Contains the result of a GetFileIDs request sent to a single pillar.
*/
public class FileIDsCompletePillarEvent extends PillarOperationEvent{
    /** The result from the pillar.*/
    private final ResultingFileIDs result;
    
    /**
     * @param result The result returned by the pillar.
     * @param pillarID The pillar which generated the result
     * @param info Additional information.
     */
    public FileIDsCompletePillarEvent(ResultingFileIDs result, String pillarID, String info) {
        super(OperationEventType.PillarComplete, pillarID, info);
        this.result = result;
    }

    /**
     * @return The results from the pillar, which has completed the GetFileIDs operation.
     */
    public ResultingFileIDs getFileIDs() {
        return result;
    }
}
