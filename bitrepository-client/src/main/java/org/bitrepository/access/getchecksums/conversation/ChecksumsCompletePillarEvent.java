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
import org.bitrepository.client.eventhandler.ContributorCompleteEvent;

/**
 * Contains the result of a checksum request sent to a single pillar.
 */
public class ChecksumsCompletePillarEvent extends ContributorCompleteEvent {
    private final ResultingChecksums result;
    private final ChecksumSpecTYPE checksumType;
    private final boolean isPartialResult;

    /**
     * @param pillarID        The pillar which generated the result
     * @param collectionID    The ID of the collection
     * @param result          The result returned by the pillar.
     * @param checksumType    The checksum specification type.
     * @param isPartialResult Whether the complete event contains only a partial results set.
     */
    public ChecksumsCompletePillarEvent(String pillarID, String collectionID, ResultingChecksums result,
                                        ChecksumSpecTYPE checksumType, boolean isPartialResult) {
        super(pillarID, collectionID);
        this.result = result;
        this.checksumType = checksumType;
        this.isPartialResult = isPartialResult;
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

    /**
     * @return Whether it is a partial result set.
     */
    public boolean isPartialResult() {
        return isPartialResult;
    }

    @Override
    public String additionalInfo() {
        StringBuilder infoSB = new StringBuilder(super.additionalInfo());
        if (result != null && result.getChecksumDataItems() != null) {
            infoSB.append(", NumberOfChecksums=").append(result.getChecksumDataItems().size());
        }

        infoSB.append(", PartialResult=").append(isPartialResult);
        return infoSB.toString();
    }
}
