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
package org.bitrepository.access.getchecksums.conversation;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

/**
* Contains the result of a checksum request sent to a single pillar.
*/
public class ChecksumsCompletePillarEvent extends PillarOperationEvent {
    /** @see #getChecksums(). */
    private final ResultingChecksums result;
    /** @see #getChecksumType(). */
    private final ChecksumSpecTYPE checksumType;
    
    /**
     * @param result The result returned by the pillar.
     * @param pillarID The pillar which generated the result
     * @param info Additional information.
     */
    public ChecksumsCompletePillarEvent(ResultingChecksums result, ChecksumSpecTYPE checksumType, String pillarID, 
            String info) {
        super(OperationEventType.PillarComplete, pillarID, info);
        this.result = result;
        this.checksumType = checksumType;
    }

    /** 
     * @return The checksum result from a single pillar. 
     */
    public ResultingChecksums getChecksums() {
        return result;
    }
    
    /**
     * @return The checksum calculation specifics (e.g. the algorithm and optionally salt).
     */
    public ChecksumSpecTYPE getChecksumType() {
        return checksumType;
    }
}
